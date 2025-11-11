# TASK-006 Secrets Strategy (Draft)

## Objectives
Eliminate hardcoded credentials. Centralize secrets in Azure Key Vault. Consume them via Managed Identity and configuration abstraction compatible with existing Open Liberty MicroProfile Config.

## Target Secrets
| Secret | Key | Source | Notes |
|--------|-----|--------|-------|
| PostgreSQL DB Password | db-password | Key Vault | Rotated manually initially |
| Entra ID Client Secret (if confidential client needed) | entra-client-secret | Key Vault | Prefer certificate auth later |
| Future: Encryption/Signing Keys | app-signing-key | Key Vault | For JWT signing if self-issued |

## Access Model
- Use system-assigned managed identity of App Service/Container App.
- RBAC assignments: `Key Vault Secrets User` role (list/get) on vault scope.
- Avoid access policies; rely on Azure AD RBAC.

## Retrieval Approaches
1. Direct SDK (azure-security-keyvault-secrets) wrapper bridging to MicroProfile Config source.
2. Environment variable injection using startup init container (less flexible) — NOT chosen.
3. Key Vault references in App Service App Settings (limited dynamic refresh) — optional for small set.

Chosen: Custom ConfigSource reading secrets via SDK with in-memory cache (TTL 5 minutes) and graceful fallback to env vars for local dev.

## Local Development Fallback
If Azure credentials/managed identity unavailable:
- Load from `.env.local` file (NOT committed) or existing `bootstrap.properties`.
- Provide sample file: `bootstrap.properties.sample` documenting required keys.

## Cache Strategy
- Cache Map<String, SecretValueWithTimestamp>
- TTL property: `secret.cache.ttl.seconds` default 300.
- On secret miss or expired → fetch from Key Vault.
- Handle transient failures with exponential backoff (max 3 attempts, base delay 200ms, jitter).

## Error Handling
- If Key Vault unreachable: log warning, fallback to previous cached value; if none, mark readiness degraded (future enhancement).
- Circuit breaker state after consecutive failures (>5 within 2 minutes) to avoid thrashing.

## Rotation Handling
- Short TTL ensures updated secret fetched.
- Manual trigger endpoint (secured) could clear cache (future).

## Implementation Steps
1. Add Azure SDK dependencies (NOT Spring Cloud due to Liberty stack) - use `com.azure:azure-security-keyvault-secrets` & `com.azure:azure-identity`.
2. Implement `KeyVaultConfigSource` extending MicroProfile `ConfigSource`.
3. Inject via `META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` service loader.
4. Provide mapping prefix: `secret.<name>` → Key Vault secret name (e.g., `secret.db-password`).
5. Update datasource password reference in `server.xml` to `${secret.db-password}` after integration ready.

## Security Considerations
- No secret values logged.
- Use default Azure credential chain: Managed Identity → Environment → Azure CLI (dev).
- Ensure Key Vault purge protection remains enabled.
- Avoid exporting secrets via metrics.

## Pending
- Validate azure SDK compatibility with Liberty classloading (likely requires inclusion in shared library).
- Add unit test with mock Key Vault client.

## Next Actions
- Add Azure SDK deps to parent POM (provided scope until runtime packaging strategy decided).
- Scaffold config source class & service registration.
