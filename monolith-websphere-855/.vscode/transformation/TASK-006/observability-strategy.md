# TASK-006 Observability Strategy (OpenTelemetry + Application Insights)

## Goals
Provide distributed tracing, metrics, and logs correlation with minimal code changes, enabling performance analysis and reliability insights.

## Components
- OpenTelemetry Java Agent (preferred for breadth) OR manual instrumentation for hot paths.
- OTLP exporter -> Azure Monitor / Application Insights (via ingestion endpoint)
- Correlate request traces with JDBC spans and custom health/DB readiness spans.

## Data Types
| Type | Source | Transport | Destination |
|------|--------|-----------|-------------|
| Traces | Java agent auto-instrumentation | OTLP | App Insights |
| Metrics | JVM + custom (connection pool) | OTLP | App Insights |
| Logs | Liberty logs (separate) | (Future: OpenTelemetry log bridge) | Log Analytics |

## Java Agent Integration
Add startup JVM arg:
```
-javaagent:/opt/otel/otel-javaagent.jar
```
For App Service container, copy agent into image at `/opt/otel/` and reference via `JAVA_TOOL_OPTIONS` env var.

## Configuration (Env Vars)
| Variable | Purpose |
|----------|---------|
| OTEL_SERVICE_NAME | Logical service name (customerorder-api) |
| OTEL_EXPORTER_OTLP_ENDPOINT | OTLP collector or Application Insights proxy (if used) |
| APPLICATIONINSIGHTS_CONNECTION_STRING | Direct AI connection (fallback) |
| OTEL_RESOURCE_ATTRIBUTES | key=value list (env=dev,deployment=azure) |
| OTEL_METRICS_EXPORTER | `otlp` |
| OTEL_TRACES_SAMPLER | `parentbased_traceidratio` |
| OTEL_TRACES_SAMPLER_ARG | `0.1` (10% sampling initial) |

## Custom Metrics Plan
- DB Connection Pool: active, idle, max.
- Order Operations: count, latency histogram.
- Error Rate: counter of failed order submissions.

Expose via Micrometer-like wrapper or manual OpenTelemetry Meter API if agent not sufficient.

## Trace Enrichment
- Add attributes: tenant.id (if multi-tenant), order.id on order spans, db.system=postgres.
- Health check spans (internal) to flag degraded states.

## Sampling Strategy
Start 10% traces in dev; adjust using `OTEL_TRACES_SAMPLER_ARG` via App Configuration for dynamic update (future dynamic reload hook).

## Rollout Steps
1. Download fixed agent version (match latest stable OTel 2.x line).
2. Include agent in Docker image build stage.
3. Set env vars in infrastructure (Bicep outputs -> App Service config).
4. Deploy and verify traces appear (filter by service name).
5. Add custom metrics (phase 2).

## Future Enhancements
- Log correlation through OpenTelemetry log appender.
- Anomaly detection leveraging AI-based queries in Log Analytics.
- Synthetic transaction probes.

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Excess telemetry cost | Sampling + metric cardinality control |
| Agent incompatibility | Pin agent version, test locally |
| Sensitive data leakage | Attribute allow/deny list, avoid PII tags |

## Next Actions
- Select agent version and add to Docker build stub.
- Add env variable placeholders to Bicep App Service module.
- Implement minimal custom meter for DB pool after base integration.
