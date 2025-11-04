# Messaging Strategy: SQS to Service Bus [WIP]

## Purpose

Modernize message-based integration by migrating from Amazon SQS or legacy queue systems to Azure Service Bus (ASB) to leverage topics, sessions, duplicate detection, and enterprise-grade observability.

## Applicability

Use when:

- Current system relies on SQS standard queues, on-prem MSMQ, Rabbit routing complexity, or custom polling loops.
- Need ordering (sessions), filtering (subscriptions with SQL rules), or dead lettering with forwarding.
- Desire richer DLQ handling and automated poison message isolation.

Avoid / Defer when:

- Throughput > ASB tier limits without partition plan.
- Ultra-low latency (< 3ms) required (consider in-memory bus + compact protocol).

## Target Outcomes

| Dimension | Goal |
|----------|------|
| Reliability | At-least-once with idempotent processing |
| Ordering | Session-aware partitioned ordering |
| Latency | P95 under 500ms (processing inclusive) |
| Observability | Correlated tracing + DLQ metrics |
| Security | Managed Identity for send/receive |

## Migration Phases

1. Inventory queues & message contracts.
2. Design topic/queue topology (topics + subscriptions vs queues).
3. Create ASB namespace, auth (Managed Identity bindings), resource provisioning.
4. Implement adapter layer abstracting enqueue/dequeue.
5. Introduce dual-write (shadow publish) + passive consumer.
6. Switch consumers to ASB (cutover), then disable legacy.
7. Enable duplicate detection and auto-forward DLQ routing.

## Architecture

```plaintext
[Producer] -> IQueuePublisher -> (Abstraction) -> ServiceBusTopic/Queue
                                      |-> LegacySqsClient (Phase 1-5 shadow)
[Consumer] -> IQueueProcessor -> (Abstraction) -> ServiceBusProcessor Client
```

## Anti-Patterns

- Embedding raw ServiceBus client usage across services.
- Using sessions when strict ordering not required (cost/throughput tradeoff).
- Ignoring DLQ; leaving poison messages untriaged.

## Key Design Decisions

| Area | Decision |
|------|----------|
| Abstraction | Introduce `IMessageBus` / `MessageBusPort` |
| Topology Versioning | Include major version in entity names `orders.v1` |
| Contracts | Use schema-registry or JSON with explicit version field |
| Retry | SDK exponential retry + poison isolation after N attempts |
| Idempotency | MessageId + store (Redis / table) for processed markers |

## Detection Signals

- Direct instantiation of SQS clients or custom polling loops.
- Hard-coded queue URLs or names.
- Lack of correlation IDs in logs for message handlers.

## Success Criteria

- Zero message loss during cutover (shadow diff logs empty).
- All consumers using abstraction layer only.
- DLQ monitored and drained within agreed SLA.
- Traces show end-to-end span continuity.

## Telemetry & Observability

- Publish span: attributes (entity, size, contractVersion).
- Consume span: processing duration, delivery count.
- DLQ metric: backlog count + age percentile.

## Rollout Plan

| Phase | Environment Action |
|-------|---------------------|
| Dev | Implement abstraction + dual publish |
| Test | Shadow consume & contract validation logs |
| Staging | Load test + failure injection (throttle, transient) |
| Prod | Gradual traffic shift via feature flag |

## Validation

- Load test matches or improves baseline throughput.
- Message duplication < defined acceptable threshold (e.g., 0.01%).
- All handlers resilient to retries (idempotent side-effects).

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Mis-sized namespace | Start standard tier; monitor and autoscale plan |
| Poison loops | Circuit breaker after repeated handler failure |
| Contract drift | Schema or versioned DTO diff CI check |
| Latency spikes | Pre-warm clients + tune prefetch |

## Backward Compatibility

Maintain legacy queue writer until post-cutover verification passes (shadow diff window ≥ 24h clean).

## References

- Azure Service Bus Docs
- Messaging patterns (Idempotent Receiver, Competing Consumers)
