# Storage Abstraction (.NET)

## Goal
Decouple business logic from `System.IO` and enable cloud-native Blob integration with resilience and observability.

## Common Legacy Patterns
| Pattern | Issue | Replacement |
| ------- | ----- | ----------- |
| `File.ReadAllText` in service | Blocking & tightly coupled | `await fileStorage.ReadAsync(path)` |
| Hard-coded path constants | Environment inflexibility | IConfiguration + logical keys |
| Manual retry loops | Inconsistent resilience | Polly / SDK retry config |

## Recommended Interface
```csharp
public interface IFileStorage {
    Task<Stream> ReadAsync(string path, CancellationToken ct=default);
    Task WriteAsync(string path, Stream content, string? contentType=null, CancellationToken ct=default);
    Task DeleteAsync(string path, CancellationToken ct=default);
    IAsyncEnumerable<string> ListAsync(string prefix, CancellationToken ct=default);
}
```

## Blob Adapter Skeleton
```csharp
public sealed class BlobFileStorage : IFileStorage {
  private readonly BlobContainerClient _container;
  public BlobFileStorage(BlobContainerClient container) => _container = container;
  public async Task<Stream> ReadAsync(string path, CancellationToken ct=default) =>
      await _container.GetBlobClient(path).OpenReadAsync(cancellationToken: ct);
  public async Task WriteAsync(string path, Stream content, string? contentType=null, CancellationToken ct=default) {
      var client = _container.GetBlobClient(path);
      await client.UploadAsync(content, new BlobUploadOptions { HttpHeaders = new BlobHttpHeaders { ContentType = contentType ?? "application/octet-stream" } }, ct);
  }
  public Task DeleteAsync(string path, CancellationToken ct=default) =>
      _container.GetBlobClient(path).DeleteIfExistsAsync(cancellationToken: ct);
  public async IAsyncEnumerable<string> ListAsync(string prefix, [EnumeratorCancellation] CancellationToken ct=default) {
      await foreach (var item in _container.GetBlobsAsync(prefix: prefix, cancellationToken: ct))
          yield return item.Name;
  }
}
```

## DI Registration
```csharp
services.AddAzureClients(b => {
  b.AddBlobServiceClient(new Uri(config["Storage:BlobServiceUri"]));
});
services.AddSingleton<IFileStorage>(sp => {
  var blob = sp.GetRequiredService<BlobServiceClient>();
  var container = blob.GetBlobContainerClient(config["Storage:Container"]);
  return new BlobFileStorage(container);
});
```

## Migration Tactics
1. Add interface + adapter
2. Register in DI (feature flag optional)
3. Wrap existing IO calls (adapter usage) leaving old path reachable
4. Replace call sites incrementally
5. Remove legacy direct calls + tests

## Testing
- Use in-memory stream implementation or local emulator (Azurite)
- Contract tests to ensure consistent semantics (read-after-write, overwrite behavior)

## Observability
- Log size / duration metrics
- Emit custom event `storage_abstraction_call`

## Pitfalls
| Issue | Cause | Mitigation |
| ----- | ----- | ---------- |
| Memory spikes | Reading full file to memory | Stream end-to-end |
| Latency | Synchronous wrappers | Ensure async all the way |
| Credentials failure | Missing Managed Identity role | Grant Storage Blob Data Contributor |

## Next Steps
Proceed to Bandish spec: `../../../bandish/dotnet/storage.task.md`.
