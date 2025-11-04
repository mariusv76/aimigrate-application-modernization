# Storage Abstraction Strategy [WIP]

## Purpose

Guidance for refactoring direct file or object storage access to Azure Blob Storage through an explicit abstraction to improve portability, testability, resiliency, and cloud alignment.

## Applicability

- Direct use of `System.IO` / `java.io` APIs inside business logic
- Embedded S3 or legacy storage SDK calls interwoven with domain logic
- Need to enable multi-cloud or local test harnesses

## Detection Signals

| Signal | Description | Example |
| ------ | ----------- | ------- |
| Direct file API usage | Calls to File/FileStream/Files.* in services/controllers | `File.ReadAllText(path)` |
| Hard-coded paths | Absolute or network share references | `C:\data\inbound` |
| Inline container/bucket names | Literal strings for container selection | `"products-container"` |
| Mixed concerns | Logic + IO + retry in same method | Large method with try/catch + File IO |

## Prerequisites

- Azure Blob Storage account (or emulator for tests)
- Identity: Managed Identity or Service Principal
- Access policy / IAM for required containers

## Transformation Flow

1. Inventory direct IO usage (grep & static scan)
2. Define abstraction interface (`IFileStorage` / `StoragePort`)
3. Implement Azure Blob adapter (async, streaming)
4. Externalize container + connection via configuration
5. Register adapter in DI / Spring context
6. Replace call sites incrementally (strangler approach)
7. Add resilience (retry, circuit breaker if policy library present)
8. Add observability (latency metrics, bytes transferred, failures)

## Validation

| Area | Check |
| ---- | ----- |
| Compilation | No remaining direct IO usages in business assemblies/packages |
| Unit Tests | Abstraction mocked / faked easily |
| Performance | No significant regression under load test (streaming vs. buffered) |
| Resilience | Retry policy kicks on transient 5xx / throttling |
| Security | No leaked connection strings or SAS keys in code |

## Rollback Plan

- Keep original code paths behind feature flag for one deployment cycle
- Revert DI binding to previous direct implementation if severe regression
- Retain old utilities until traffic / usage metrics confirm stability

## Risks & Mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Over-abstraction | Keep interface minimal: read, write, delete, list |
| Large refactor blast radius | Strangler pattern—wrap first, then migrate hotspots |
| Performance regression | Use streaming APIs and measure early |
| Credential misconfiguration | Prefer Managed Identity over connection strings |

## Telemetry Recommendations

| Event | Purpose |
| ----- | ------- |
| storage_abstraction_call | Measure adoption + latency |
| storage_abstraction_error | Capture failure mode & retry count |
| storage_abstraction_migrated_file | Count migrated call sites |

## Related Language Docs

- [.NET details](languages/dotnet.md)
- [Java details](languages/java.md)

## Bandish Specs

For executable automation see:

- `../../bandish/dotnet/storage.task.md`
- `../../bandish/java/storage.task.md`

## References

- Azure Blob Storage SDK
- Resilience patterns (retry, circuit breaker)
- Clean architecture port-adapter guidance
