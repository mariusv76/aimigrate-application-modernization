# Framework Upgrade (Java)

## Objectives
Upgrade legacy Java 8/11 codebases to Java 17 (primary) or 21 (optional) LTS with controlled API, build, and dependency modernization.

## Target Baseline
| Area | Target |
|------|--------|
| Language Level | 17 (or 21 if strategic) |
| Build Flags | --release 17 |
| GC | G1 (default) / Consider ZGC for latency workloads |
| Modules | Automatic (no full modularization initial phase) |

## Pre-Upgrade Checklist
- All tests pass under current version.
- Ensure build agents / CI have JDK 17+.
- Inventory dependencies and note any with Java 8 only support.

## Build Configuration (Maven)
```xml
<properties>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  <maven.compiler.release>17</maven.compiler.release>
</properties>
```

## Build Configuration (Gradle)
```kotlin
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
```

## Common Remediation Areas
| Legacy Aspect | Action |
|---------------|--------|
| javax.* (Jakarta EE shift) | Migrate to jakarta.* packages where applicable |
| Deprecated security props | Replace with updated algorithms / providers |
| Reflection internals (setAccessible) | Replace or add `--add-opens` (temporary) |
| PermGen references | Remove (obsolete) |

## Encapsulated Internals
JDK 17 strongly encapsulates internals. Avoid adding broad `--add-opens`; isolate to minimal modules and plan removal iterations.

## Testing Focus
| Area | Consideration |
|------|--------------|
| Serialization (Jackson) | Java record compatibility / module warnings |
| TLS / Cipher Suites | Stricter defaults; validate outbound integrations |
| Time APIs | Prefer java.time; remove legacy Date/Calendar edge logic |

## Performance Validation
Capture baseline throughput, latency, GC pause metrics. After upgrade compare; tune heap sizing, consider enabling String deduplication if beneficial.

## Optional Enhancements Post-Upgrade
| Feature | Benefit |
|---------|--------|
| Records | Reduced boilerplate DTOs |
| Switch Expressions | Cleaner control flow |
| Text Blocks | Simplify multi-line strings |
| Pattern Matching for instanceof | Safer casts |

## Rollback
Preserve previous build artifacts (Java 8/11) and maintain branch until post-deployment burn-in window completes. Automated pipeline can toggle target toolchain property.

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Dependency not Java 17 compatible | Find upgrade/fork; isolate behind interface |
| Illegal reflective access warnings -> errors future | Track & refactor reflective usages |
| Performance regression | GC & profiler analysis (JFR) |
| Jakarta namespace drift | Migrate incrementally + shading if necessary |
