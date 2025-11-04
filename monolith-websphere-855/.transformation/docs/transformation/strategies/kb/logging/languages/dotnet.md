# Logging & Observability (.NET)

## Objectives
Adopt structured logging, distributed tracing, and base metrics using Microsoft.Extensions.Logging + OpenTelemetry with export to Azure Monitor / Application Insights.

## Stack Components
| Concern | Library / Package | Notes |
|---------|-------------------|-------|
| Logger Abstraction | Microsoft.Extensions.Logging | DI injected generic `ILogger<T>` |
| Structured Enrichment | Serilog (optional) or built-in scopes | Use if advanced sinks needed |
| Tracing & Metrics | OpenTelemetry .NET (AspNetCore, HttpClient, SqlClient) | Add custom ActivitySource for domain spans |
| Export | Azure Monitor OpenTelemetry Distro OR OTLP exporter + Azure Monitor | Prefer distro for simplified config |

## Bootstrap (Minimal)
```csharp
builder.Services.AddOpenTelemetry()
    .WithTracing(t => t
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddSqlClientInstrumentation()
        .AddSource("Domain")
    )
    .WithMetrics(m => m
        .AddAspNetCoreInstrumentation()
        .AddRuntimeInstrumentation()
    );
```

## Configuration Guidelines
- Set `service.name`, `deployment.environment`, `service.version` via ResourceBuilder.
- Use appsettings / environment variables for sampling rate (e.g. `OTEL_TRACES_SAMPLER_ARG`).
- Prefer Managed Identity over instrumentation key where supported.

## Replacing Legacy Patterns
| Legacy Pattern | Replacement |
|----------------|------------|
| Static `Serilog.Log` | Inject `ILogger<T>` |
| `Trace.Write*` | Structured log with level + eventId |
| Manual correlation IDs | Rely on Activity.Current & middleware |

## Custom Spans
```csharp
using var activity = _activitySource.StartActivity("order.validate");
activity?.SetTag("order.id", order.Id);
```
Keep tags low cardinality; avoid raw user PII.

## Metrics (Example)
```csharp
static readonly Meter Meter = new("MyService", "1.0.0");
static readonly Counter<long> OrdersCreated = Meter.CreateCounter<long>("orders_created");
OrdersCreated.Add(1, new KeyValuePair<string, object?>("region", region));
```

## Validation Checklist
- [ ] All controllers/services use injected `ILogger<T>`.
- [ ] Activity traces visible in App Insights with correct service.name.
- [ ] No static logger usages remain.

## Rollback
Disable exporter registration; keep DI logger usage unchanged to minimize revert impact.

## Risks
| Risk | Mitigation |
|------|------------|
| Excess span volume | Tune sampling & limit custom spans |
| Sensitive tag leakage | Central tag allowlist + redaction |
| High log volume | Set log level defaults + category overrides |
