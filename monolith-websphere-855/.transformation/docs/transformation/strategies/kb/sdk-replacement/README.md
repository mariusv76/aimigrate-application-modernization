# SDK Replacement Strategy [WIP]

## Purpose

Modernize legacy or non-Azure SDK dependencies to current Azure SDK (track 2) libraries or approved open-source alternatives while preserving functional behavior and improving reliability, observability, and security.

## Problems Addressed

- Deprecated or unsupported legacy SDKs increasing security & compliance risk.
- Inconsistent or missing distributed tracing / metrics across client calls.
- Embedded connection strings or static credentials inside configuration and code.
- Divergent retry / timeout behavior causing uneven resilience posture.
- Leaky abstractions: business logic coupled directly to vendor-specific models.

## Goals

| Goal | Description | KPI |
|------|-------------|-----|
| Security & Support | Eliminate deprecated / unmaintained libs | 0 high CVEs; all deps in support window |
| Observability | Standardize tracing / metrics | 100% client ops emit OpenTelemetry spans |
| Identity Alignment | Remove embedded keys | 0 connection strings committed; managed identity default |
| Resilience | Unified retry + timeout policy | Consistent transient success rate > target |
| Consistency | Single abstraction per capability | One adapter interface per domain |

## When to Replace vs Wrap

| Scenario | Action | Rationale |
|----------|--------|-----------|
| Legacy SDK superseded by track 2 Azure SDK | Replace | New API surface + perf + diagnostics |
| Third-party vendor feature gap still needed | Wrap (Adapter) | Keep surface stable; internal migration later |
| Multiple callers tightly coupled to legacy types | Introduce Anti-Corruption Layer | Isolate churn; incremental refactor |
| Broad surface area but only subset needed | Facade | Minimize public API exposed |
| High risk big-bang change | Strangler (dual-path) | Parallel run + cutover metric-driven |

## Migration Phases

1. Discovery: inventory dependencies & transitive graph
2. Mapping: build replacement table (legacy -> target)
3. Abstraction: define or confirm domain interfaces (e.g. IBlobStore / StorageClient)
4. Dual Wiring (optional): instantiate both legacy & new behind feature flag
5. Refactor Call Sites: replace usages incrementally (per interface / per feature)
6. Observability + Resilience: add standardized pipeline (retry, logging, tracing)
7. Remove Legacy: delete old adapter + dependency entries
8. Verify & Harden: load test & failure injection

## Pattern Catalog

| Pattern | Use | Notes |
|---------|-----|-------|
| Adapter | Map old API to new client | Thin translation; keep semantics |
| Anti-Corruption Layer | Shield domain from 3rd party model | Convert DTOs inward/outward |
| Facade | Simplify broad client surface | Hide complexity & vendor lock |
| Strangler | Incremental replacement | Route by feature/percentage |
| Compatibility Shim | Provide interim types | Temporary; scheduled removal |

## Inventory & Mapping (Example)

| Legacy | Use | Replacement | Notes |
|--------|-----|------------|-------|
| Microsoft.WindowsAzure.Storage (CloudBlobClient) | Blob storage ops | Azure.Storage.Blobs (BlobServiceClient) | Flat -> hierarchical path semantics |
| com.microsoft.azure.storage | Blob storage (track1) | com.azure:azure-storage-blob | Builder pattern + Response&lt;T&gt; wrappers |
| AWS SDK SQS | Queue messaging | Azure.Messaging.ServiceBus | Settlement semantics differ |
| Custom HttpClient JSON wrapper | REST calls | Azure SDK specific client | Leverage pipeline policies |

## Telemetry & Measurement

- Before: capture baseline latency, error rates per dependency
- After: verify parity or improvement; instrument with OpenTelemetry spans named `Azure.<Service>.<Operation>`
- Track adoption progress (% call sites migrated) via simple static analysis / grep on legacy namespace imports

## Validation Criteria

- All replaced namespaces removed from code (0 references)
- All new clients constructed with DefaultAzureCredential (except explicit test cases)
- Retry policy standardized (e.g. exponential backoff) across all new clients
- Functional & regression tests green (no behavioral drift)
- Performance delta within agreed SLO thresholds (or improved)

## Rollback Strategy

Maintain legacy package version in lock file until final removal branch merges. Keep adapter code for one release window; feature flag can revert traffic to legacy path if critical defect emerges.

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Hidden semantic differences (e.g. pagination, timeouts) | Write contract tests vs abstraction |
| Performance regression | Benchmark pre/post; tune concurrency & buffering |
| Partial migration inconsistency | Enforce adapter usage rule in code review |
| Credential scope changes | Centralize credential factory; integration tests |

## Checklist

- [ ] Dependency inventory exported
- [ ] Mapping table approved
- [ ] Abstractions defined / updated
- [ ] Dual wiring (if needed) implemented
- [ ] Call sites migrated
- [ ] Observability added
- [ ] Legacy deps removed
- [ ] Rollback path documented

## Next

Proceed to language-specific implementation guides in `languages/`.
