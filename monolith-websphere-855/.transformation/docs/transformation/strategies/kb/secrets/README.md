# Secrets Externalization Strategy [WIP]

## Purpose

Externalize plaintext secrets, credentials, and sensitive configuration values from code and static configuration files into Azure Key Vault with secure, identity-based retrieval.

## Applicability

- Plaintext secrets in `appsettings.json`, `application.yml`, `.properties`, or code
- Inline connection strings with embedded credentials
- Manual secret rotation pain points

## Detection Signals

| Signal | Description | Example |
| ------ | ----------- | ------- |
| Regex secret patterns | Keys/tokens inline | `ApiKey=ABC123` |
| JDBC URL with credentials | Username/password embedded | `jdbc:sqlserver://...;user=sa;password=P@ss` |
| Inline storage keys | AccountKey fragments | `AccountKey=` |

## Prerequisites

- Azure Key Vault provisioned
- Managed Identity or Service Principal with required access policies
- Secret naming convention agreed

## Transformation Flow

1. Scan for candidate secrets (regex + heuristics)
2. Classify & map to Key Vault names
3. Create entries in Key Vault (idempotent)
4. Replace inline values with references (configuration provider / environment binding)
5. Add identity-based credential retrieval
6. Validate application startup & secret access

## Validation

| Area | Check |
| ---- | ----- |
| Repository diff | No plain secrets remain |
| Runtime access | All secrets resolved successfully |
| Audit logging | Access logged for each secret retrieval |

## Rollback Plan

- Revert to previous config files (avoid unless emergency)
- Maintain temporary backup of replaced values in secure location

## Risks & Mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Secret drift | Establish rotation automation |
| Over-broad vault policies | Use least-privilege scoping |
| Performance latency | Cache non-rotating secrets appropriately |

## Telemetry Recommendations

| Event | Purpose |
| ----- | ------- |
| secret_externalized | Track migration progress |
| secret_access_failure | Alert on retrieval issues |

## Language Details

- [.NET details](languages/dotnet.md)
- [Java details](languages/java.md)

## Bandish Specs

- `../../bandish/dotnet/secrets.task.md`
- `../../bandish/java/secrets.task.md`

## References

- Azure Key Vault best practices
- Secret rotation policy
