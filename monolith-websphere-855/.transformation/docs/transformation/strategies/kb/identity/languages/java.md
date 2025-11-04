# Identity Migration (Java)

## Objectives
Transition from JAAS / LDAP / custom token validation to Entra ID using Spring Security OIDC & OAuth2 client, removing manual credential storage and proprietary session tokens.

## Key Dependencies
| Area | Library | Notes |
|------|---------|-------|
| OIDC Resource Server | spring-boot-starter-oauth2-resource-server | Validates JWT access tokens |
| OIDC Client (web) | spring-boot-starter-oauth2-client | For interactive flows |
| Token Acquisition (daemon) | azure-identity (ManagedIdentityCredential, ClientSecretCredential) | Prefer Managed Identity |
| Graph / Downstream APIs | msgraph-sdk / RestTemplate/WebClient w/ token | Use on-behalf-of where needed |

## Detection Targets
| Legacy Pattern | Replace With |
|----------------|-------------|
| `InitialDirContext` / direct LDAP | OIDC JWT validation (issuer/audience enforced) |
| Custom session token filter | Spring Security filter chain + JwtAuthenticationProvider |
| Manual credential caching | azure-identity credential chain |

## Configuration (application.yml)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/<tenant>/v2.0
      client:
        registration:
          web-app:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET:}
            scope: openid,profile,api://resource/.default
```

## Custom Claims Mapping
Implement a JwtGrantedAuthoritiesConverter to map roles / groups:
```java
converter.setAuthorityPrefix("");
converter.setAuthoritiesClaimName("roles");
```
Augment with a custom converter if legacy group-to-role translation required.

## Outbound Token (Managed Identity)
```java
var credential = new ManagedIdentityCredentialBuilder().build();
var token = credential.getToken(new TokenRequestContext().addScopes("https://graph.microsoft.com/.default"));
webClient.get().uri("/me")
  .header("Authorization", "Bearer " + token.getToken())
  .retrieve();
```

## Authorization
Use method or URL authorization:
```java
http.authorizeHttpRequests(auth -> auth
  .requestMatchers("/admin/**").hasAuthority("billing")
  .anyRequest().authenticated());
```

## Token Caching
- Rely on azure-identity internal caching + optional external caching for high-frequency daemon apps.
- Monitor token acquisition latency via custom metrics.

## Telemetry
| Event | Data |
|-------|------|
| auth_success | flow, issuer |
| auth_failure | flow, error | 
| token_acquire | scopeCount, durationMs |

## Validation Checklist
- [ ] No direct `InitialDirContext` or JAAS `LoginContext` usage remains.
- [ ] JWT tokens validated (invalid signature test blocked).
- [ ] Role mapping present in SecurityContext authorities.
- [ ] Managed Identity token acquisition works in deployed env.

## Rollback
Disable Spring Security OIDC config; re-enable legacy filter chain behind profile/flag temporarily.

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Incorrect issuer config | Automated startup health check fetch JWKS |
| Excessive token requests | Ensure reuse of WebClient + default caching |
| Group claim size issues | Use app roles / custom roles claim |
