# Configuration Externalization (Java)

## Objectives

Adopt Spring Cloud Azure App Configuration + Key Vault + Feature Flags replacing property file proliferation & manual secret handling.

## Key Dependencies (Gradle)

```kotlin
dependencies {
  implementation("com.azure.spring:azure-spring-cloud-appconfiguration-config")
  implementation("com.azure.spring:azure-spring-cloud-feature-management")
  implementation("com.azure.spring:azure-spring-cloud-starter-keyvault-secrets")
  implementation("com.azure:azure-identity")
}
```

## Bootstrap / Connection

`application.yml` (or `bootstrap.yml` if using legacy bootstrap context):

```yaml
spring:
  cloud:
    azure:
      appconfiguration:
        stores:
          - endpoint: ${APP_CONFIG_ENDPOINT}
            monitoring:
              refresh-interval: 30s
              triggers:
                - key: __configSentinel
    feature-management:
      feature-flags-cache-expiration: 15s
```

Use managed identity or workload identity; no connection string embedding.

## Key Vault Secret References

Define a Key Vault reference in App Configuration: `@Microsoft.KeyVault(SecretUri=https://<vault>.vault.azure.net/secrets/DbPassword/<version>)`.

Secrets flow through automatically; no code for secret client required.

## Strongly Typed Binding

```kotlin
@ConfigurationProperties(prefix = "cache")
@ConstructorBinding
data class CacheSettings(val ttlSeconds: Int)
```

Register:

```kotlin
@EnableConfigurationProperties(CacheSettings::class)
class CacheConfig
```

Validation via `@Validated` + JSR-303 annotations.

## Feature Flag Usage

```kotlin
@RestController
class BetaController(private val featureManager: FeatureManagerSnapshot) {
  @GetMapping("/beta")
  fun beta(): ResponseEntity<String> =
    if (featureManager.isEnabled("NewBetaFlow")) ResponseEntity.ok("beta") else ResponseEntity.notFound().build()
}
```

## Dynamic Refresh

Triggered when `__configSentinel` key updated; environment refreshes and beans rebind. Ensure beans needing live updates use `@RefreshScope` if not constructor bound.

## Validation Checklist

- [ ] No direct `System.getProperty` or raw `Environment.getProperty` calls for business config.
- [ ] All secrets indirect via Key Vault references.
- [ ] Feature flag toggles apply without restart.
- [ ] Config classes use constructor binding or refresh scope appropriately.

## Rollback

Remove App Configuration dependency entries and disable store block; fall back to local `application.yml` values (retain external keys for rapid reactivation).

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Inconsistent refresh timing | Use single sentinel + monitoring interval tuning |
| Accidental secret log | Avoid printing resolved property maps; enable logging filters |
| Excess flags | Governance: naming + review cadence |
