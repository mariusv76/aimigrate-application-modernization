# Transformation Agent Prerequisites: Modernizing from .NET 8 (LTS) to .NET 9 (STS)

> Status: Draft (Keep aligned with official Microsoft release notes & breaking change docs). Update this file whenever new .NET 9 servicing releases, SDK patches, or breaking changes are published.

## 1. Purpose & Scope

This document defines the minimum prerequisites the Transformation Agent expects before attempting an automated modernization of an application currently targeting **.NET 8 LTS** to **.NET 9 STS**. The goal is to reduce failed transformations, surface blockers early, and ensure the codebase is in a healthy, observable, testable state prior to applying upgrade recipes (code mods, dependency bumps, feature enablement, infra changes).

## 2. Version & Support Strategy

| Aspect | .NET 8 | .NET 9 |
|--------|--------|--------|
| Release Type | LTS | STS (Short Term Support) |
| Initial Release | Nov 2023 | Nov 2024 |
| Support End | Nov 2026 | ~May 2026 (18 months after release) |
| Recommended For | Long-lived production baseline | Early adoption of platform improvements; prep for .NET 10 LTS |

Prereq Decision: Confirm business acceptance of moving from an LTS baseline to an STS runtime (shorter support window). If not acceptable, defer upgrade or constrain to selective backports.

## 3. Applicability

Supported project types for automated transformation (initial phase):

