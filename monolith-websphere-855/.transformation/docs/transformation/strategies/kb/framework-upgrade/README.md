# Framework Upgrade Strategy [WIP]

## Purpose

Systematically upgrade legacy framework/runtime versions (.NET Framework <=4.x, outdated .NET Core, Java 8/11) to supported LTS baselines (.NET 8, Java 17/21) with minimal disruption and controlled risk.

## Problems Addressed

- Security exposure from unsupported runtimes.
- Dependency/library stagnation (no updates available for older TFMs / Java levels).
- Tooling incompatibility (new analyzers, container images, cloud SDKs require modern runtime).
- Performance & memory inefficiencies versus modern JIT/GC improvements.

## Goals

| Goal | Description | Success Indicator |
|------|-------------|-------------------|
| LTS Alignment | Target current organizational LTS versions | All projects build for chosen LTS |
| Risk Reduction | Stage risky changes incrementally | No Sev1 regression in rollout window |
| Observability | Telemetry around upgrade impact | Baseline vs post-upgrade metrics dashboard |
| Reversibility | Fast rollback plan | Rollback executable < 30 min |
| Automation | Repeatable upgrade pipeline | Scripted steps (>80% automated) |

## Non-Goals

- Full application re-architecture.
- Language feature adoption beyond required changes (advanced refactors tracked separately).

## Upgrade Philosophy

Prefer incremental, low-blast-radius steps: multi-target where feasible, introduce analyzers early, separate mechanical edits from semantic logic changes, maintain green tests between phases.

## Phase Overview

| Phase | Objective | Outputs |
|-------|----------|---------|
| 1 Inventory | Catalog frameworks, Java levels, dependencies | Inventory report |
| 2 Target Selection | Choose LTS baseline & interim multi-target (if needed) | Upgrade decision record |
| 3 Preparation | Add analyzers, enable nullable / warnings | Static analysis report |
| 4 Dual Build | Multi-target (net48 + netstandard2.0) or preview Java build | Dual build success |
| 5 Final Bump | Set single modern target (net8.0 / Java 17/21) | Updated project/build files |
| 6 API Remediation | Replace deprecated/removed APIs | Remediation patch set |
| 7 Dependency Modernization | Update packages/plugins & lockfiles | Dependency diff report |
| 8 Test & Perf | Full test, perf baseline comparison | Test & perf reports |
| 9 Release & Monitor | Deploy + enhanced telemetry checks | Post-upgrade health log |
| 10 Cleanup | Remove shims, obsolete directives | Final cleanup PR |

## Decision Matrix (When to Multi-Target)

| Condition | Action |
|-----------|--------|
| Large code surface + legacy dependencies | Introduce netstandard2.0 bridge first |
| Minimal external dependencies | Direct jump to net8.0 |
| Java 8 with heavy javax.* usage (Jakarta shift) | Intermediate upgrade to Java 11 + transformation, then 17 |
| Build tooling lacks new compiler | Upgrade build agents/toolchain before code changes |

## Compatibility Considerations

| Area | .NET | Java |
|------|------|------|
| Reflection / AppDomains | AppDomain APIs removed/limited | Modules vs class loaders unchanged |
| WCF | Replace with gRPC / REST | n/a |
| Remoting | Remove; use gRPC / named pipes | n/a |
| Cryptography defaults | Stronger TLS / cipher defaults | Updated TLS provider defaults |
| GC / Memory | Server GC enhancements | G1/ZGC improvements |

## Breaking Change Mitigation

- Introduce compatibility shims (extension methods, adaptor classes) temporarily.
- Wrap risky API migrations (e.g., binary formatter removal) behind feature flags.
- Stage config changes (Kestrel, TLS) separate from code refactors.

## Testing Strategy

| Layer | Focus |
|-------|-------|
| Unit | API shape & logic unchanged |
| Integration | External service contracts & serialization |
| Migration Specific | Performance, memory, TLS/cipher negotiation |
| Canary Deployment | Real traffic sample; monitor error budget |

## Telemetry & KPIs

| KPI | Why |
|-----|-----|
| Startup time delta | Detect initialization regressions |
| Throughput delta | Assess performance improvement/regression |
| Memory footprint change | GC tuning evaluation |
| Error rate change | Hidden breaking change detection |

## Rollback Strategy

Maintain previous build artifacts & infra deployment slot. Rollback triggers: error rate spike, critical perf degradation, security policy conflict. Automate config toggles to restore prior runtime quickly.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Hidden transitive dependency incompatibility | Runtime failure | Dependency graph & BOM analysis |
| Large API surface churn | Extended freeze | Parallel work streams + module prioritization |
| Performance regression | SLA breach | Perf test gates + targeted profiling |
| Security posture drift | Compliance failure | Re-validate vulnerability scan post-upgrade |

## References

- Official .NET Support Policy
- Java LTS Roadmap
- Internal Upgrade Playbook

## Next Steps (Per Language)

See language-specific guides in `languages/dotnet.md` and `languages/java.md`.
