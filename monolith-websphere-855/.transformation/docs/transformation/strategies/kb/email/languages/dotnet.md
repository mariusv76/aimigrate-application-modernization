# Email Modernization (.NET)

## Legacy Patterns

| Pattern | Issue | Replacement |
| ------- | ----- | ----------- |
| `new SmtpClient()` | Obsolete API / limited resiliency | ACS EmailClient via Azure.Communication.Email |
| Inline credentials | Security risk | Managed Identity / Key Vault secret ref |
| Direct send in controllers | Tight coupling | Inject `IEmailSender` abstraction |

## Abstraction

```csharp
public interface IEmailSender {
  Task SendAsync(string to, string subject, string htmlBody, CancellationToken ct=default);
}
```

## Adapter Example

```csharp
public sealed class AcsEmailSender : IEmailSender {
  private readonly EmailClient _client;
  private readonly string _sender;
  public AcsEmailSender(EmailClient client, IConfiguration cfg) {
    _client = client; _sender = cfg["Email:Sender"]!; }
  public async Task SendAsync(string to, string subject, string htmlBody, CancellationToken ct=default) {
    var message = new EmailMessage(_sender, new EmailRecipients(new[]{ new EmailAddress(to)}), subject){
       Content = new EmailContent(subject){ Html = htmlBody }
    };
    await _client.SendAsync(Azure.WaitUntil.Completed, message, ct);
  }
}
```

## DI Setup

```csharp
services.AddSingleton<IEmailSender, AcsEmailSender>();
```

## Migration Steps

1. Introduce abstraction
2. Add ACS SDK & configuration
3. Replace direct SmtpClient usages
4. Remove stored credentials
5. Add retry / logging decorators

## Testing

- Fake IEmailSender for unit tests
- Integration test with sandbox or stub mode

## Telemetry

| Event | Purpose |
| ----- | ------- |
| email_send_attempt | Volume tracking |
| email_send_result | Success/failure |
| email_send_latency_ms | Performance |

## Pitfalls

| Issue | Cause | Mitigation |
| ----- | ----- | ---------- |
| Slow sends | Network / cold start | Warm-up + retry policy |
| Missing DKIM/SPF | Domain not verified | Verify sender domain early |

## Next Steps

Proceed to Bandish spec: `../../../bandish/dotnet/email.task.md`.
