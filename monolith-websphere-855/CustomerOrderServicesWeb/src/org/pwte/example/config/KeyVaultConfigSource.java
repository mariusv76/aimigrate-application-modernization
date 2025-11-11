package org.pwte.example.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MicroProfile ConfigSource implementation that retrieves secrets from Azure Key Vault.
 * Uses managed identity (DefaultAzureCredential) for authentication.
 * Implements caching with configurable TTL to minimize Key Vault API calls.
 * 
 * Configuration properties:
 * - azure.keyvault.endpoint: Key Vault URI (e.g., https://kv-customerorder-dev.vault.azure.net/)
 * - azure.keyvault.cache.ttl.seconds: Cache TTL in seconds (default: 300)
 * 
 * Secrets are accessed with "secret." prefix, e.g., "secret.db-password"
 */
public class KeyVaultConfigSource implements ConfigSource {
    
    private static final Logger LOGGER = Logger.getLogger(KeyVaultConfigSource.class.getName());
    private static final String SECRET_PREFIX = "secret.";
    private static final int DEFAULT_ORDINAL = 300; // Higher than default sources
    private static final long DEFAULT_CACHE_TTL_SECONDS = 300; // 5 minutes
    
    private final SecretClient secretClient;
    private final Map<String, CachedSecret> cache = new ConcurrentHashMap<>();
    private final long cacheTtlMillis;
    private final boolean enabled;
    
    private static class CachedSecret {
        final String value;
        final Instant timestamp;
        
        CachedSecret(String value) {
            this.value = value;
            this.timestamp = Instant.now();
        }
        
        boolean isExpired(long ttlMillis) {
            return Duration.between(timestamp, Instant.now()).toMillis() > ttlMillis;
        }
    }
    
    public KeyVaultConfigSource() {
        String vaultEndpoint = System.getenv("AZURE_KEYVAULT_ENDPOINT");
        String cacheTtlStr = System.getenv("AZURE_KEYVAULT_CACHE_TTL_SECONDS");
        
        if (vaultEndpoint == null || vaultEndpoint.isEmpty()) {
            LOGGER.info("Azure Key Vault not configured (AZURE_KEYVAULT_ENDPOINT not set). ConfigSource disabled.");
            this.secretClient = null;
            this.cacheTtlMillis = DEFAULT_CACHE_TTL_SECONDS * 1000;
            this.enabled = false;
            return;
        }
        
        try {
            this.secretClient = new SecretClientBuilder()
                .vaultUrl(vaultEndpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
            
            this.cacheTtlMillis = parseCacheTtl(cacheTtlStr);
            this.enabled = true;
            
            LOGGER.info(String.format("Azure Key Vault ConfigSource initialized: endpoint=%s, cacheTtl=%ds", 
                vaultEndpoint, cacheTtlMillis / 1000));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Azure Key Vault client", e);
            throw new RuntimeException("Failed to initialize Key Vault ConfigSource", e);
        }
    }
    
    private long parseCacheTtl(String ttlStr) {
        if (ttlStr == null || ttlStr.isEmpty()) {
            return DEFAULT_CACHE_TTL_SECONDS * 1000;
        }
        try {
            return Long.parseLong(ttlStr) * 1000;
        } catch (NumberFormatException e) {
            LOGGER.warning(String.format("Invalid cache TTL value '%s', using default %d seconds", 
                ttlStr, DEFAULT_CACHE_TTL_SECONDS));
            return DEFAULT_CACHE_TTL_SECONDS * 1000;
        }
    }
    
    @Override
    public Map<String, String> getProperties() {
        if (!enabled) {
            return new HashMap<>();
        }
        
        // Return cached secrets only (don't enumerate all secrets in Key Vault)
        Map<String, String> properties = new HashMap<>();
        cache.forEach((key, cachedSecret) -> {
            if (!cachedSecret.isExpired(cacheTtlMillis)) {
                properties.put(SECRET_PREFIX + key, cachedSecret.value);
            }
        });
        return properties;
    }
    
    @Override
    public Set<String> getPropertyNames() {
        return getProperties().keySet();
    }
    
    @Override
    public String getValue(String propertyName) {
        if (!enabled || !propertyName.startsWith(SECRET_PREFIX)) {
            return null;
        }
        
        String secretName = propertyName.substring(SECRET_PREFIX.length());
        
        // Check cache first
        CachedSecret cached = cache.get(secretName);
        if (cached != null && !cached.isExpired(cacheTtlMillis)) {
            LOGGER.fine(String.format("Cache hit for secret: %s", secretName));
            return cached.value;
        }
        
        // Fetch from Key Vault
        try {
            LOGGER.fine(String.format("Fetching secret from Key Vault: %s", secretName));
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            
            if (secret != null && secret.getValue() != null) {
                cache.put(secretName, new CachedSecret(secret.getValue()));
                return secret.getValue();
            }
            
            LOGGER.warning(String.format("Secret not found in Key Vault: %s", secretName));
            return null;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("Failed to retrieve secret '%s' from Key Vault", secretName), e);
            
            // Return stale cache value if available
            if (cached != null) {
                LOGGER.info(String.format("Returning stale cached value for secret: %s", secretName));
                return cached.value;
            }
            
            return null;
        }
    }
    
    @Override
    public String getName() {
        return "azure-keyvault";
    }
    
    @Override
    public int getOrdinal() {
        return DEFAULT_ORDINAL;
    }
}
