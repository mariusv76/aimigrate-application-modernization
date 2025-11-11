# TASK-006 Azure Integration TODOs

## Phase 1: Planning & Infrastructure ✅
- [x] Draft execution plan
- [x] Infra folder scaffold
- [x] Bicep module stubs (kv, appconfig, insights, acr, appservice, containerapps)
- [x] Add infra/README.md
- [x] Secrets strategy documented
- [x] Auth (Entra ID) strategy documented
- [x] Observability (OTel + App Insights) strategy documented
- [x] Deployment agent contract specification
- [x] Azure resources provisioned (KV, AppConfig, Insights, ACR, Container Apps Environment)
- [x] Bicep parameters file (dev) - North Europe region
- [x] Container Apps module created

## Phase 2: SDK Integration & Configuration ✅
- [x] Add Azure SDK dependencies to parent POM (azure-identity, azure-security-keyvault-secrets, azure-data-appconfiguration)
- [x] Implement KeyVaultConfigSource (MicroProfile ConfigSource)
- [x] Implement AppConfigurationConfigSource (MicroProfile ConfigSource)
- [x] Register ConfigSource implementations via META-INF/services
- [x] Test secret/config resolution locally with Azure CLI credentials
- [x] Store database password in Key Vault
- [x] Store configuration values in App Configuration (db host/port/name, telemetry sampling)

## Phase 3: Authentication & Authorization ✅
- [x] Create Entra ID App Registration (customerorder-api-dev)
- [x] Configure app roles (Orders.Read, Orders.Write)
- [x] Update microprofile-config.properties with actual Tenant ID and Client ID
- [ ] Migrate @RolesAllowed("SecureShopper") to @RolesAllowed("Orders.Read"/"Orders.Write")
- [ ] Test JWT validation with Entra ID tokens

## Phase 4: Observability & Deployment 🔄
- [ ] Update Dockerfile with OpenTelemetry Java agent download/copy
- [ ] Configure OTEL environment variables for App Insights export
- [ ] Role assignments (Managed Identity -> KV Secrets User, AppConfig Data Reader, AcrPull)
- [ ] Build and push Docker image to ACR
- [ ] Update Container App with new image
- [ ] Verify telemetry in Application Insights

## Phase 5: Documentation & Validation 📝
- [ ] Local fallback config documentation
- [ ] Deployment validation checklist
- [ ] Final handoff documentation with resource details
- [ ] Update progress.md with completion status
