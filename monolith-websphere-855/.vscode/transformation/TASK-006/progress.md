# TASK-006 Progress Tracker

Start Date: 2025-11-11
Branch: migration/task-006-azure-integration
Status: In Progress - Phase 4 (Observability) In Progress

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
- [x] Entra ID app registration created
- [x] Security annotations verified (none exist - infrastructure ready)
- [x] OpenTelemetry agent added to Docker image
- [x] Docker image built and tested locally
- [x] Local testing validated (PostgreSQL + REST API + OTEL)
- [x] Azure PostgreSQL Flexible Server provisioned (psql-customerorder-dev)
- [x] Database schema and data migrated to Azure PostgreSQL
- [ ] Managed identity role assignments configured
- [ ] Docker image pushed to ACR
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
| 2025-11-11 14:45 | Progress Updated | Documented Phase 1 & 2 completion, Spring Boot vs MicroProfile decision |
| 2025-11-11 15:00 | Entra ID App Registration | Created customerorder-api-dev with Orders.Read/Orders.Write roles |
| 2025-11-11 15:15 | JWT Config Updated | Updated microprofile-config.properties with actual tenant/client IDs |
| 2025-11-11 15:20 | Security Annotations | Verified no existing @RolesAllowed annotations; security layer to be added fresh |
| 2025-11-11 16:00 | Dockerfile Created | Created Dockerfile.liberty with OpenTelemetry agent integration |
| 2025-11-11 16:10 | Docker Compose Added | Created docker-compose.yml for local testing |
| 2025-11-11 16:15 | Dockerignore Added | Created .dockerignore to optimize build |
| 2025-11-11 16:30 | Dockerfile Fixed | Corrected Liberty base image tag, PostgreSQL driver location to /config/resources/ |
| 2025-11-11 16:45 | Bootstrap Properties | Updated bootstrap.properties to use ${env.VAR} substitution for database config |
| 2025-11-11 17:00 | Docker Network | Created custom network for container DNS resolution between app and PostgreSQL |
| 2025-11-11 17:15 | Local Testing Complete | Successfully validated: Liberty startup, DB connection, REST API (/api/customers/business), OTEL agent loaded |
| 2025-11-11 17:30 | Azure PostgreSQL | Provisioned Azure Database for PostgreSQL Flexible Server (psql-customerorder-dev.postgres.database.azure.com) |
| 2025-11-11 17:35 | Database Password | Generated strong password, stored in Key Vault as db-password secret |
| 2025-11-11 17:40 | Firewall Rule | Added firewall rule to allow connection from development IP (77.173.178.210) |
| 2025-11-11 17:45 | Schema Migration | Exported schema from local PostgreSQL, imported to Azure PostgreSQL (12 tables) |
| 2025-11-11 17:50 | Data Migration | Migrated 2 customers, 2 suppliers to Azure PostgreSQL using pg_dump/restore |
| 2025-11-11 18:00 | Role Assignments | Granted Container App managed identity access to Key Vault, App Configuration, ACR |
| 2025-11-11 18:10 | ACR Push | Tagged and pushed Docker image (latest, v1.0.0) to customerorderdevacr.azurecr.io |
| 2025-11-11 18:15 | Container App Update | Deployed image with Azure PostgreSQL connection, Application Insights integration |
| 2025-11-11 18:20 | Deployment Validation | Verified health endpoints UP, datasource connected to Azure PostgreSQL |

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
  - PostgreSQL Flexible Server: psql-customerorder-dev (Standard_B2s, 32GB storage, PostgreSQL 16)

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
- Secrets stored in Key Vault: db-password (32-character secure password for Azure PostgreSQL)
- Configuration stored in App Configuration:
  - db:host = psql-customerorder-dev.postgres.database.azure.com
  - db:port = 5432
  - db:name = orderdb
  - telemetry:sampling = 0.1

### Phase 3: Authentication & Authorization ✅ COMPLETE
- [x] Create Entra ID App Registration
- [x] Configure app roles (Orders.Read, Orders.Write)
- [x] Update microprofile-config.properties with Tenant/Client IDs
- [x] Verify security annotations status (none exist - will be added in future phase)

**Entra ID Details**:
- App Registration: customerorder-api-dev
- Client ID: 3d3c655f-43b5-4130-816c-74493287e79b
- Tenant ID: 89cea9ee-73b0-4f74-9541-b8153dff5960
- App Roles: Orders.Read, Orders.Write
- Documentation: `.vscode/transformation/TASK-006/entra-id-registration.md`

**Note**: Application currently has no @RolesAllowed security annotations. JWT validation infrastructure is configured and ready for future security implementation.

### Phase 4: Observability & Deployment 🔄 IN PROGRESS

