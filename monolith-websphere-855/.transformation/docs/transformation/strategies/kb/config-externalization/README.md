# Configuration Externalization Strategy [WIP]

## Purpose

Centralize application configuration (non-secret settings, feature flags, endpoint lists) and separate secret material using Azure App Configuration + Key Vault so changes propagate without redeploying code.

## Problems Addressed

- Divergent per-environment config files and drift.
- Hard-coded literals & magic strings in source.
- Inability to toggle features or dynamic routing without deployment.
- Embedded secrets within general configuration artifacts.

## Goals

| Goal | Description | Success Indicator |
|------|-------------|-------------------|
| Central Source of Truth | Single managed store for config | All services load baseline from App Configuration |
| Secrets Isolation | Secrets only in Key Vault | No secrets in repo or App Config non-keyvault values |
| Dynamic Refresh | Safe runtime reload of mutable keys | Refresh latency < 30s (target) |
| Feature Management | Controlled progressive rollout | Feature flag logs show staged exposure |
| Governance & Audit | Track config changes centrally | Audit log coverage 100% of mutations |

## Non-Goals

- Full policy/approval workflow (handled by governance tooling).
- Replacing build-time code generation (separate concern).

## Configuration Taxonomy

| Category | Examples | Location | Refresh | Notes |
|----------|----------|----------|---------|-------|
| Static Non-Sensitive | Service name, region list | App Configuration | Rare | Versioned keys |
| Dynamic Runtime | Endpoint weights, timeouts | App Configuration | Frequent | Use sentinel key |
| Feature Flags | canaryFeature, ui.newNav | App Configuration (Feature Mgmt) | On change | Targeted rollout rules |
| Secrets | Conn strings, API keys | Key Vault | On rotation | Key Vault ref in App Config |

## Migration Phases

| Phase | Description | Output |
|-------|-------------|--------|
| 1 Inventory | Collect keys, classify secret vs non-secret | Inventory spreadsheet |
| 2 Modeling | Define naming conventions & namespaces | Naming guideline doc |
| 3 Provisioning | Create keys & secret references | Populated stores |
| 4 Integration | Add providers / refresh & feature libs | Bootstrapped app config |
| 5 Cutover | Remove local duplicates; enable sentinel | Clean config commit |
| 6 Optimization | Add caching, trim unused keys | Reduced footprint |

## Naming Conventions

`<app>:<domain>:<feature>:<setting>` (kebab or colon separated). Include version segment for contract-sensitive values: e.g., `orders:pricing:v2:multiplier`.

## Refresh & Sentinel Pattern

Use a dedicated sentinel key (e.g., `__configSentinel`) to force reload cycles—update sentinel last in a batch to trigger clients.

## Feature Flag Strategy

- Store flags under dedicated namespace `FeatureManagement`.
- Apply ring-based rollout (internal → pilot → general) with percentage or targeting rules.
- Telemetry: log flag evaluation (flag, variation, user context) at debug level with sampling.

## Security & Compliance

- Key Vault references for secrets (never inline secret values in App Configuration).
- RBAC restrict write operations; separate config writers from general readers.
- Enable content integrity checks / scanning for accidental secret leakage.

## Telemetry & Observability

| Signal | Example | Purpose |
|--------|---------|---------|
| Config Reload Event | config_reload {changedKeys} | Verify refresh functioning |
| Feature Flag Evaluation | feature_eval {flag, variant} | Rollout audit |
| Stale Config Warning | config_stale {ageSeconds} | Detect refresh failures |

## Validation Checklist

- [ ] All previously hard-coded keys now externalized.
- [ ] Secrets retrieved exclusively via Key Vault references.
- [ ] Refresh sentinel update triggers client reload.
- [ ] Feature flag toggling changes application behavior without redeploy.

## Rollback Strategy

Re-enable local configuration files (retain external provider disabled via feature flag) while investigating external store issues; keep external structure intact for quick restore.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Over-frequent reloads | Performance overhead | Debounce refresh & batch sentinel changes |
| Secret in App Config by mistake | Exposure risk | Automated scanner + CI failing build |
| Naming collision | Ambiguous lookups | Prefix with bounded context & environment |
| Flag evaluation latency | Slow requests | Cache evaluation results per request scope |

## References

- Azure App Configuration documentation
- Key Vault references format
- Feature Management library usage guide

## Next Steps (Per Language)

See `languages/dotnet.md` and `languages/java.md`.
