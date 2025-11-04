# Identity Migration Strategy [WIP]

## Purpose

Migrate legacy/on-prem identity mechanisms (LDAP, custom token services, legacy AD auth helpers) to Entra ID (Azure AD) using modern OAuth2/OIDC, MSAL / Azure Identity libraries, and Managed Identity to eliminate static credentials.

## Problems Addressed

- Direct LDAP binds and password verification logic in application code.
- Hard‑coded service account credentials / secrets.
- Custom session or proprietary token formats lacking standardized claims.
- Manual token caching and refresh flows.
- Inconsistent role/group to application authorization mapping.

## Goals

| Goal | Description | Success Indicator |
|------|-------------|-------------------|
| Centralized Auth | Delegate identity to Entra ID | No direct LDAP binds in code paths |
| Modern Protocols | Use OAuth2/OIDC standards | ID & access tokens issued by Entra |
| Principle of Least Privilege | Remove embedded credentials | Managed Identity & app registration secrets rotated |
| Claims-Based Authorization | Map roles/groups to claims | Authorization decisions rely on claims principal |
| Confidential Data Protection | Eliminate plain-text passwords | No credential findings in scans |

## Non-Goals

- Building a custom identity provider.
- SSO experience design (handled by platform configuration).

## High-Level Architecture

1. Application trusts Entra ID (OIDC / JWT validation) for inbound user tokens.
2. Outbound calls acquire tokens via MSAL (confidential/daemon) or Managed Identity.
3. Claims transformation layer maps legacy roles/groups to standardized app roles.
4. Policy-based or attribute-based authorization enforces access control.
5. Secrets replaced with Managed Identity, certificate, or federated credential.

## Migration Phases

| Phase | Description | Output |
|-------|-------------|--------|
| 1 Discovery | Inventory auth & LDAP usage, credential storage | Auth inventory report |
| 2 Design | Map legacy groups → app roles/claims | Role mapping matrix |
| 3 Enablement | Add OIDC/JWT validation + token acquisition libs | Auth bootstrap code |
| 4 Refactor | Remove LDAP & custom token code | Simplified auth layer |
| 5 Hardening | Add cache, retry, rotation & telemetry | Hardened identity config |
| 6 Validation | Functional & security tests | Test reports |

## Telemetry & Observability

| Signal | Example | Purpose |
|--------|---------|---------|
| Auth Success Count | auth_success_total | Baseline success rate |
| Auth Failure Count | auth_failure_total (code) | Detect spikes / misconfig |
| Token Acquisition Latency | token_acquire_ms | Performance tuning |
| Managed Identity Fallback | mi_fallback_invocations | Catch unexpected secret path usages |

## Security Considerations

- Prefer Managed Identity over client secrets; if certs required, store in Key Vault.
- Enforce token audience (aud) & issuer (iss) validation.
- Validate nonce and state for interactive flows.
- Shorten lifetime / add continuous access evaluation where feasible.

## Validation Checklist

- [ ] No LDAP bind or DirectoryServices calls executed in integration tests.
- [ ] All tokens validated by framework middleware/component.
- [ ] Roles/permissions resolved exclusively from claims principal.
- [ ] Static credentials removed from repository & configuration.

## Rollback Strategy

- Re-enable legacy auth provider behind feature flag while retaining new modules for rapid reattempt.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Incorrect role mapping | Authorization regressions | Dual-run audit of legacy vs new claims |
| Token acquisition latency | Request delays | Cache + proactive refresh |
| Secret fallback usage | Security exposure | Telemetry alert on secret path usage |
| Multi-tenant misconfig | Unauthorized access | Enforce tenant ID & issuer checks |

## References

- MSAL / Azure.Identity documentation
- Entra ID application roles & group claims
- Internal RBAC guidelines

## Next Steps (Per Language)

See `languages/dotnet.md` and `languages/java.md` for implementation details.
