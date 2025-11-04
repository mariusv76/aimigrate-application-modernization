# Storage Abstraction (Java)

## Goal
Detach domain logic from direct `java.io` / blocking file operations and enable Azure Blob async/reactive patterns.

## Legacy Indicators
| Indicator | Problem | Example |
| --------- | ------- | ------- |
| `FileInputStream` scattered | Hard to test & migrate | new FileInputStream(path) |
| Static utility with IO + logic | Mixed concerns | FileUtil.readConfigAndProcess() |
| Hard-coded path separators | Portability issues | path += "\\" + name |

## Abstraction Interface
```java
public interface StoragePort {
  InputStream read(String path) throws IOException;
  void write(String path, InputStream data, String contentType) throws IOException;
  boolean delete(String path) throws IOException;
  Stream<String> list(String prefix) throws IOException;
}
```

## Async/Reactive Consideration
If Reactor present, evolve to `Mono<InputStream>` / `Flux<ByteBuffer>` for streaming pipelines.

## Blob Adapter Sketch
```java
public class BlobStoragePort implements StoragePort {
   private final BlobContainerClient container;
   public BlobStoragePort(BlobContainerClient container) { this.container = container; }
   public InputStream read(String path) { return container.getBlobClient(path).openInputStream(); }
   public void write(String path, InputStream data, String contentType) {
       BlobClient client = container.getBlobClient(path);
       client.upload(data, true);
       client.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
   }
   public boolean delete(String path) { return container.getBlobClient(path).deleteIfExists(); }
   public Stream<String> list(String prefix) {
       return StreamSupport.stream(container.listBlobs(new ListBlobsOptions().setPrefix(prefix), null).spliterator(), false)
           .map(BlobItem::getName);
   }
}
```

## Spring Configuration
```java
@Bean
BlobContainerClient blobContainer(@Value("${storage.account-url}") String accountUrl,
                                  @Value("${storage.container}") String container) {
    BlobServiceClient svc = new BlobServiceClientBuilder().endpoint(accountUrl).buildClient();
    return svc.getBlobContainerClient(container);
}

@Bean
StoragePort storagePort(BlobContainerClient container) { return new BlobStoragePort(container); }
```

## Migration Steps
1. Introduce interface & adapter
2. Register Spring beans
3. Replace high-risk IO hotspots first
4. Remove legacy static utilities
5. Add performance instrumentation

## Testing Strategy
- Use local Azurite or testcontainer wrapper
- Contract tests verifying read-after-write & overwrite semantics

## Observability
| Signal | Rationale |
| ------ | --------- |
| blob.read.ms | Latency distribution |
| blob.write.bytes | Payload sizing |
| storage_abstraction_error | Failure causes |

## Pitfalls
| Issue | Cause | Mitigation |
| ----- | ----- | ---------- |
| Thread blocking | Using blocking IO in reactive pipeline | Use reactive SDK variant / bounded elastic scheduler |
| Large memory footprint | Full buffering of large files | Stream chunked |
| Encoding issues | Not specifying content-type | Pass MIME consistently |

## Next Steps
Proceed to Bandish spec: `../../../bandish/java/storage.task.md`.
