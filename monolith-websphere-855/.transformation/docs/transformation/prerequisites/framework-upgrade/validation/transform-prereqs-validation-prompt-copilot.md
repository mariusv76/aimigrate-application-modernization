# Copilot Integration Prompt: .NET 8 -> .NET 9 Prerequisite Validation

This variant of the prerequisite validation prompt is optimized for use inside GitHub Copilot Chat / future agent integrations. It introduces modes, diff awareness, chunking rules, and anti‑hallucination safeguards while remaining compatible with the standalone schema.

> Schema Reference: The authoritative JSON Schema is **not embedded here** to avoid drift. Always validate against `docs/transformation/prerequisites/framework-upgrade/validation/transform-prereqs.schema.json`. If that schema changes (new fields, enums, required keys), update only the standalone file plus its change log; this Copilot prompt remains schema-light and refers externally.

---

## 1. Usage Overview

Use this prompt template when invoking Copilot to generate a machine-readable readiness report for upgrading from .NET 8 (LTS) to .NET 9 (STS). The output MUST be a single JSON object conforming to the standalone schema (`transform-prereqs.schema.json`). This file adds:

- Modes (full, diff, focus, remediation, summary, packages-chunk)
- Run correlation (`runId`)
- Diff minimization rules
- Package chunk streaming strategy
- Strict evidence + redaction policies
- Deterministic ordering & precedence rules

---
 
## 2. Template (Insert Signals Between Markers)

```text
<<PREREQ_PROMPT_SPEC_BEGIN>>
Role: Prerequisite Validation Engine for .NET 8 -> .NET 9 upgrade.

Modes:
  full | diff | focus:<category|itemId> | remediation | summary | packages-chunk:<n>/<total>

Contract:
  - Output exactly ONE JSON object; no markdown fences, no extra text.
  - Top-level keys (fixed order):
    metadata, overall, versionSupport, applicability, toolchain, repoHealth, projectModel,
    dependencies, languageRuntime, breakingChanges, configurationHosting, observability,
    security, dataTier, testing, featureFlags, performanceBaseline, automation, riskRegister,
    readinessChecklist, agentInputs, postUpgradeValidation, documentation, rollback
  - Status enum: pass | warn | fail | na
  - Precedence for overall.status: fail > warn > pass > na (ignore optional=true when escalating).
  - Missing evidence => warn + remediation starting 'Provide evidence: ...'
  - Never hallucinate file names not in provided signals.
  - Never upgrade uncertain items to pass.
  - Redact secrets (tokens, keys, passwords) -> "<redacted>".

Additional JSON fields:
  metadata: {
    schemaVersion: "1.0.0", runId: <string>, generatedAtUtc (ISO8601),
    targetFramework: "net9.0", sourceFramework: "net8.0", multiTarget?: bool
  }
  overall: may include { hints?: string[] }
  dependencies: add packagesCounts { total, pass, warn, fail, deprecated, breaking }

Large dependency handling:
  - If dependencies.packages would exceed 200 entries AND mode not packages-chunk/full:
      Include only non-pass entries + top 10 recently changed (if diff data supplied) and packagesCounts.

Diff mode:
  - Input may include previousResult JSON.
  - Output only categories/items whose status OR remediation OR evidence changed, plus metadata & overall.
  - For changed items add previousStatus.

Focus mode (focus:<categoryName> or focus:<itemId>):
  - Evaluate only the specified scope.
  - All other categories: status = "na", empty items[], no packages.

Remediation mode:
  - Include only items where status != pass (still include metadata & overall).

Summary mode:
  - Include metadata, overall, each category with status + counts of items by status; omit per-item arrays except those not pass.

Packages chunk mode (packages-chunk:n/total):
  - Emit only dependencies category plus metadata & overall.
  - Include chunkInfo { index: n, totalChunks }.
  - Each chunk's packages array is a slice; still provide packagesCounts.

Evidence object allowed keys ONLY: files[], commands[], description, hashes[].
Truncate description > 1000 chars with '...<truncated>'.

If a category has zero relevant signals and no mandatory checks -> status = na (NOT pass).

Compute overall.hints array with gating messages for any fail conditions (max 5 concise strings).

Return ONLY the JSON object.

<<REPO_SIGNALS_BEGIN>>
(Insert dynamic repository signals here: file list, csproj contents, build logs, test summaries, analyzer reports, vuln scans, performance baselines, prior JSON result, etc.)
<<REPO_SIGNALS_END>>

Requested mode: <MODE_PLACEHOLDER>
Run ID: <RUN_ID_PLACEHOLDER>

<<PREREQ_PROMPT_SPEC_END>>
```

