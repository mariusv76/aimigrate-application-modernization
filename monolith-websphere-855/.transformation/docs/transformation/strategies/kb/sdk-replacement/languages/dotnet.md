# SDK Replacement (.NET)

## Objectives

Migrate legacy or third-party SDK usages to Azure .NET track 2 libraries with unified identity, resiliency, and telemetry.

## Common Legacy -> Modern Mappings

| Legacy Namespace / Type | Modern Azure SDK | Key Changes |
|-------------------------|------------------|-------------|
| Microsoft.WindowsAzure.Storage (CloudBlobClient) | Azure.Storage.Blobs (BlobServiceClient) | New client hierarchy; async-first; RequestConditions |
| Microsoft.WindowsAzure.Storage (CloudQueueClient) | Azure.Storage.Queues (QueueClient) | Base64 handling explicit; message TTL options |
| Microsoft.Azure.ServiceBus | Azure.Messaging.ServiceBus | Settlement model; Processor abstraction |
| WindowsAzure.Storage RetryPolicies | RetryOptions on client options | Exponential backoff centralized |

## Client Construction Pattern

```csharp
var blobService = new BlobServiceClient(new Uri(builder.Configuration["Storage:BlobEndpoint"]), new DefaultAzureCredential(),
    new BlobClientOptions { Retry = { Mode = RetryMode.Exponential, MaxRetries = 5, NetworkTimeout = TimeSpan.FromSeconds(60) } });
```

Centralize creation via a factory when multiple services share pipeline policy configuration.

## Dependency Injection Example

```csharp
builder.Services.AddSingleton(sp =>
{
    var cfg = sp.GetRequiredService<IConfiguration>();
    return new BlobServiceClient(new Uri(cfg["Storage:BlobEndpoint"]), new DefaultAzureCredential());
});

builder.Services.AddScoped<IObjectStorage, BlobStorageAdapter>();
```

## Adapter Example

```csharp
public interface IObjectStorage
{
    Task UploadAsync(string path, Stream content, string contentType, CancellationToken ct = default);
    Task<Stream> DownloadAsync(string path, CancellationToken ct = default);
}

public class BlobStorageAdapter : IObjectStorage
{
    private readonly BlobServiceClient _svc;
    public BlobStorageAdapter(BlobServiceClient svc) => _svc = svc;

    public async Task UploadAsync(string path, Stream content, string contentType, CancellationToken ct = default)
    {
        var client = _svc.GetBlobContainerClient("data").GetBlobClient(path);
        await client.UploadAsync(content, new BlobUploadOptions
        {
            HttpHeaders = new BlobHttpHeaders { ContentType = contentType }
        }, ct);
    }

    public async Task<Stream> DownloadAsync(string path, CancellationToken ct = default)
    {
        var client = _svc.GetBlobContainerClient("data").GetBlobClient(path);
        var resp = await client.DownloadStreamingAsync(cancellationToken: ct);
        return resp.Value.Content; // Caller owns disposal
    }
}
```

## Observability & Diagnostics

Enable distributed tracing + logging:

```csharp
AzureEventSourceListener.CreateConsoleLogger(); // Optional verbose
ActivitySource.AddActivityListener(new ActivityListener { /* configure sampling */ });
```

Leverage OpenTelemetry (if already configured) via `AddOpenTelemetry().WithMetrics().WithTracing(...)` and ensure Azure client instrumentation is enabled (`AzureMonitorTraceExporter` or OTLP exporter).

## Pagination Mapping

Legacy paged APIs often exposed continuation tokens implicitly. Modern clients use `AsyncPageable<T>`:

```csharp
await foreach (var blob in container.GetBlobsAsync())
{
    // process
}
```

## Dual-Wiring (Strangler) Sample

```csharp
if (flags.IsEnabled("UseModernBlob"))
    await modern.UploadAsync(key, stream, ct: ct);
else
    await legacy.UploadAsync(key, stream, ct: ct);
```

## Validation Checklist

- [ ] All legacy namespaces removed (`Microsoft.WindowsAzure.Storage` etc.)
- [ ] All new clients built with `DefaultAzureCredential`
- [ ] Retries standardized
- [ ] Telemetry spans visible in tracing backend
- [ ] Memory / stream disposal validated

## Rollback

Keep legacy package reference and adapter in a separate project file branch until post-release; feature flag toggles path.
