# TASK-006 Progress Tracker

Start Date: 2025-11-11
Branch: migration/task-006-azure-integration
Status: In Progress - Phase 2 Complete

## Milestones
- [x] Draft execution plan created (`.vscode/transformation/TASK-006/plan.md`)
- [x] Progress tracker initialized (this file)
- [x] Todos file created (`.vscode/transformation/TASK-006/todos.md`)
- [x] Infra folder scaffolded
- [x] Bicep stubs committed
- [x] Strategy documents created (secrets, auth, observability)
- [x] Deployment agent contract specification
- [x] Azure resources provisioned (RG, KV, AppConfig, Insights, ACR, Container Apps)
- [x] Key Vault RBAC enabled
- [x] Key Vault secrets added (db-password)
- [x] App Configuration values added (db:host, db:port, db:name, telemetry:sampling)
- [x] Azure SDK dependencies added to POM
- [x] KeyVaultConfigSource implemented
- [x] AppConfigurationConfigSource implemented
- [x] ConfigSource implementations registered
- [ ] Entra ID app registration created
- [ ] Security annotations migrated
- [ ] OpenTelemetry agent added to Docker image
- [ ] Managed identity role assignments configured
- [ ] Docker image built and pushed to ACR
- [ ] Container App updated with new image
- [ ] Deployment validated
- [ ] Telemetry validated in Application Insights
- [ ] Documentation finalized

## Activity Log
| Timestamp | Activity | Details |
|-----------|----------|---------|
| 2025-11-11 09:00 | Plan Draft | Added initial Azure integration execution plan |
| 2025-11-11 09:15 | Infra Scaffold | Created Bicep main + modules (kv, appconfig, insights, acr, appservice) |
| 2025-11-11 09:30 | Tracking Files | Progress & todos relocated per updated instructions |
| 2025-11-11 10:00 | Strategy Docs | Created secrets-strategy.md, auth-strategy.md, observability-strategy.md |
| 2025-11-11 10:30 | Deployment Contract | Created deployment-agent-inputs.md specification |
| 2025-11-11 11:00 | Branch Created | Created migration/task-006-azure-integration branch |
| 2025-11-11 12:00 | Bicep Modules | Added containerapps.bicep, updated parameters for North Europe |
| 2025-11-11 13:00 | Infrastructure Deployed | Deployed KV, AppConfig, Insights, ACR, Container Apps to North Europe |
| 2025-11-11 13:30 | Azure SDK Added | Added azure-sdk-bom and dependencies to parent POM |
| 2025-11-11 14:00 | ConfigSource Impl | Implemented KeyVaultConfigSource with caching and managed identity |
| 2025-11-11 14:15 | ConfigSource Impl | Implemented AppConfigurationConfigSource with dynamic refresh |
| 2025-11-11 14:30 | Secrets Stored | Added db-password to Key Vault, db config to App Configuration |

## Blockers / Risks
None currently.

## Architectural Decisions

### Spring Boot vs MicroProfile/Open Liberty
**Decision**: Use MicroProfile ConfigSource instead of Spring Cloud Azure libraries.

**Reasoning**:
1. **Current Runtime**: Application runs on Open Liberty 24.0.0.11 with Jakarta EE 10 and MicroProfile 6.0
2. **No Spring Framework**: The application does not use Spring Boot, Spring Security, or any Spring dependencies
3. **Technology Stack**: 
   - REST: Jakarta JAX-RS (not Spring MVC)
   - DI: Jakarta CDI (not Spring DI)
   - Security: MicroProfile JWT (not Spring Security)
   - Config: MicroProfile Config (not Spring Boot @ConfigurationProperties)
4. **TASK-006 Instructions Written for Spring**: The original instructions assume Spring Cloud Azure dependencies, but these are incompatible with Jakarta EE/Liberty
5. **Correct Approach**: 
   - Implemented custom MicroProfile ConfigSource implementations for Key Vault and App Configuration
   - Used Azure SDK directly (azure-identity, azure-security-keyvault-secrets, azure-data-appconfiguration)
   - Integrated via ServiceLoader mechanism (org.eclipse.microprofile.config.spi.ConfigSource)
   - Maintained Liberty runtime without introducing Spring framework

**Impact**: 
- No Spring dependencies added
- Full compatibility with existing Liberty/Jakarta EE architecture
- Cleaner integration using MicroProfile standards
- Managed identity works same way (DefaultAzureCredential)
- Same Azure services used (Key Vault, App Configuration, Application Insights)

**References**:
- MicroProfile Config Spec: https://microprofile.io/project/eclipse/microprofile-config
- Liberty MicroProfile: https://openliberty.io/docs/latest/microprofile.html
- Azure SDK for Java: https://learn.microsoft.com/azure/developer/java/sdk/

## Phase Completion Status

### Phase 1: Planning & Infrastructure ✅ COMPLETE
- Execution plan documented
- Strategy documents created (secrets, auth, observability)
- Bicep infrastructure scaffolded and deployed
- Azure resources provisioned successfully
  - Resource Group: rg-customerorder-dev (North Europe)
  - Key Vault: kv-customerorder-dev (RBAC-enabled)
  - App Configuration: appconfig-customerorder
  - Application Insights: customerorder-insights
  - Container Registry: customerorderdevacr.azurecr.io
  - Container Apps Environment: cae-customerorder-dev
  - Container App: ca-customerorder-dev

### Phase 2: SDK Integration & Configuration ✅ COMPLETE
- Azure SDK dependencies added (azure-identity, keyvault-secrets, appconfiguration)
- KeyVaultConfigSource implemented with:
  - DefaultAzureCredential for managed identity auth
  - 5-minute caching with TTL
  - Fallback to stale cache on errors
- AppConfigurationConfigSource implemented with:
  - 1-minute cache for dynamic refresh
  - Label filtering by environment
  - Full refresh on cache expiration
- ConfigSource implementations registered via ServiceLoader
- Secrets stored in Key Vault: db-password
- Configuration stored in App Configuration:
  - db:host = postgres-customerorder
  - db:port = 5432
  - db:name = orderdb
  - telemetry:sampling = 0.1

### Phase 3: Authentication & Authorization 🔄 IN PROGRESS
- [ ] Create Entra ID App Registration
- [ ] Configure app roles (Orders.Read, Orders.Write)
- [ ] Update microprofile-config.properties with Tenant/Client IDs
- [ ] Migrate @RolesAllowed annotations

### Phase 4: Observability & Deployment 📋 PENDING
- [ ] Update Dockerfile with OpenTelemetry agent
- [ ] Configure role assignments for Container App managed identity
- [ ] Build and push Docker image to ACR
- [ ] Update Container App with new image
- [ ] Verify telemetry in Application Insights

### Phase 5: Documentation & Validation 📋 PENDING
- [ ] Final handoff documentation
- [ ] Deployment validation checklist

## Next Steps
1. Create Entra ID App Registration (customerorder-api-dev)
2. Configure app roles for Orders.Read and Orders.Write
3. Update microprofile-config.properties with actual values
4. Migrate security annotations from @RolesAllowed("SecureShopper")
