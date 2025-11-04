# Identity Migration (.NET)

## Objectives
Adopt Entra ID for authentication & authorization using Microsoft.Identity.Web / Azure.Identity, removing LDAP and custom token code paths.

## Key Libraries
| Area | Library | Notes |
|------|---------|-------|
| OIDC Web Apps / APIs | Microsoft.Identity.Web | Quick add for AddAuthentication + downstream API calls |
| Token Acquisition (daemon) | Azure.Identity (DefaultAzureCredential) | Prefer Managed Identity; fallback to ClientSecretCredential only locally |
| Graph / Downstream APIs | Microsoft Graph SDK / custom HttpClient w/ bearer injection | Use incremental consent if interactive |

## Detection Targets
| Legacy Pattern | Replace With |
|----------------|-------------|
| `DirectoryEntry` / `PrincipalContext` | ClaimsPrincipal from OIDC middleware |
| Custom token cache class | Built-in MSAL distributed cache (e.g., Redis) |
| Hard-coded service credential | Managed Identity or Key Vault stored cert |

## Configuration (Example)
```csharp
builder.Services.AddAuthentication(OpenIdConnectDefaults.AuthenticationScheme)
    .AddMicrosoftIdentityWebApp(builder.Configuration.GetSection("AzureAd"));

builder.Services.AddMicrosoftIdentityWebApiAuthentication(builder.Configuration, "AzureAd");
```

## Downstream API Token
```csharp
var credential = new DefaultAzureCredential();
var token = await credential.GetTokenAsync(new TokenRequestContext(new[]{"https://graph.microsoft.com/.default"}));
httpReq.Headers.Authorization = new("Bearer", token.Token);
```

## Claims Mapping
- Use `IClaimsTransformation` to map legacy roles → `role` / custom `app_role` claim.
- Keep mapping table versioned; log unresolved legacy groups.

## Authorization
```csharp
builder.Services.AddAuthorization(o =>
{
    o.AddPolicy("RequireBillingRole", p => p.RequireClaim("app_role", "billing"));
});
```
Annotate controllers/actions with `[Authorize(Policy = "RequireBillingRole")]`.

## Token Caching
- Use `AddDistributedMemoryCache` (dev) then Redis or SQL cache in production.
- Monitor cache hit ratio & acquisition latency.

## Telemetry
| Event | Data |
|-------|------|
| auth_success | flow, tenantId |
| auth_failure | flow, errorCode |
| token_acquire | resource, durationMs |

## Validation Checklist
- [ ] No `System.DirectoryServices` references remain.
- [ ] OIDC discovery endpoint reachable at startup.
- [ ] App roles appear in user principal claims.
- [ ] Managed Identity token acquisition succeeds in integration environment.

## Rollback
Disable new authentication schemes in config; re-enable legacy custom validator (feature flag). Keep new code paths to avoid rework.

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Mis-scoped permissions | Principle of least privilege review |
| Token cache misses | Configure distributed cache + warmup |
| Cross-tenant leakage | Enforce explicit tenant ID in validation options |
