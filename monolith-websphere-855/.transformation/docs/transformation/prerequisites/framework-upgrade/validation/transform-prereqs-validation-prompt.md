# Transformation Prerequisites Validation Prompt

Use this prompt to have an agent (or LLM function) produce a deterministic, machine‑readable JSON (preferred) or YAML artifact that encodes the current repository's readiness for upgrading from .NET 8 (LTS) to .NET 9 (STS), based on the human guidelines in `transform-prereqs.md`.

---

## Prompt (Copy/Paste Below When Invoking the Validation Agent)

SYSTEM ROLE (Validator)
You are a strict prerequisite validation engine for a .NET application modernization pipeline (upgrade .NET 8 -> .NET 9). You MUST:

1. Parse repository signals (provided separately) and map them to structured readiness fields.
2. Output ONLY a single JSON document (no commentary) conforming to the schema below.
3. For each check, set status = "pass" | "fail" | "warn" | "na".
4. Provide machine-actionable remediation guidance for every item not "pass".
5. Never invent PASS—prefer WARN when uncertain.
6. Preserve field ordering shown in the schema for diff stability.
7. Include a top-level overall.status computed via precedence (fail > warn > pass > na) ignoring items explicitly marked optional.
8. Keep strings concise; remediation.maxLength <= 280 chars.

USER CONTENT
Validate the repository against the defined transformation prerequisites. Use supplied metadata (file listings, project files, build/test outputs, dependency manifests, analyzer reports, security scan results, performance baselines, etc.). If a signal is missing, mark the item warn with a remediation that describes what evidence is required.

OUTPUT FORMAT
Return ONLY JSON that validates against the JSON Schema below. No markdown fences.

---

