# Secrets Externalization (Java)

## Common Patterns

| Pattern | Issue | Mitigation |
| ------- | ----- | ---------- |
| Hard-coded JDBC URL with creds | Credential exposure | Externalize + Key Vault / env ref |
| Plain password in properties | Rotations brittle | Key Vault + Spring Cloud Azure |
| Custom secret util class | Re-implementation risk | Use Spring config + vault integration |

## Spring Boot Integration

```yaml
azure:
  keyvault:
    enabled: true
    uri: ${KEY_VAULT_URI}
```

## Example Replacement

Inline:
```java
String apiKey = "ABC123"; // TODO rotate
```
Refactored:
```java
@Value("${secrets.thirdparty.apiKey}")
private String apiKey;
```

## Migration Steps

1. Inventory secrets (grep + regex)  
2. Define normalized secret names  
3. Load secrets into Key Vault  
4. Configure Spring Cloud Azure Key Vault property source  
5. Replace literals with property references  
6. Validate startup + runtime retrieval  

## Testing

- Context load test ensures property resolution  
- Rotation simulation: update secret; confirm reflection without restart (if configured)  

## Telemetry

| Event | Purpose |
| ----- | ------- |
| secret_externalized | Progress |
| secret_access_failure | Diagnostics |

## Pitfalls

| Issue | Cause | Solution |
| ----- | ----- | -------- |
| Missed secret | Pattern not matched | Manual review & heuristic expansion |
| Startup failure | Missing access policy | Assign managed identity role |

## Next Steps

Proceed to Bandish spec: `../../../bandish/java/secrets.task.md`.