---

## 3. Canonical Item IDs

Use the same IDs as documented in the base validation prompt or previously generated reports (e.g., `dotnet9-sdk-installed`, `perf-baseline-captured`). Adding new IDs is allowed; ensure `id` matches `^[a-z0-9-]+$`. When introducing a new ID that requires schema changes (e.g., a new top-level category), update `transform-prereqs.schema.json` first and increment `schemaVersion` if breaking.

---

## 4. Invocation Guidance

1. Gather signals (scripts can collect build/test/analyzer/vulnerability/perf outputs into a temp folder).
2. Template-fill: replace markers, choose mode.
3. For diff mode pass previous JSON as part of signals.
4. Send prompt to Copilot Chat. Ensure no extraneous commentary precedes markers.
5. Validate returned JSON against schema (fail pipeline if invalid / status=fail).

---

## 5. Example (Diff Mode Minimal)

```text
Requested mode: diff
Run ID: 2025-09-29-002

<<REPO_SIGNALS_BEGIN>>
previousResult: { ... prior JSON ... }
changes: Updated Serilog to 3.1.0, added perf baseline
buildOutput: (trimmed)
<<REPO_SIGNALS_END>>
```

(Expected: Only categories with changed items + metadata + overall.)

---

## 6. Post-Processing Recommendations

- Schema Validation: Always point validators at `docs/transformation/prerequisites/framework-upgrade/validation/transform-prereqs.schema.json` (single source of truth).
- Gating Logic: Fail CI if any fail items OR overall.status == fail.
- Trend: Persist each run’s JSON (folder `upgrade-readiness-history/`).
- Observability: Optionally emit a condensed SARIF or Markdown summary for PR comments.
- Drift Prevention: Do not copy the schema inline here; regenerate prompt if schema major version changes.

---
 
## 7. Security & Compliance Notes

- Redaction required for any probable secret (pattern: 32+ hex chars, GUID-like tokens, `key=`, `secret=`, etc.).
- Do not include raw certificate PEM blocks; summarize fingerprint (SHA256 first 12 chars).

---

## 8. Extension Hooks (Future)

Reserved additions (do not break schema now):

- supplyChain, aotReadiness, trimming
- sbomVerification, provenanceAttestation

---

## 9. Quick Checklist (Human)

| Concern | Covered Here |
|---------|--------------|
| Diff Minimization | Yes |
| Chunked Dependencies | Yes |
| Secret Redaction | Yes |
| Strict Output | Yes |
| Modes for UI/CI | Yes |
| Idempotent Field Order | Yes |

---

## 10. Change Control

`schemaVersion` is declared ONLY in `docs/transformation/prerequisites/framework-upgrade/validation/transform-prereqs.schema.json`.

| Concern | Action |
|---------|--------|
| Add optional field | Bump minor if consumer logic depends on it; otherwise patch. |
| Add required field / enum removal | Bump major and announce in change log. |
| Remove unused optional field | Minor (if not required). |
| Prompt mode changes (diff/focus/etc.) | Document here; schema bump only if structural output changes. |

Current alignment: schemaVersion 1.0.0 (no breaking changes pending).

---
© 2025 Transformation Agent – Copilot Prompt Variant