## JSON Schema (Draft 2020-12)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://example.org/transform-prereqs.schema.json",
  "title": ".NET 8 -> .NET 9 Upgrade Prerequisites Validation Result",
  "type": "object",
  "required": [
    "metadata","overall","versionSupport","applicability","toolchain","repoHealth","projectModel","dependencies","languageRuntime","breakingChanges","configurationHosting","observability","security","dataTier","testing","featureFlags","performanceBaseline","automation","riskRegister","readinessChecklist","agentInputs","postUpgradeValidation","documentation","rollback"
  ],
  "properties": {
    "metadata": {
      "type": "object",
      "required": ["schemaVersion","generatedAtUtc","targetFramework","sourceFramework","generator"],
      "properties": {
        "schemaVersion": {"type": "string","const": "1.0.0"},
        "generatedAtUtc": {"type": "string","format": "date-time"},
        "targetFramework": {"type": "string","const": "net9.0"},
        "sourceFramework": {"type": "string","const": "net8.0"},
        "generator": {"type": "string"},
        "multiTarget": {"type": "boolean"},
        "notes": {"type": "string"}
      }
    },
    "overall": {
      "type": "object",
      "required": ["status","summary"],
      "properties": {
        "status": {"type": "string","enum": ["pass","warn","fail","na"]},
        "summary": {"type": "string"},
        "counts": {
          "type": "object",
          "required": ["pass","warn","fail","na"],
          "properties": {
            "pass": {"type": "integer","minimum": 0},
            "warn": {"type": "integer","minimum": 0},
            "fail": {"type": "integer","minimum": 0},
            "na": {"type": "integer","minimum": 0}
          }
        }
      }
    },
    "versionSupport": {"$ref": "#/definitions/category"},
    "applicability": {"$ref": "#/definitions/category"},
    "toolchain": {"$ref": "#/definitions/category"},
    "repoHealth": {"$ref": "#/definitions/category"},
    "projectModel": {"$ref": "#/definitions/category"},
    "dependencies": {"$ref": "#/definitions/dependencyCategory"},
    "languageRuntime": {"$ref": "#/definitions/category"},
    "breakingChanges": {"$ref": "#/definitions/category"},
    "configurationHosting": {"$ref": "#/definitions/category"},
    "observability": {"$ref": "#/definitions/category"},
    "security": {"$ref": "#/definitions/category"},
    "dataTier": {"$ref": "#/definitions/category"},
    "testing": {"$ref": "#/definitions/category"},
    "featureFlags": {"$ref": "#/definitions/category"},
    "performanceBaseline": {"$ref": "#/definitions/category"},
    "automation": {"$ref": "#/definitions/category"},
    "riskRegister": {"$ref": "#/definitions/category"},
    "readinessChecklist": {"$ref": "#/definitions/category"},
    "agentInputs": {"$ref": "#/definitions/category"},
    "postUpgradeValidation": {"$ref": "#/definitions/category"},
    "documentation": {"$ref": "#/definitions/category"},
    "rollback": {"$ref": "#/definitions/category"}
  },
  "definitions": {
    "statusEnum": {"type": "string","enum": ["pass","warn","fail","na"]},
    "evidence": {
      "type": "object",
      "required": ["description"],
      "properties": {
        "files": {"type": "array","items": {"type": "string"}},
        "commands": {"type": "array","items": {"type": "string"}},
        "description": {"type": "string"}
      }
    },
    "item": {
      "type": "object",
      "required": ["id","title","status"],
      "properties": {
        "id": {"type": "string","pattern": "^[a-z0-9-]+$"},
        "title": {"type": "string"},
        "status": {"$ref": "#/definitions/statusEnum"},
        "optional": {"type": "boolean","default": false},
        "details": {"type": "string"},
        "remediation": {"type": "string"},
        "evidence": {"$ref": "#/definitions/evidence"},
        "metrics": {"type": "object","additionalProperties": {"type": ["number","string","boolean"]}},
        "links": {"type": "array","items": {"type": "string","format": "uri"}}
      }
    },
    "category": {
      "type": "object",
      "required": ["status","items"],
      "properties": {
        "status": {"$ref": "#/definitions/statusEnum"},
        "items": {"type": "array","items": {"$ref": "#/definitions/item"}},
        "summary": {"type": "string"}
      }
    },
    "dependencyDetail": {
      "type": "object",
      "required": ["name","currentVersion","status"],
      "properties": {
        "name": {"type": "string"},
        "currentVersion": {"type": "string"},
        "latestKnownVersion": {"type": "string"},
        "targetVersion": {"type": "string"},
        "status": {"$ref": "#/definitions/statusEnum"},
        "breaking": {"type": "boolean"},
        "deprecated": {"type": "boolean"},
        "migrationNotes": {"type": "string"},
        "license": {"type": "string"},
        "source": {"type": "string","enum": ["nuget","local","git","other"]}
      }
    },
    "dependencyCategory": {
      "type": "object",
      "required": ["status","items","packages"],
      "properties": {
        "status": {"$ref": "#/definitions/statusEnum"},
        "items": {"type": "array","items": {"$ref": "#/definitions/item"}},
        "packages": {"type": "array","items": {"$ref": "#/definitions/dependencyDetail"}},
        "summary": {"type": "string"}
      }
    }
  }
}
```

---

## Required Core Items (Map to Doc Sections)

Below are canonical item IDs to keep consistency. The agent SHOULD emit at least these (others may be appended):

| Section | Item ID | Title |
|---------|---------|-------|
| versionSupport | lts-to-sts-acceptance | Business acceptance of STS move |
| applicability | supported-project-types-detected | Supported project types detected |
| toolchain | dotnet9-sdk-installed | .NET 9 SDK present |
| toolchain | ci-agent-updated | CI agent images updated |
| repoHealth | clean-build-net8 | Clean net8 build |
| repoHealth | tests-pass-threshold | Test success threshold |
| repoHealth | analyzers-critical-clear | Critical analyzer rules clear |
| projectModel | sdk-style-projects | All projects SDK style |
| projectModel | targetframework-updatable | TargetFramework(s) updatable |
| dependencies | package-matrix-complete | Dependency matrix completeness |
| dependencies | deprecated-packages-flagged | Deprecated packages flagged |
| languageRuntime | lang-features-scope-defined | Feature adoption scope defined |
| breakingChanges | preview-warnings-reviewed | Preview / new warnings reviewed |
| configurationHosting | container-base-updated | Containers base image upgradable |
| observability | otel-compatible | OpenTelemetry packages compatible |
| security | cve-scan-clean | Critical CVEs remediated |
| dataTier | ef-migrations-clean | EF migrations clean |
| testing | contract-diff-baseline | API contract baseline captured |
| featureFlags | rollout-flags-configured | Feature flags configured |
| performanceBaseline | perf-baseline-captured | Performance baseline captured |
| automation | pipeline-multitarget-ready | Pipeline multi-target ready |
| riskRegister | risk-register-defined | Risk register defined |
| readinessChecklist | checklist-all-marked | Checklist conditions satisfied |
| agentInputs | agent-inputs-available | Inputs resolvable |
| postUpgradeValidation | validation-plan-defined | Post-upgrade validation plan |
| documentation | changelog-entry-prepared | Changelog entry prepared |
| rollback | rollback-assets-retained | Rollback assets retained |

---

## Example Minimal Output (Illustrative Only)

```json
{
  "metadata": {
    "schemaVersion": "1.0.0",
    "generatedAtUtc": "2025-09-29T12:34:56Z",
    "targetFramework": "net9.0",
    "sourceFramework": "net8.0",
    "generator": "transform-prereq-agent@1.2.3",
    "multiTarget": true
  },
  "overall": {"status": "warn","summary": "2 fail, 5 warn, 15 pass","counts": {"pass": 15,"warn": 5,"fail": 2,"na": 0}},
  "versionSupport": {"status": "pass","items": [{"id": "lts-to-sts-acceptance","title": "Business acceptance of STS move","status": "pass","details": "Approved in ARCH-42"}]},
  "applicability": {"status": "pass","items": [{"id": "supported-project-types-detected","title": "Supported project types detected","status": "pass","evidence": {"files": ["src/WebApp/WebApp.csproj"]}}]},
  "toolchain": {"status": "fail","items": [{"id": "dotnet9-sdk-installed","title": ".NET 9 SDK present","status": "fail","remediation": "Install SDK 9.0.x and update CI image","evidence": {"commands": ["dotnet --list-sdks"],"description": "Only 8.0.401 installed"}}]},
  "repoHealth": {"status": "warn","items": [{"id": "clean-build-net8","title": "Clean net8 build","status": "pass"},{"id": "tests-pass-threshold","title": "Test success threshold","status": "warn","remediation": "Raise critical test pass rate to >=80% (current 71%)","metrics": {"passRate": 0.71}}]},
  "projectModel": {"status": "pass","items": [{"id": "sdk-style-projects","title": "All projects SDK style","status": "pass"}]},
  "dependencies": {"status": "warn","items": [{"id": "package-matrix-complete","title": "Dependency matrix completeness","status": "warn","remediation": "Add versions for 3 remaining packages"}],"packages": [{"name": "Serilog","currentVersion": "3.1.0","latestKnownVersion": "3.1.0","targetVersion": "3.1.0","status": "pass","source": "nuget"},{"name": "Some.Legacy.Package","currentVersion": "1.2.0","latestKnownVersion": "1.2.0","targetVersion": "(replace)","status": "fail","deprecated": true,"migrationNotes": "Replace with BCL feature"}]},
  "languageRuntime": {"status": "na","items": []},
  "breakingChanges": {"status": "warn","items": [{"id": "preview-warnings-reviewed","title": "Preview / new warnings reviewed","status": "warn","remediation": "Run build with .NET 9 preview SDK and capture NETSDK warnings"}]},
  "configurationHosting": {"status": "pass","items": [{"id": "container-base-updated","status": "pass","title": "Containers base image upgradable"}]},
  "observability": {"status": "pass","items": [{"id": "otel-compatible","title": "OpenTelemetry packages compatible","status": "pass"}]},
  "security": {"status": "pass","items": [{"id": "cve-scan-clean","title": "Critical CVEs remediated","status": "pass"}]},
  "dataTier": {"status": "pass","items": [{"id": "ef-migrations-clean","title": "EF migrations clean","status": "pass"}]},
  "testing": {"status": "warn","items": [{"id": "contract-diff-baseline","title": "API contract baseline","status": "warn","remediation": "Export OpenAPI spec pre-upgrade and store artifact"}]},
  "featureFlags": {"status": "pass","items": [{"id": "rollout-flags-configured","title": "Feature flags configured","status": "pass"}]},
  "performanceBaseline": {"status": "fail","items": [{"id": "perf-baseline-captured","title": "Performance baseline captured","status": "fail","remediation": "Execute load test and record P95 latency & throughput"}]},
  "automation": {"status": "pass","items": [{"id": "pipeline-multitarget-ready","title": "Pipeline multi-target ready","status": "pass"}]},
  "riskRegister": {"status": "pass","items": [{"id": "risk-register-defined","title": "Risk register defined","status": "pass"}]},
  "readinessChecklist": {"status": "warn","items": [{"id": "checklist-all-marked","title": "Checklist conditions satisfied","status": "warn","remediation": "Two items remain unchecked: perf-baseline-captured, package-matrix-complete"}]},
  "agentInputs": {"status": "pass","items": [{"id": "agent-inputs-available","title": "Inputs resolvable","status": "pass"}]},
  "postUpgradeValidation": {"status": "warn","items": [{"id": "validation-plan-defined","title": "Post-upgrade validation plan","status": "warn","remediation": "Add automation steps for public API diff"}]},
  "documentation": {"status": "pass","items": [{"id": "changelog-entry-prepared","title": "Changelog entry prepared","status": "pass"}]},
  "rollback": {"status": "pass","items": [{"id": "rollback-assets-retained","title": "Rollback assets retained","status": "pass"}]}
}
```

---

## Implementation Notes (Status & Guidance)

This section tracks the operational state of supporting assets and how to evolve them safely.

### Current Status

| Aspect | Status | Notes |
|--------|--------|-------|
| Version control & schema evolution | In place | `schemaVersion` = 1.0.0. No breaking changes introduced yet. |
| Companion JSON Schema | Completed | `docs/transformation/transform-prereqs.schema.json` is the authoritative schema (includes extensions: `hints`, `previousStatus`, `packagesCounts`, `chunkInfo`). |
| Embedded Schema (below) | Core only | Does NOT include the extended fields above; retained for readability. |
| Diff support | Supported conceptually | Use Copilot prompt modes (`diff`, `focus`, `remediation`). Recommend storing outputs under `upgrade-readiness-history/`. |
| Tooling scripts | Added | See `ai-foundry-agents/transform-agent/scripts/` (`gather-prereq-signals.ps1`, `stage-prereq-prompt.ps1`, `validate-prereqs.ps1`, `merge-dependency-chunks.ps1`). |
| Automation workflow | Optional | A sample workflow can be (re)introduced if needed; not required for manual runs. |
| Change log | Pending | Add entries here when schema fields are added/removed or semantics change. |

### Authoritative Schema

Always treat the standalone file as source of truth for validation logic:

```text
docs/transformation/transform-prereqs.schema.json
```

If you add or remove required fields OR alter enums:

1. Increment `schemaVersion` (semver major for breaking, minor for additive, patch for clarifications).
2. Update both the standalone schema and (optionally) reflect changes in this document's embedded copy.
3. Add a row to the change log (see below after first change).

### Recommended Change Workflow

1. Propose update (new field / category) in a PR with rationale.
2. Update standalone schema first; run validator script on sample JSON.
3. Update Copilot prompt variant if new modes or fields affect output contract.
4. Regenerate or adjust any historical baseline JSON if needed (only if breaking).

### Future Enhancements (Tracked)

- Add `supplyChain`, `aotReadiness`, `trimming` sections (currently placeholders).
- Introduce optional `sbomVerification`, `provenanceAttestation` once supply chain scanning is integrated.
- Provide a machine-readable severity weighting map for gating logic.

### (Reserved) Schema Change Log

| Version | Date | Change | Impact |
|---------|------|--------|--------|
| 1.0.0 | Initial | Base categories + extended dependency fields referenced externally | Baseline |

> When a new version is released, append a new row rather than rewriting history.

## Future Extensions (Reserved Fields)

- `supplyChain` (SBOM, signing, provenance)
- `aotReadiness` (Native AOT suitability)
- `trimming` (ILLink warnings summary)

---
© 2025 Transformation Agent Prerequisites Schema (Internal Draft)
