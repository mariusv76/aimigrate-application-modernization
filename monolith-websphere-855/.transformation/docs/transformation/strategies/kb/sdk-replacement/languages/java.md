# SDK Replacement (Java)

## Objectives

Replace legacy Track 1 Azure SDKs (com.microsoft.azure.*) or third‑party cloud SDKs with modern Track 2 (com.azure.*) libraries adopting builder pattern, Reactor-based async APIs, unified identity, and OpenTelemetry instrumentation.

## Common Legacy -> Modern Mappings

| Legacy Package / Type | Modern | Key Changes |
|-----------------------|--------|-------------|
| com.microsoft.azure.storage (CloudBlobClient) | com.azure:azure-storage-blob (BlobServiceClient) | Builder, Response&lt;T&gt;, async + sync, segmented listing |
| com.microsoft.azure.storage.queue | com.azure:azure-storage-queue | Visibility timeout & receipt semantics differ |
| com.microsoft.azure:azure-servicebus (old) | com.azure:azure-messaging-servicebus | Processor abstraction; settlement + auto lock renewal |
| AWS S3 SDK usage | azure-storage-blob | Path semantics & conditional ETags |

## Client Construction Pattern

```java
BlobServiceClient blobService = new BlobServiceClientBuilder()
    .endpoint(System.getenv("BLOB_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .retryOptions(new RequestRetryOptions(RetryMode.EXPONENTIAL, 5, null, null, null, null))
    .buildClient();
```

Async variant:
```java
BlobServiceAsyncClient asyncClient = new BlobServiceClientBuilder()
    .endpoint(endpoint)
    .credential(cred)
    .buildAsyncClient();
```

## Adapter Interface Example

```java
public interface ObjectStorage {
    Mono<Void> upload(String path, InputStream data, String contentType);
    Mono<byte[]> download(String path);
}
```

Implementation:
```java
public class BlobObjectStorage implements ObjectStorage {
  private final BlobContainerAsyncClient container;
  public BlobObjectStorage(BlobServiceAsyncClient svc) {
    this.container = svc.getBlobContainerAsyncClient("data");
  }
  public Mono<Void> upload(String path, InputStream data, String contentType) {
    return container.getBlobAsyncClient(path)
        .upload(FluxUtil.toFluxByteBuffer(data), new ParallelTransferOptions(),
            new BlobHttpHeaders().setContentType(contentType), null, null, null);
  }
  public Mono<byte[]> download(String path) {
    return container.getBlobAsyncClient(path)
        .downloadContent().map(r -> r.toBytes());
  }
}
```

## Pagination & Listing

Track 2 reactive listing:
```java
blobService.listBlobContainers().forEach(c -> System.out.println(c.getName()));
```
Async reactive variant:
```java
asyncClient.listBlobContainers().subscribe(c -> System.out.println(c.getName()));
```

## Observability

Add OpenTelemetry instrumentation: ensure global `OpenTelemetry` SDK registered; Azure SDK automatically creates spans when `AZURE_REQUEST_TRACING_ENABLED` (or standard instrumentation) is active. Configure exporters (OTLP / Azure Monitor) at application bootstrap.

## Dual Wiring (Strangler)

```java
if (featureManager.isEnabled("UseModernBlob")) {
  return modern.upload(path, data, contentType);
} else {
  return legacy.upload(path, data, contentType);
}
```

## Resilience Considerations

| Concern | Modern Approach |
|---------|----------------|
| Retries | Central `RequestRetryOptions` on builder |
| Timeouts | Per-operation via context / cancellation tokens (or Reactor `timeout`) |
| Backpressure | Reactor operators (limitRate / buffer) |
| Concurrency | Parallel transfer options for large uploads |

## Validation Checklist

- [ ] Legacy `com.microsoft.azure.storage` imports removed
- [ ] All new clients created via builder pattern
- [ ] DefaultAzureCredential used (no embedded keys)
- [ ] Reactive flows handle backpressure & dispose correctly
- [ ] Spans appear in tracing backend

## Rollback

Retain legacy dependency for one release; feature flag route determines which adapter implementation is invoked.
