# Email Modernization (Java)

## Legacy Indicators

| Indicator | Issue | Replacement |
| --------- | ----- | ----------- |
| `Transport.send` direct calls | Blocking + tight coupling | Abstraction + ACS Email client |
| Inline SMTP credentials | Security exposure | Managed Identity / secret reference |
| JavaMail Session properties scattered | Configuration sprawl | Centralized config & adapter |

## Abstraction Interface

```java
public interface EmailPort {
  void send(String to, String subject, String htmlBody) throws MessagingException;
}
```

## Adapter Sketch

```java
public class AcsEmailPort implements EmailPort {
  private final EmailClient client; // placeholder conceptual class
  private final String sender;
  public AcsEmailPort(EmailClient client, String sender){ this.client = client; this.sender = sender; }
  public void send(String to, String subject, String htmlBody){
     // build and send ACS email request (pseudo)
  }
}
```

## Migration Steps

1. Identify JavaMail usage
2. Introduce EmailPort
3. Implement ACS adapter
4. Externalize sender + connection config
5. Remove inline credentials
6. Add logging & retry

## Testing

- Mock EmailPort
- Integration test with sandbox / test double

## Telemetry

| Event | Purpose |
| ----- | ------- |
| email_send_attempt | Track attempt volume |
| email_send_result | Success/failure breakdown |
| email_send_latency_ms | Performance monitoring |

## Pitfalls

| Issue | Cause | Mitigation |
| ----- | ----- | ---------- |
| Credential leakage | Hard-coded auth | Use identity / secret provider |
| Slow send path | Synchronous mail operations | Adopt async if supported |

## Next Steps

Proceed to Bandish spec: `../../../bandish/java/email.task.md`.
