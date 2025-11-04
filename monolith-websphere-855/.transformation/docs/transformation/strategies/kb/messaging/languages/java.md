# Messaging Modernization (Java)

## Abstractions
```java
public interface MessageBus {
    <T> void publish(String topic, T message);
}

public interface MessageProcessor {
    void start();
}
```

## Adapter (Service Bus)
```java
@Component
public class ServiceBusMessageBus implements MessageBus {
    private final ServiceBusSenderClient sender;
    public ServiceBusMessageBus(ServiceBusClientBuilder builder) {
        this.sender = builder.buildClient().createSender("orders");
    }
    public <T> void publish(String topic, T message) {
        ServiceBusMessage sb = new ServiceBusMessage(Json.toBytes(message));
        sender.sendMessage(sb);
    }
}
```

## Processor Sample
```java
@Component
public class OrdersProcessor {
    private final ServiceBusProcessorClient processor;

    public OrdersProcessor(ServiceBusClientBuilder builder) {
        processor = builder
            .processor()
            .topicName("orders")
            .subscriptionName("orders.sub")
            .processMessage(ctx -> {
                byte[] bytes = ctx.getMessage().getBody().toBytes();
                // deserialize & process
                ctx.complete();
            })
            .processError(err -> {})
            .buildProcessorClient();
    }

    @PostConstruct
    public void start() { processor.start(); }
}
```

## Spring Configuration
```java
@Bean
public ServiceBusClientBuilder sbBuilder() {
    return new ServiceBusClientBuilder()
        .credential("<fully-qualified-namespace>", new DefaultAzureCredential());
}
```

## Telemetry
- Wrap publish/consume in OpenTelemetry spans.
- Attributes: entity, contract.version, message.size.

## Idempotency
```java
public interface ProcessedStore { boolean addIfAbsent(String id); }
```
