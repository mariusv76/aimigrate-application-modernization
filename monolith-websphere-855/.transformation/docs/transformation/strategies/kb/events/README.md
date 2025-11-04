# Events Modernization (CloudWatch / EventBridge → Event Grid) [WIP]

## Purpose

Migrate from AWS CloudWatch Events / EventBridge constructs to Azure Event Grid to enable native Azure routing, consistent event contracts, and integration with downstream subscribers (Functions, Service Bus, WebHooks).

## Problems Addressed

- Proprietary AWS event envelope & rule constructs inside code.
- Custom pollers or lambda triggers not portable to Azure.
- Inconsistent event type naming & lack of versioned subjects.
- Missing replay / dead-letter strategy in legacy flow.

## Goals

| Goal | Description | Success Indicator |
|------|-------------|-------------------|
| Unified Event Contract | Standardize event metadata (id, source, type, subject, time, data) | All published events validate against schema contract |
| Reliable Delivery | Use retries + DLQ / storage fallback | Zero unhandled publish exceptions in validation window |
| Observability | Emit metrics/traces around publish latency & failure counts | Dashboard shows P95 publish latency target met |
| Extensibility | Support new consumers without code changes | Adding subscription requires no publisher modification |

## Non-Goals

- Redesign of full domain event taxonomy (only normalization & forward compatibility).
- Building a complex event sourcing store.

## Architecture Overview

1. Event producers publish to an Event Grid custom topic or domain.
2. Event Grid routes events to configured subscriptions (Functions, WebHooks, queues).
3. Dead-letter or fallback endpoint captures poison events.
4. Observability pipeline records publish attempts, failures, latency.

## Mapping CloudWatch/EventBridge → Event Grid

| CloudWatch / EventBridge Field | Event Grid Field | Notes |
|--------------------------------|------------------|-------|
| `detail-type` | `eventType` | Normalize to kebab/namespace form (e.g. `order.created.v1`) |
| `source` | `topic` / part of `subject` | Optionally map to subject prefix (e.g. `aws/orders`) |
| `detail` | `data` | Preserve payload; translate keys if renaming required |
| `id` | `id` | Use original or generate UUID if absent |
| `time` | `eventTime` | Ensure RFC3339 format |
| (Rule ARN context) | `subject` | Include path-style subject with version segment |

## Migration Phases

| Phase | Description | Output |
|-------|-------------|--------|
| 1 Discovery | Catalog existing rules, event patterns, targets | Source-to-target inventory |
| 2 Contract Design | Define normalized event types & subject schema | Event contract matrix |
| 3 Publisher Abstraction | Implement Event Grid publish client wrapper | Reusable publisher service |
| 4 Replacement | Swap CloudWatch/EventBridge publish/invoke logic | Updated code paths |
| 5 Reliability | Add retry, idempotency keys, DLQ route | Hardened delivery pipeline |
| 6 Validation | Replay sample set & verify consumer behavior | Validation report |

## Event Naming Guidelines

- Use reverse domain or bounded-context prefix: `orders.created.v1`.
- Increment version suffix only on breaking payload changes.
- Subject segments: `<context>/<entity>/<id>` or `<context>/<aggregate>` when no id.

## Reliability & Retry

| Concern | Approach |
|---------|----------|
| Transient failure | Exponential backoff (3–5 attempts) |
| Persistent failure | Dead-letter endpoint (Storage / Service Bus queue) |
| Idempotency | Include deterministic `id`; consumers treat duplicate as no-op |

## Telemetry Model

| Signal | Example | Purpose |
|--------|---------|---------|
| Metric | event_publish_latency_ms | Performance tracking |
| Counter | event_publish_fail_total | Alerting baseline |
| Log Event | event_publish_failure {type, reason} | Debug root cause |
| Span | events.publish (attrs: eventType, size, retryCount) | Distributed tracing |

## Security & Compliance

- Use Managed Identity for Event Grid publish operations.
- Validate outbound payloads against schema before publish.
- Avoid embedding secrets in event data; reference secure resource identifiers instead.

## Validation Checklist

- [ ] All legacy CloudWatch/EventBridge publish paths removed.
- [ ] Event Grid topic receives events with normalized eventType & subject.
- [ ] Dead-letter path tested with forced failure.
- [ ] Replay sample events consumed successfully by subscribers.

## Rollback Strategy

- Temporarily dual-publish to both CloudWatch/EventBridge and Event Grid until confidence; revert by disabling Event Grid publisher feature flag.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Incorrect event type normalization | Consumer mismatch | Maintain mapping table & unit tests |
| Missing replay support | Data loss during cutover | Export & replay historical sample set |
| Latency spikes under burst | Delayed consumers | Batch publish & monitor throttling |
| Schema drift | Downstream breakage | Versioned event types + contract tests |

## References

- Azure Event Grid documentation
- Organization event contract standards

## Next Steps (Per Language)

See `languages/dotnet.md` and `languages/java.md` for implementation details.
