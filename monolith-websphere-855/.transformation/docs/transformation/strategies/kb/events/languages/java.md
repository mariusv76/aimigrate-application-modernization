# Events Modernization (Java)

## Objectives
Replace AWS EventBridge / CloudWatch event publishing with Azure Event Grid using the `EventGridPublisherClient` from the Java SDK and normalized event contracts.

## Key Libraries
| Concern | Library | Notes |
|---------|---------|-------|
| Event Publishing | com.azure:azure-messaging-eventgrid | Track 2 Azure SDK |
| Auth | com.azure:azure-identity | DefaultAzureCredential for Managed Identity |
| JSON | jackson-databind | Schema consistency /

## Publisher Setup
```java
EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
    .endpoint(System.getenv("EVENT_GRID_TOPIC_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildEventGridEventPublisherClient();
```

## Publish Helper
```java
public record OrderCreated(String orderId, BigDecimal total) {}

public void publishOrderCreated(OrderCreated evt) {
    EventGridEvent egEvent = new EventGridEvent(
        "orders/" + evt.orderId(),
        "orders.created.v1",
        evt,
        "1.0"
    );
    client.sendEvent(egEvent);
}
```

## Batch Publish
```java
client.sendEvents(List.of(event1, event2));
```

## Replacing CloudWatch Patterns
| Legacy | Replacement |
|--------|-------------|
| AWS SDK `PutEventsRequest` | `EventGridPublisherClient.sendEvent(s)` |
| DetailType inline string | Versioned constant `orders.created.v1` |
| Manual retry loop | SDK retry + optional resilience layer |

## Retry & Resilience
- Configure underlying HTTP client retry; add backoff wrapper for prolonged outages.
- Consider idempotency via event `id` or payload hash in consumer state.

## Telemetry
- Counter: `event_publish_fail_total{type}`.
- Histogram: `event_publish_latency_ms`.
- Span: `events.publish` (attributes: eventType, size, attemptCount).

## Validation Checklist
- [ ] No AWS EventBridge publish code remains.
- [ ] Events arrive with normalized eventType and subject in subscriptions.
- [ ] Dead-letter path processes failed event scenario.

## Rollback
Toggle feature flag to re-enable legacy AWS publisher branch (optional dual-write during transition period).

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Subject construction errors | Unit tests for subject builder logic |
| Payload drift vs schema | Contract tests + CI schema validation |
| Burst publish throttling | Batch sends + monitor throttling metrics |
