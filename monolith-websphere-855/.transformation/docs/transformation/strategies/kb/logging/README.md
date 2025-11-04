# Logging & Observability Strategy [WIP]

## Purpose

Establish a unified, language-agnostic approach to structured logging, distributed tracing, and metrics using OpenTelemetry, flowing into Azure Monitor / Application Insights, enabling correlation across services and reliable production diagnostics.

## Problems Addressed

- Inconsistent log schemas and ad‑hoc string formatting.
- Missing or partial distributed tracing (no end-to-end correlation IDs).
- Static/global loggers limiting testability.
- Manual correlation / custom middleware duplication.
- Lack of standardized telemetry naming and resource attributes.

## Goals

| Goal | Description | Success Indicator |
|------|-------------|-------------------|
| Structured Logs | Enforce schema (timestamp, level, trace/span ids, service.name, event, message) | 100% sampled logs include trace/span IDs |
| Distributed Tracing | Propagate W3C trace context (traceparent/tracestate) | Spans visible in dependency map across services |
| Metrics Foundation | Provide base process/runtime and custom domain metrics | Baseline + custom meters exported to backend |
| Correlation | Enable log ⇄ trace ⇄ metric pivot | App Insights shows linked logs/spans |
| Vendor Neutrality | Abstraction via OpenTelemetry SDKs | Swap/augment exporter without code churn |

## Non-Goals

- Full APM feature parity (profilers, heap snapshot tooling).
- Automatic remediation or alert configuration (handled separately).

## Architecture Overview

Core layers:

1. Application / Domain emits structured logs via DI logger facade.
2. OpenTelemetry SDK instruments framework + selected libraries.
3. Resource attributes applied (service.name, deployment.environment, version).
4. Export pipeline: OTLP (preferred) → Azure Monitor (direct exporter or Azure Monitor OpenTelemetry Distro).
5. Optional enrichment: add user/session claims (PII-safe), feature flag identifiers.

## Key Design Principles

- Prefer dependency-injected logger abstractions over static singletons.
- Enforce minimal, consistent event field set; extend only with reviewed keys.
- Keep logs value-centric (avoid embedding stack traces except at ERROR/FATAL with sampling).
- Avoid logging secrets or high-cardinality identifiers unredacted.
- Normalize asynchronous context propagation (executors, thread pools, tasks).

## Migration Phases

| Phase | Description | Artifacts |
|-------|-------------|-----------|
| 1 Discovery | Inventory logging & tracing calls, classify patterns | Scan report |
| 2 Foundation | Add OpenTelemetry SDK + resource & exporter config | Base telemetry bootstrap |
| 3 Refactor | Replace static/global logging with DI + structured fields | Updated code paths |
| 4 Correlation | Add middleware/filters for context propagation | Propagation layer |
| 5 Enhancement | Add custom spans, semantic attributes, metrics | Extended instrumentation |
| 6 Hardening | Tune sampling, error log volume, retention | Config updates |

## Telemetry Model

| Category | Examples | Notes |
|----------|----------|-------|
| Logs | service event, validation failure | Log schema enforced via formatter/enricher |
| Spans | http.server, messaging.publish, db.query | Use semantic conventions |
| Metrics | request_duration_ms, active_connections | Histograms / counters / gauges |

## Sampling Strategy

- Start with parent-based TraceIdRatio (10–20%) for lower environments; adjust production based on volume.
- Always sample ERROR/FATAL spans.
- Keep logs unsampled but throttle noisy categories.

## Correlation & Context

- Inject & extract W3C tracecontext headers.
- Propagate baggage only for essential cross-service keys (e.g., tenant, featureFlagSet).
- Capture trace/span ids automatically; do not manually thread IDs except in edge cases.

## Security & Compliance

- Redact potential secrets via configured processor/enricher.
- Exclude PII fields unless explicitly approved (classification doc link placeholder).
- Ensure role-based access to telemetry backend (no broad contributor rights for viewers).

## Validation Checklist

- [ ] Log lines contain trace/span IDs.
- [ ] Spans appear with correct service.name and environment tags.
- [ ] Error logs rate within agreed budget.
- [ ] No plaintext secrets detected by scanning tool.

## Rollback Strategy

- Disable OpenTelemetry exporter; fall back to baseline framework logging (maintain DI logger usage to avoid code churn).
- Retain resource & instrumentation setup for fast re-enable.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Excess log volume | Cost / signal dilution | Level filtering, sampling, retention tuning |
| Missing propagation in async | Trace breaks | Executor / middleware instrumentation |
| High-cardinality labels | Backend cardinality blow-up | Curate metric dimensions |
| Sensitive data leakage | Compliance breach | Redaction + scanning gates |

## References

- OpenTelemetry Specification
- Azure Monitor / Application Insights OpenTelemetry Distro
- Organization Log Schema Catalog (internal)

## Next Steps (Per Language)

See `languages/dotnet.md` and `languages/java.md` for implementation specifics.
