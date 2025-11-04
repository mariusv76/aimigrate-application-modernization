# Secrets Externalization (.NET)

## Common Patterns

| Pattern | Issue | Mitigation |
| ------- | ----- | ---------- |
| Inline connection string | Credential exposure | Key Vault + config provider |
| Embedded API key constants | Rotation blocked | Externalize & reference by name |
| Custom secret loader class | Reinvented wheel | Replace with Key Vault provider |

## Key Vault Integration

```csharp
builder.Configuration.AddAzureKeyVault(new Uri(vaultUri), new DefaultAzureCredential());
```

## Example Replacement

Inline:

```csharp
var apiKey = "ABC123"; // TODO rotate

```

Refactored:

```csharp
var apiKey = configuration["Secrets:ThirdParty:ApiKey"];
```

## Migration Steps

1. Enumerate secrets (regex + manual review)  
2. Define vault names (consistent prefix)  
3. Import into Key Vault (script / automation)  
4. Wire Key Vault provider  
5. Replace literals with configuration lookups  
6. Remove plaintext from git history (filter-repo optional)  

## Testing

- Startup test: ensure all required keys resolve  
- Integration: simulate secret rotation (update value, confirm reload)  

## Telemetry

| Event | Purpose |
| ----- | ------- |
| secret_externalized | Progress tracking |
| secret_access_failure | Alerting |

## Pitfalls

| Issue | Cause | Solution |
| ----- | ----- | -------- |
| Performance hit | Excessive per-call retrieval | Cache stable secrets |
| Missed secret | Regex false negative | Manual code review pass |

## Next Steps

Proceed to Bandish spec: `../../../bandish/dotnet/secrets.task.md`.
