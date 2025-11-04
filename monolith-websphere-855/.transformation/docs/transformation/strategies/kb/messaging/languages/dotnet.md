# Messaging Modernization (.NET)

## Abstractions
```csharp
public interface IMessageBus
{
    Task PublishAsync<T>(string topic, T message, CancellationToken ct = default);
}

public interface IMessageProcessor
{
    Task StartAsync(CancellationToken ct = default);
}
```

## Adapter Implementation (Service Bus)
```csharp
public sealed class ServiceBusMessageBus : IMessageBus
{
    private readonly ServiceBusSender _sender;
    public ServiceBusMessageBus(ServiceBusClient client, string topic)
        => _sender = client.CreateSender(topic);

    public Task PublishAsync<T>(string topic, T message, CancellationToken ct = default)
    {
        var body = BinaryData.FromObjectAsJson(message);
        return _sender.SendMessageAsync(new ServiceBusMessage(body), ct);
    }
}
```

## Processor Sample
```csharp
public sealed class OrdersProcessor : BackgroundService
{
    private readonly ServiceBusProcessor _processor;
    public OrdersProcessor(ServiceBusClient client)
    {
        _processor = client.CreateProcessor("orders", new ServiceBusProcessorOptions
        {
            MaxConcurrentCalls = 8,
            AutoCompleteMessages = false
        });
        _processor.ProcessMessageAsync += HandleAsync;
        _processor.ProcessErrorAsync += args => Task.CompletedTask;
    }

    protected override Task ExecuteAsync(CancellationToken stoppingToken) => _processor.StartProcessingAsync(stoppingToken);

    private async Task HandleAsync(ProcessMessageEventArgs args)
    {
        try
        {
            var order = args.Message.Body.ToObjectFromJson<Order>();
            // process
            await args.CompleteMessageAsync(args.Message);
        }
        catch (Exception)
        {
            await args.AbandonMessageAsync(args.Message);
        }
    }
}
```

## Configuration & DI
```csharp
services.AddAzureClients(builder =>
{
    builder.AddServiceBusClient(Configuration["ServiceBus:Namespace"]) // sb://... endpoint
           .WithCredential(new DefaultAzureCredential());
});
services.AddSingleton<IMessageBus>(sp =>
{
    var client = sp.GetRequiredService<ServiceBusClient>();
    return new ServiceBusMessageBus(client, "orders");
});
```

## Telemetry
- Add ActivitySource("Messaging") spans around publish & process.
- Tag: message.contractVersion, message.id, deliveryCount.

## Idempotency Pattern
```csharp
public interface IProcessedStore { Task<bool> AddIfAbsentAsync(string id); }
```
