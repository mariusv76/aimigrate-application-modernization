# TASK-006 Azure Integration Execution Plan

## 1. Scope & Objectives
Integrate Customer Order Services application with core Azure platform services to achieve secure secret management, centralized configuration, identity-based authentication, and end-to-end observability.

Target Azure Services:
- Azure Container Registry (ACR) for image storage
- Azure App Service (Linux) or Azure Container Apps (evaluation) for runtime
- Azure Key Vault for secrets (DB password, OAuth client secret)
- Azure App Configuration for externalized config values
- Azure Monitor / Application Insights + OpenTelemetry for traces/metrics/logs
- Microsoft Entra ID (App Registration + roles) for authentication/authorization
- (Future consideration) Azure PostgreSQL Flexible Server (managed database)

Non-Goals (Phase 1): Autoscaling optimization, advanced chaos testing, cost management policies.

## 2. Architectural Overview
Containerized Open Liberty application deployed to Azure using managed identity. Application reads secrets at runtime from Key Vault and dynamic configuration from App Configuration. Telemetry exported via OpenTelemetry SDK to Application Insights. Entra ID protects API endpoints via OAuth2 JWT Bearer tokens, role-based access enforced in business service layer.

## 3. Resource Inventory (Initial)
| Resource | Naming Convention | Notes |
|----------|-------------------|-------|
| Resource Group | rg-customerorder-dev | Logical grouping |
| Key Vault | kv-customerorder-dev | Purge protection ON |
| App Config | appconfig-customerorder | Standard SKU |
| App Insights | customerorder-insights | East US region |
| Container Registry | acrcustomerorderdev | Geo-redundancy optional |
| PostgreSQL | pgflex-customerorder-dev | Later phase (currently local) |
| Entra App Registration | customerorder-api-dev | Exposes API roles |
| Managed Identity | system-assigned (runtime) | Grants Key Vault/App Config access |

## 4. Identity & Security Strategy
- Use system-assigned managed identity from runtime (App Service/Container Apps) for Key Vault/App Configuration access.
- App Registration with roles: Orders.Read, Orders.Write.
- Principle of least privilege: Key Vault access policy grants get/list secrets only.
- No hardcoded credentials; database password stored as secret `db-password`.
- Future: rotate secrets via Key Vault + short TTL for client secret.

## 5. Configuration Externalization
| Config Item | Location | Key | Refresh |
|-------------|----------|-----|---------|
| DB host/port/name | App Configuration | db:host, db:port, db:name | Manual (feature flag later) |
| Feature toggles | App Configuration | feature:... | Dynamic |
| Telemetry sampling | App Configuration | telemetry:sampling | Dynamic |

Mapping environment variables -> Spring/Liberty bootstrap via bootstrap.properties or MicroProfile Config with custom source reading App Configuration provider.

## 6. Secrets Management
| Secret | Key Vault Name | Consumption |
|--------|----------------|------------|
| DB password | db-password | MicroProfile Config (custom Key Vault source) |
| OAuth client secret | entra-client-secret | Startup credential for token validation if needed |

Access via managed identity using Azure SDK (Java) KeyVaultSecretClient; integrate with Config API.

## 7. Observability Plan
- OpenTelemetry Java agent or SDK instrumentation for HTTP server, JDBC, JAX-RS.
- Export traces/metrics to Application Insights using OTLP exporter.
- Health endpoints already implemented (/health). Add /metrics for Prometheus format if needed.
- Log correlation using trace/span IDs.

## 8. Deployment Strategy
Phased:
1. Container build (Docker) → push to ACR.
2. Deploy to Azure App Service (Linux, container) initial.
3. Evaluate Azure Container Apps for dynamic scale + Dapr (future).
4. Introduce Bicep templates under infra/ for reproducible RG + resources.

## 9. IaC Structure (Bicep)
```
infra/
  main.bicep        # orchestrates modules
  modules/
    rg.bicep
    acr.bicep
    keyvault.bicep
    appconfig.bicep
    insights.bicep
    appservice.bicep
    roleassign.bicep
```

Parameters: location, env, baseName, enableContainerApps (bool).

## 10. Access Control & RBAC
- Key Vault: access policy or RBAC (prefer RBAC) granting Managed Identity secret get/list.
- App Configuration: data reader role for Managed Identity.
- ACR: AcrPull role for App Service Managed Identity.

## 11. Risk & Mitigation
| Risk | Impact | Mitigation |
|------|--------|-----------|
| Secret resolution latency | Config startup delay | Cache secrets in memory with short TTL |
| Misconfigured roles | Auth failures | Validate via Azure Portal/CLI + unit tests |
| Telemetry volume cost | Increased spend | Sampling configuration, 10% trace sample initially |
| Network egress restrictions | Failed resource access | Use private endpoints (future) |

## 12. Validation Checklist
- Container runs locally with env var fallbacks.
- Key Vault secret fetched successfully (mock in dev if no Azure).
- App Configuration value overrides default.
- App registration token accepted; role mapping enforced.
- Traces visible in Application Insights.

## 13. Implementation Sequencing
1. Create branch + progress tracking files.
2. Add infra skeleton + README.
3. Add Dockerfile adjustments for production (multi-stage).
4. Implement Key Vault + App Config integration layer (Java provider / MicroProfile Config). Initially stub.
5. Add Entra ID security filter (consider migration from EJB roles to MP JWT or custom).
6. Add OpenTelemetry exporter configuration.
7. Validate locally (simulated) then provision Azure resources.
8. Bind managed identity permissions.
9. Deploy container, test end-to-end.
10. Documentation and summary.

## 14. Open Questions
- Use App Service vs Container Apps first? (Default: App Service for simplicity.)
- Use OpenTelemetry Java agent vs manual instrumentation? (Start with agent for broad coverage.)
- Replace EJB security annotations fully or maintain hybrid? (Plan full migration.)

## 15. Next Actions (Pending Confirmation)
- Create progress tracker and todos files.
- Scaffold infra folder.
- Prepare Bicep module stubs.
- Proceed after approval.

---
Generated: 2025-11-11
Branch: agent-test
Status: Draft Plan
