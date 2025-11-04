# Configuration Externalization (.NET)

## Objectives

Adopt Azure App Configuration + Key Vault providers with strongly-typed options & dynamic refresh to replace hard-coded lookups and large appsettings.* sprawl.

## Key Packages

| Concern | Package | Notes |
|---------|---------|-------|
| App Configuration | Azure.Data.AppConfiguration / Microsoft.Extensions.Configuration.AzureAppConfiguration | Provider + feature flags |
| Key Vault References | (Within App Config) | Indirect secret retrieval |
| Options Binding | Microsoft.Extensions.Options | Strong typing + validation |
| Feature Flags | Microsoft.FeatureManagement | Flag evaluation + filters |

## Provider Registration (Minimal)

```csharp
builder.Configuration.AddAzureAppConfiguration(options =>
{
    options.Connect(new Uri(builder.Configuration["AppConfig:Endpoint"]), new DefaultAzureCredential())
           .ConfigureRefresh(r => r.Register("__configSentinel", refreshAll: true).SetCacheExpiration(TimeSpan.FromSeconds(30)))
           .UseFeatureFlags(f => f.CacheExpirationInterval = TimeSpan.FromSeconds(15));
});

builder.Services.AddAzureAppConfiguration();
```

## App Pipeline Hook

```csharp
var app = builder.Build();
app.UseAzureAppConfiguration(); // Enables dynamic refresh & feature flags
```

## Strongly Typed Options

```csharp
public class CacheSettings { public int TtlSeconds { get; set; } }
builder.Services.AddOptions<CacheSettings>()
    .Bind(builder.Configuration.GetSection("cache"))
    .ValidateDataAnnotations()
    .Validate(o => o.TtlSeconds > 0, "TTL must be positive");
```

## Feature Flags Usage

```csharp
builder.Services.AddFeatureManagement();

public class BetaController : ControllerBase
{
  private readonly IFeatureManager _fm;
  public BetaController(IFeatureManager fm) => _fm = fm;
  [HttpGet("/beta")] public async Task<IActionResult> Get() =>
      await _fm.IsEnabledAsync("NewBetaFlow") ? Ok("beta") : NotFound();
}
```

## Key Vault Secret Reference Pattern

In App Configuration value: `@Microsoft.KeyVault(SecretUri=https://<vault>.vault.azure.net/secrets/DbPassword/<version>)`

No code change required; provider resolves secret at runtime.

## Refresh Sentinel

- Update regular keys first.
- Update `__configSentinel` last to broadcast change.

## Validation Checklist

- [ ] No direct `ConfigurationManager.AppSettings` lookups remain.
- [ ] All secrets accessed via Key Vault references.
- [ ] Feature flag toggling changes behavior without restart.
- [ ] Options validation fails fast on invalid config.

## Rollback

Disable `AddAzureAppConfiguration` code path (feature flag) and reinstate local file use; retain external keys for quick re-enable.

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Over-refresh churn | Tune cache expiration; consolidate updates |
| Secret exposure in logs | Filter config providers; redact on serialization |
| Flag explosion | Namespace & lifecycle policies |
