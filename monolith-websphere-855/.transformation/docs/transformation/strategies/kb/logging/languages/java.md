# Logging & Observability (Java)

## Objectives
Unify logging, tracing, and metrics using SLF4J + OpenTelemetry Java SDK with export to Azure Monitor enabling correlation and structured analysis.

## Stack Components
| Concern | Library / Module | Notes |
|---------|------------------|-------|
| Logger API | SLF4J | Chosen façade; swap backend if needed |
| Backend | Logback (baseline) | Configure JSON encoder or pattern layout |
| Tracing | OpenTelemetry Java Agent / Manual SDK | Prefer auto-instrumentation agent + selective manual spans |
| Metrics | OpenTelemetry Metrics | Add custom instruments for domain KPIs |
| Export | OTLP → Azure Monitor / App Insights | Use distro or collector sidecar |

## Bootstrap (Manual Minimal)
```java
SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
    .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
    .setResource(Resource.getDefault().merge(Resource.create(Attributes.of(
        ResourceAttributes.SERVICE_NAME, "orders-service",
        ResourceAttributes.SERVICE_VERSION, "1.2.0"))))
    .build();
OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
```

## Auto Instrumentation
- Prefer Java agent (`-javaagent:opentelemetry-javaagent.jar`) for HTTP, JDBC, messaging, executor services.
- Use manual spans sparingly for domain boundaries (e.g., `order.validate`).

## Replacing Legacy Patterns
| Legacy Pattern | Replacement |
|----------------|------------|
| `System.out.println` | SLF4J logger at appropriate level |
| Log4j 1.x usage | SLF4J + Logback (or Log4j2 via bridge) |
| Manual thread correlation | Rely on context propagation from agent |

## Custom Span Example
```java
var tracer = openTelemetry.getTracer("domain");
var span = tracer.spanBuilder("order.validate").startSpan();
try {
  span.setAttribute("order.id", orderId);
  // validation logic
} finally {
  span.end();
}
```

## Metrics Example
```java
Meter meter = GlobalOpenTelemetry.meterBuilder("orders").build();
LongCounter created = meter.counterBuilder("orders.created").build();
created.add(1, Attributes.of(AttributeKey.stringKey("region"), region));
```

## Configuration Guidelines
- Externalize exporter endpoint / auth via env (OTEL_EXPORTER_OTLP_ENDPOINT).
- Set resource attributes for environment, version, deployment slot.
- Use sampler ratio tuned per environment.

## Validation Checklist
- [ ] No direct `System.out.print*` or Log4j1 calls remain.
- [ ] Spans present with correct service.name in App Insights.
- [ ] Log lines include correlation IDs (trace/span IDs).

## Rollback
Remove javaagent flag or disable exporter; retain SLF4J usage so instrumentation can be re-enabled without code changes.

## Risks
| Risk | Mitigation |
|------|------------|
| Excessive span cardinality | Limit dynamic attribute values |
| Large log volume | Adjust root + package logger levels |
| Agent version drift | Pin agent version & test upgrade separately |