- [x] Update Dockerfile with OpenTelemetry agent
  - Created `Deployment/Dockerfile.liberty` with multi-stage build
  - Stage 1: Build application with Maven
  - Stage 2: Download OpenTelemetry Java agent 2.10.0
  - Stage 3: Runtime with Open Liberty base image (full-java17-openj9-ubi)
  - Configured OTEL environment variables
  - Added health check endpoint
  - Non-root user (1001)
  - Fixed PostgreSQL driver location (/config/resources/)
- [x] Create Docker Compose for local testing
  - PostgreSQL service with init scripts
  - Application service with environment variables
  - Health checks for both services
- [x] Create .dockerignore for optimized builds
- [x] Fix bootstrap.properties for environment variable substitution
  - Changed from hardcoded values to ${env.VAR} syntax
  - Allows runtime configuration via Docker environment variables
- [x] Local Docker testing complete
  - Created custom Docker network (customerorder-net) for DNS resolution
  - Validated Liberty startup (55 seconds)
  - Validated database connectivity (PostgreSQL on port 5432)
  - Validated REST API endpoints (/CustomerOrderServicesWeb/api/customers/business)
  - Verified OpenTelemetry agent loaded successfully (version 2.10.0)
  - Confirmed MicroProfile Telemetry feature active
- [x] Provision Azure Database for PostgreSQL Flexible Server
  - Created psql-customerorder-dev.postgres.database.azure.com
  - SKU: Standard_B2s (Burstable, 2 vCPU, 4GB RAM)
  - Storage: 32 GB with auto-grow enabled
  - PostgreSQL version 16
  - Backup retention: 7 days
  - Firewall configured for Azure services and development IP
- [x] Migrate database from local PostgreSQL to Azure
  - Exported schema (12 tables) using pg_dump
  - Imported schema to Azure PostgreSQL
  - Exported data using pg_dump with column inserts
  - Imported 2 customers, 2 suppliers successfully
  - Verified row counts match source database
- [x] Update App Configuration with Azure PostgreSQL FQDN (db:host = psql-customerorder-dev.postgres.database.azure.com)
- [x] Configure role assignments for Container App managed identity
  - **Implementation**: Automated via Bicep infrastructure-as-code
  - Module: `infra/modules/roleassignments.bicep`
  - Deployment: Integrated into `infra/main.bicep`
  - Three role assignments:
    * Key Vault Secrets User (Role ID: 4633458b-17de-408a-b874-1327992a3a45)
    * App Configuration Data Reader (Role ID: 516239f1-63e1-4d78-a4de-a74fb236a071)
    * AcrPull (Role ID: 7f951dda-4ed3-4680-a7ca-43fe172d538d)
  - Deployment script: `infra/deploy.ps1`
  - Documentation: `.vscode/transformation/TASK-006/automated-deployment-guide.md`
  - **Key Features**:
    - Idempotent using `guid()` for deterministic names
    - Conditional deployment (only when useContainerApps=true)
    - Dynamic principal ID from Container Apps module output
    - Resource-scoped (not resource group level)
    - CI/CD pipeline ready
  - Granted AcrPull role
- [x] Build and push Docker image to ACR
  - Tagged: customerorderdevacr.azurecr.io/customerorder-api:latest
  - Tagged: customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
  - Pushed both tags successfully
  - Image digest: sha256:a22277f65344aecb276304fa58381b85074f7b806ede02fa8fb61589a864b2b1
- [x] Update Container App with new image and environment variables
  - Image: customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
  - Configured all Azure service endpoints
  - Added database connection parameters
  - Configured OpenTelemetry for Application Insights
  - Application URL: https://ca-customerorder-dev.agreeableriver-e0e3f1d8.northeurope.azurecontainerapps.io
  - Status: Running (3 replicas configured, min 1)
  - Health check: UP (datasource connected to Azure PostgreSQL)
  - Startup time: 70 seconds
- [ ] Verify telemetry in Application Insights
- [ ] Test API endpoints with authentication token

### Phase 5: Documentation & Validation 📋 PENDING
- [ ] Final handoff documentation
- [ ] Deployment validation checklist

## Next Steps
1. ~~Update Dockerfile with OpenTelemetry Java agent~~ ✅ DONE
2. ~~Build and test Docker image locally~~ ✅ DONE
3. ~~Configure managed identity role assignments (Key Vault, App Configuration, ACR)~~ ✅ AUTOMATED IN BICEP
4. ~~Build and push Docker image to ACR~~ ✅ DONE
5. ~~Deploy and validate application in Azure Container Apps~~ ✅ DONE
6. Verify telemetry flowing to Application Insights
7. Complete Phase 5 documentation
