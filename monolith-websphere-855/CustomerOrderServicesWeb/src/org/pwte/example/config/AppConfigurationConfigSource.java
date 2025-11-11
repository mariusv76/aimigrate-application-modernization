package org.pwte.example.config;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
 * MicroProfile ConfigSource implementation that retrieves configuration from Azure App Configuration.
 * Uses managed identity (DefaultAzureCredential) for authentication.
 * Implements caching with configurable TTL and supports dynamic refresh.
 * 
 * Configuration properties:
 * - azure.appconfiguration.endpoint: App Configuration endpoint (e.g., https://appconfig-customerorder.azconfig.io)
 * - azure.appconfiguration.label: Label filter (default: environment from AZURE_ENVIRONMENT or empty)
 * - azure.appconfiguration.cache.ttl.seconds: Cache TTL in seconds (default: 60)
 * 
 * Keys are accessed directly without prefix, e.g., "db:host", "telemetry:sampling"
 */
public class AppConfigurationConfigSource implements ConfigSource {
    
    private static final Logger LOGGER = Logger.getLogger(AppConfigurationConfigSource.class.getName());
    private static final int DEFAULT_ORDINAL = 250; // Lower than Key Vault but higher than default
    private static final long DEFAULT_CACHE_TTL_SECONDS = 60; // 1 minute for dynamic config
    
    private final ConfigurationClient configClient;
    private final Map<String, CachedConfig> cache = new ConcurrentHashMap<>();
    private final long cacheTtlMillis;
    private final String label;
    private final boolean enabled;
    private Instant lastFullRefresh = Instant.MIN;
    
    private static class CachedConfig {
        final String value;
        final Instant timestamp;
        
        CachedConfig(String value) {
            this.value = value;
            this.timestamp = Instant.now();
        }
        
        boolean isExpired(long ttlMillis) {
            return Duration.between(timestamp, Instant.now()).toMillis() > ttlMillis;
        }
    }
    
    public AppConfigurationConfigSource() {
        String endpoint = System.getenv("AZURE_APPCONFIGURATION_ENDPOINT");
        String cacheTtlStr = System.getenv("AZURE_APPCONFIGURATION_CACHE_TTL_SECONDS");
        
        if (endpoint == null || endpoint.isEmpty()) {
            LOGGER.info("Azure App Configuration not configured (AZURE_APPCONFIGURATION_ENDPOINT not set). ConfigSource disabled.");
            this.configClient = null;
            this.cacheTtlMillis = DEFAULT_CACHE_TTL_SECONDS * 1000;
            this.label = null;
            this.enabled = false;
            return;
        }
        
        try {
            this.configClient = new ConfigurationClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
            
            this.cacheTtlMillis = parseCacheTtl(cacheTtlStr);
            this.label = determineLabel();
            this.enabled = true;
            
            LOGGER.info(String.format("Azure App Configuration ConfigSource initialized: endpoint=%s, label=%s, cacheTtl=%ds", 
                endpoint, label != null ? label : "(no label)", cacheTtlMillis / 1000));
                
            // Initial load
            refreshAll();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Azure App Configuration client", e);
            throw new RuntimeException("Failed to initialize App Configuration ConfigSource", e);
        }
    }
    
    private String determineLabel() {
        // Priority: explicit label > environment variable > null (no label filter)
        String explicitLabel = System.getenv("AZURE_APPCONFIGURATION_LABEL");
        if (explicitLabel != null && !explicitLabel.isEmpty()) {
            return explicitLabel;
        }
        
        String environment = System.getenv("AZURE_ENVIRONMENT");
        if (environment != null && !environment.isEmpty()) {
            return environment;
        }
        
        return null; // No label filter
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
    
    /**
     * Refresh all configuration from App Configuration.
     * Called on initialization and periodically to enable dynamic refresh.
     */
    private void refreshAll() {
        if (!enabled) {
            return;
        }
        
        try {
            SettingSelector selector = new SettingSelector();
            if (label != null) {
                selector.setLabelFilter(label);
            }
            
            Map<String, CachedConfig> newCache = new ConcurrentHashMap<>();
            
            configClient.listConfigurationSettings(selector).forEach(setting -> {
                if (setting.getKey() != null && setting.getValue() != null) {
                    newCache.put(setting.getKey(), new CachedConfig(setting.getValue()));
                }
            });
            
            cache.clear();
            cache.putAll(newCache);
            lastFullRefresh = Instant.now();
            
            LOGGER.info(String.format("Refreshed %d configuration settings from App Configuration", cache.size()));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh configuration from App Configuration", e);
        }
    }
    
    @Override
    public Map<String, String> getProperties() {
        if (!enabled) {
            return new HashMap<>();
        }
        
        // Refresh if cache expired
        if (Duration.between(lastFullRefresh, Instant.now()).toMillis() > cacheTtlMillis) {
            refreshAll();
        }
        
        Map<String, String> properties = new HashMap<>();
        cache.forEach((key, cachedConfig) -> properties.put(key, cachedConfig.value));
        return properties;
    }
    
    @Override
    public Set<String> getPropertyNames() {
        return getProperties().keySet();
    }
    
    @Override
    public String getValue(String propertyName) {
        if (!enabled) {
            return null;
        }
        
        // Check if full cache refresh needed
        if (Duration.between(lastFullRefresh, Instant.now()).toMillis() > cacheTtlMillis) {
            refreshAll();
        }
        
        CachedConfig cached = cache.get(propertyName);
        if (cached != null) {
            LOGGER.fine(String.format("Cache hit for config key: %s", propertyName));
            return cached.value;
        }
        
        // Try to fetch single key if not in cache
        try {
            LOGGER.fine(String.format("Fetching config key from App Configuration: %s", propertyName));
            ConfigurationSetting setting = configClient.getConfigurationSetting(propertyName, label);
            
            if (setting != null && setting.getValue() != null) {
                cache.put(propertyName, new CachedConfig(setting.getValue()));
                return setting.getValue();
            }
            
            return null;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, String.format("Config key '%s' not found in App Configuration", propertyName), e);
            return null;
        }
    }
    
    @Override
    public String getName() {
        return "azure-appconfiguration";
    }
    
    @Override
    public int getOrdinal() {
        return DEFAULT_ORDINAL;
    }
}
