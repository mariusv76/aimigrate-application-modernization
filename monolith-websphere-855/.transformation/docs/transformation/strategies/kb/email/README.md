# Email Modernization Strategy [WIP]

## Purpose

Modernize legacy SMTP (System.Net.Mail / JavaMail) message sending to Azure Communication Services (ACS) Email with secure identity, configuration externalization, abstraction, and observability.

## Applicability

- Direct use of `SmtpClient`, `MailMessage`
- JavaMail `Transport.send` usage
- Hard-coded SMTP host/port/credentials
- Need for secure sending & centralized templates

## Detection Signals

| Signal | Description | Example |
| ------ | ----------- | ------- |
| SMTP client construction | Legacy API use | `new SmtpClient(host)` |
| Inline credentials | Hard-coded username/password | `client.Credentials = new NetworkCredential("user","pw")` |
| JavaMail Session props | SMTP property map | `props.put("mail.smtp.host", ...)` |
| Direct send calls | Tight coupling | `Transport.send(mimeMessage)` |

## Prerequisites

- ACS Email resource provisioned
- Managed Identity or Azure AD app registration
- Sender domain verified

## Transformation Flow

1. Inventory SMTP/JavaMail usage
2. Introduce abstraction (`IEmailSender` / `EmailPort`)
3. Implement ACS Email adapter (async send)
4. Externalize sender, domain, connection configuration
5. Remove plaintext SMTP credentials
6. Add retry + telemetry (send duration, status)
7. Add structured logging (exclude PII)

## Validation

| Area | Check |
| ---- | ----- |
| Unit Tests | Abstraction mockable |
| Security | No credentials in code or config |
| Functional | Test email stub or sandbox send succeeds |
| Observability | Telemetry events emitted |

## Rollback Plan

- Rebind abstraction to legacy SMTP adapter
- Retain old config (flagged) for one deployment

## Risks & Mitigations

| Risk | Mitigation |
| ---- | ---------- |
| Credential sprawl | Managed Identity / Key Vault references |
| Template divergence | Centralize templates in storage or ACS |
| Latency increase | Async batching / retry policy |

## Telemetry Recommendations

| Event | Purpose |
| ----- | ------- |
| email_send_attempt | Tracking volume |
| email_send_result | Success/failure stats |
| email_send_latency_ms | Performance envelope |

## Language Details

- [.NET details](languages/dotnet.md)
- [Java details](languages/java.md)

## Bandish Specs

- `../../bandish/dotnet/email.task.md`
- `../../bandish/java/email.task.md`

## References

- ACS Email SDK docs
- Secure configuration guidelines
- Retry / resilience standards