- ASP.NET Core (web/api/minimal APIs)
- Worker services / background services
- Class libraries (C#, F# limited, VB not prioritized)
- gRPC services
- Azure Functions isolated worker (net8.0) transitioning to net9.0 (ensure Functions runtime support status; if unsupported at execution time, flag as BLOCKER)

Out of scope (flag for manual path):

- Legacy ASP.NET (System.Web) not already ported to ASP.NET Core
- WCF server components (unless already migrated to CoreWCF / gRPC)
- Xamarin / MAUI (handled separately)
- Unity / Blazor Hybrid special workloads

## 4. Toolchain Prerequisites

You MUST have the following locally or in the build container before running the agent:

- .NET 9 SDK (matching target preview or GA version) installed (verify with `dotnet --list-sdks`)
- Latest .NET 8 SDK retained if multi-targeting/backward validation needed
- Compatible C# language version (roslyn in .NET 9) – set `<LangVersion>preview</LangVersion>` only if using preview-only features; otherwise omit for default
- Updated CI build agents / hosted runners that contain the .NET 9 SDK
- Global.json (if present) reviewed: remove pin to an obsolete 8.x SDK unless deliberate
- Package restore lock files (`packages.lock.json`) either updated or removed if they block version roll-up

## 5. Repository Health Requirements

| Area | Requirement | Validation Method |
|------|-------------|-------------------|
| Clean Build (net8.0) | Succeeds with no build errors | `dotnet build -c Release` |
| Tests | ≥80% of critical path tests passing | `dotnet test` summary |
| Analyzers | No CAxxxx rules at error severity for upgrade-blocking categories (security, reliability) | Analyzer report |
| Code Style | Optional but recommended: consistent formatter (EditorConfig) | Presence of `.editorconfig` |
| Source Control | Working branch clean; all changes committed | Git status |
| Large Binary Artifacts | Excluded from transformation scope | .gitignore review |

## 6. Project File & Target Framework Requirements

Before upgrade, each project should:

- Use SDK-style project format (implicit `<Project Sdk="Microsoft.NET.Sdk">`)
- Use `<TargetFramework>` (singular) or `<TargetFrameworks>` without legacy `netcoreapp3.1` / `net5.0` entries slated for removal
- Avoid hard-coded RID graph duplication unless necessary
- Central Package Management (Directory.Packages.props) consistent or documented
- No custom build targets that pin .NET 8 specific tasks (e.g., referencing old AspNetCore Razor SDK tasks) without update path

## 7. Dependency Readiness

Prepare a dependency matrix capturing each NuGet package:

- Current version, desired .NET 9 compatible version, status (OK / Needs Update / Unknown / Deprecated)
- Flag any packages that are abandoned (no update in ≥12 months) – plan replacement
- Ensure EF Core, ASP.NET Core, gRPC, Serilog/Logging providers, HealthChecks, Polly, System.Text.Json converters, OpenTelemetry exporters have .NET 9 support
- If using native interop (e.g., runtime identifiers with native binaries), confirm updated runtimes compiled for .NET 9
- Remove transitive references to deprecated packages replaced by BCL features (e.g., some polyfills)

## 8. Language & Runtime Feature Considerations

Potential .NET 9 / C# improvements (illustrative – verify via official release notes):

- JIT & GC tuning improvements (may change performance profiles → baseline before & after)
- Additional performance-focused BCL APIs (Span/Memory optimizations)
- Enhanced Native AOT compatibility (if moving workloads toward AOT)

Prereq: Document whether adopting new features is IN-SCOPE (transform) or POST-UPGRADE (defer).

## 9. Known / Potential Breaking Changes Checklist

Create/maintain a local copy of relevant breaking change IDs from Microsoft docs. Typical categories to pre-scan:

- ASP.NET Core middleware behavior adjustments
- Default security protocol / cipher suite shifts
- EF Core behavioral changes (query translation differences, lazy loading adjustments)
- Obsoleted APIs elevated from warning to error
- JSON serialization defaults (e.g., polymorphism, number handling) – verify custom converters
- Trimming / AOT related warnings turning into errors

Prereq: Run `dotnet build -warnaserror:NETSDK*` under .NET 9 preview to enumerate new warnings early.

## 10. Configuration & Hosting

| Topic | Prerequisite |
|-------|--------------|
| AppSettings | No environment-specific overrides relying on removed keys |
| Logging | Structured logging pipeline version-compatible (Serilog sinks / OpenTelemetry exporters updated) |
| Kestrel | Custom limits validated under new runtime (connection limits, HTTP/2, HTTP/3 toggles) |
| Containers | Base image updated to `mcr.microsoft.com/dotnet/aspnet:9.0` & `mcr.microsoft.com/dotnet/sdk:9.0` (multi-arch as needed) |
| Azure App Service / Functions | Target environment supports net9.0 (verify platform availability) |
| Certificates / TLS | No reliance on obsolete crypto algorithms removed / blocked by updated OS images |

## 11. Observability & Telemetry

Prerequisites:

- Distributed tracing (OpenTelemetry) packages at versions supporting .NET 9
- Metrics & logging exporters tested in canary environment
- Health check endpoints passing (liveness/readiness) prior to upgrade
- Baseline performance metrics captured (latency, throughput, memory, CPU, GC stats) – store snapshot for regression comparison

## 12. Security & Compliance

| Item | Requirement |
|------|------------|
| Vulnerability Scan | All high/critical CVEs in dependencies remediated or accepted with documented exception before upgrade |
| Secret Management | No secrets in source; using Key Vault / environment injection |
| TLS | Enforce TLS 1.2+ (1.3 if platform supported) |
| Auth Libraries | Identity / OAuth libraries upgraded to net9-compatible versions |
| Code Scanning | Static analysis (e.g., GitHub Advanced Security / CodeQL) baseline run stored |

## 13. Data, Storage & EF Core

Prereqs:

- Migrations folder clean; pending model snapshot matches database
- Provider packages (SQL Server, PostgreSQL, Cosmos, etc.) updated for .NET 9
- Feature flags for new EF Core behaviors (if needed) documented
- Performance-critical queries benchmarked (BenchmarkDotNet or representative load test)

## 14. Testing Prerequisites

Minimum expected test coverage set (e.g., 60–70% line OR 80% of critical service layer). Ensure:

- Unit tests deterministic (no reliance on wall clock / random without seeding)
- Integration tests can spin up ephemeral dependencies (containers via Testcontainers / Docker Compose)
- Load / smoke test suite prepared for post-upgrade validation
- Contract / API compatibility snapshots (e.g., OpenAPI diff) captured

## 15. Feature Flags & Gradual Rollout

Plan a ring-based rollout:

1. Dev / local validation
2. Internal canary (small % traffic)
3. Staged environment (performance test)
4. Production partial rollout (progressive traffic shifting)

Prereq: Toggle infrastructure (LaunchDarkly, App Configuration, custom) in place.

## 16. Performance Baseline

Record (pre-upgrade):

- P95 / P99 latency for key endpoints
- Throughput (req/sec) under representative load
- Memory footprint steady-state & GC pause durations
- Startup time (cold / warm) – especially for Functions / container autoscale

Store baseline artifacts to compare after upgrade.

## 17. Automation & Pipeline Readiness

| Stage | Prereq |
|-------|--------|
| Build | Pipeline images updated with .NET 9 SDK |
| Test | CI can run multi-target if temporarily maintaining net8.0 + net9.0 |
| SCA / License | Updated scanning supports new package versions |
| SBOM | Regenerate SBOM post-upgrade (e.g., `dotnet build /p:GeneratePackageOnBuild=true`) |
| Deployment | Infra templates (Bicep/Terraform) parameterized for new image tags |

## 18. Risk Register (Sample)

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Third-party package lacks .NET 9 release | Medium | High | Identify fork / replacement early |
| Performance regression in serialization | Medium | Medium | Capture baseline & run A/B canary |
| Analyzer warnings escalate to errors | Medium | Low | Pre-scan with preview SDK |
| Shorter STS support misaligned with policy | High | High | Executive sign-off or delay upgrade |

## 19. Upgrade Readiness Checklist (Agent Gate)

Mark all TRUE before invoking transformation:

- [ ] Clean net8.0 build & tests pass
- [ ] .NET 9 SDK installed & discoverable in CI
- [ ] Dependency matrix completed (no UNKNOWN status)
- [ ] All critical packages have net9-compatible versions
- [ ] Breaking change review performed & documented
- [ ] Baseline performance & telemetry snapshot stored
- [ ] Security scan: no unmitigated critical CVEs
- [ ] EF Core migrations clean
- [ ] Observability stack updated (OTel, logging, metrics)
- [ ] Rollout plan & feature flags prepared
- [ ] Risk register reviewed & accepted
- [ ] Business approval for STS adoption obtained

## 20. Agent Inputs & Metadata Required

When invoking the Transformation Agent, supply (or ensure it can derive):

- Repository root path
- Solution / project file list
- Target TFM: `net9.0`
- Multi-target policy (retain net8.0? yes/no)
- Dependency allow/deny lists
- Analyzer severity configuration
- Paths to test projects
- Baseline metrics artifact references (optional but recommended)

## 21. Post-Upgrade Validation Targets

The agent or follow-up pipeline should automatically run:

- Build & test (all target frameworks)
- API surface diff (e.g., `dotnet publicapi` if used)
- OpenAPI contract diff (if swagger) – fail on breaking changes unless approved
- Performance smoke test (minimum threshold check)
- Security scan & SBOM regeneration
- Logging/tracing sanity check (no missing spans, no cardinality explosion)

## 22. Documentation & Traceability

Maintain:

- CHANGELOG entry summarizing upgrade scope
- Recorded commit hash for baseline pre-upgrade
- Upgrade decision log (architecture / risk sign-off)
- Link to internal runbook for rollback procedure (revert image tag / redeploy previous artifact)

## 23. Rollback Preconditions

Ensure ability to revert quickly:

- Container images for .NET 8 build retained (do not prune until stabilization)
- Database schema changes (if any) are backward compatible or reversible
- Feature flags allow disabling net9-only features

## 24. References (Update with official links)

| Topic | Link Placeholder |
|-------|------------------|
| Official .NET 9 Release Notes | (add when published) |
| .NET Breaking Changes Catalog | (add) |
| EF Core 9 Docs | (add) |
| ASP.NET Core 9 Docs | (add) |
| OpenTelemetry for .NET | (add) |

---
Revision History:

- 2025-09-29: Initial draft created for transformation agent.

