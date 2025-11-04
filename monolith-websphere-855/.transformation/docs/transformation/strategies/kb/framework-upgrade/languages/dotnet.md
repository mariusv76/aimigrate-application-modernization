# Framework Upgrade (.NET)

## Objectives
Upgrade legacy .NET Framework (4.x) or early .NET Core applications to .NET 8 (LTS) using a staged, test-first approach minimizing behavioral drift.

## Target Baseline
| Area | Target |
|------|--------|
| Runtime | .NET 8 (LTS) |
| Nullable | Enabled (treat warnings as build issues in later phase) |
| Implicit Usings | Enabled |
| AOT / Trimming | Evaluated post-upgrade (optional) |

## Pre-Upgrade Checklist
- All tests green on legacy branch.
- Dependency audit: list packages, versions, transitive counts.
- Confirm build agents & CI support .NET 8 SDK.

## Project File Conversion
| Legacy Aspect | Action |
|---------------|--------|
| packages.config | Migrate to `<PackageReference>` (nuget migrate) |
| old csproj schema | Convert to SDK style (`<Project Sdk="Microsoft.NET.Sdk">`) |
| AssemblyInfo.cs | Move attributes into project file or keep if customization required |

## Multi-Target Strategy (Optional)
Add interim multi-target:
```xml
<TargetFrameworks>net48;netstandard2.0</TargetFrameworks>
```
Refactor incompatible APIs under `#if NET48` guards; keep surface stable.

## Final Target Bump
```xml
<TargetFramework>net8.0</TargetFramework>
```
Remove legacy conditional blocks; delete obsolete binding redirects / config.

## Analyzer & Code Quality
Add analyzers early:
```xml
<ItemGroup>
  <PackageReference Include="Microsoft.CodeAnalysis.NetAnalyzers" Version="*" PrivateAssets="All" />
</ItemGroup>
```
Enable nullable:
```xml
<Nullable>enable</Nullable>
```
Treat warnings as errors later:
```xml
<TreatWarningsAsErrors>true</TreatWarningsAsErrors>
```

## Common API Remediation
| Legacy API | Replacement |
|------------|------------|
| BinaryFormatter | System.Text.Json / custom serializer |
| AppDomain evidence / shadow copy | Hosting abstractions / AssemblyLoadContext |
| WCF client | gRPC / REST client (Refit / HttpClient) |
| Remoting | gRPC / named pipes |

## Configuration & Hosting
Migrate Web.config settings to appsettings.json / builder configuration. Replace Global.asax with Program.cs + minimal hosting model.

## Logging & Telemetry
Adopt unified logging (ILogger) and OpenTelemetry instrumentation as part of upgrade to leverage modern diagnostics.

## Testing Focus Areas
| Area | Consideration |
|------|--------------|
| Serialization | Differences in casing / defaults |
| Threading | Task vs sync context shifts |
| Time / Culture | Invariant globalization differences |

## Performance Validation
Collect baseline metrics (throughput, P95 latency, memory) prior to switch; compare after deployment; tune GC or thread pool if regression > agreed threshold.

## Rollback
Retain last stable deployment package for .NET Framework; feature flag runtime-specific behavior; maintain branch until stability confirmed.

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Hidden dependency on GAC | Identify via assembly binding logs prior to cut |
| Silent serialization change | Golden file snapshot tests |
| Excess nullable noise | Stage enabling: annotate hot paths first |
