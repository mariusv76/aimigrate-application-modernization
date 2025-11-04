# Events Modernization (.NET)

## Objectives
Replace AWS CloudWatch/EventBridge publishing with Azure Event Grid using the `EventGridPublisherClient`, enforcing normalized event types & subjects.

## Key Libraries
| Concern | Package | Notes |
|---------|---------|-------|
| Event Publishing | Azure.Messaging.EventGrid | Track 2 SDK |
| Auth | Azure.Identity (DefaultAzureCredential) | Supports Managed Identity / local dev chain |
| Serialization | System.Text.Json | Consistent payload casing / schema |

## Publisher Setup
```csharp
using Azure;
using Azure.Identity;
using Azure.Messaging.EventGrid;

var endpoint = new Uri(builder.Configuration["EventGrid:TopicEndpoint"]);
var client = new EventGridPublisherClient(endpoint, new DefaultAzureCredential());
```

## Publish Helper
```csharp
public record OrderCreated(string OrderId, decimal Total);

public async Task PublishOrderCreated(OrderCreated evt)
{
    var egEvent = new EventGridEvent(
        subject: $"orders/{evt.OrderId}",
        eventType: "orders.created.v1",
        dataVersion: "1.0",
        data: BinaryData.FromObjectAsJson(evt)
    );
    await _client.SendEventAsync(egEvent);
}
```

## Batch Publish
```csharp
await _client.SendEventsAsync(new[]{ egEvent1, egEvent2 });
```

## Replacing CloudWatch Patterns
| Legacy | Replacement |
|--------|-------------|
| `AmazonCloudWatchEventsClient.PutEventsAsync` | `EventGridPublisherClient.SendEventAsync` |
| Custom retry wrapper | Built-in retry (configure via client options) |
| DetailType string inline | Versioned eventType constant |

## Retry & Resilience
Configure `EventGridPublisherClientOptions` for retry policy; wrap with Polly if extended backoff needed for sustained outages.

## Telemetry
- Log publish attempt & duration.
- Counter: `event_publish_fail_total` with tags (type, reason).
- Activity/Span: `events.publish` with attributes (eventType, size, retryCount).

## Validation Checklist
- [ ] All CloudWatch publish code removed.
- [ ] Events visible in intended subscriptions.
- [ ] Dead-letter tested by forcing failure (disabled endpoint).

## Rollback
Feature flag the new publisher; toggle back to legacy path (temporary dual-write optional).

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Incorrect subject format | Unit tests for subject builder |
| Large payload size | Compress or move blob reference |
| Duplicate events | Idempotent consumers with event ID cache |
