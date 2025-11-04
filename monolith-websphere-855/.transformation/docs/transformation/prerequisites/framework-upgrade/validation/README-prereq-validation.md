# .NET Upgrade Prerequisite Validation – Guide

This guide explains the two prerequisite validation prompt assets and how to use the GitHub Copilot variant to produce a machine‑readable readiness report for upgrading from **.NET 8 (LTS)** to **.NET 9 (STS)**.

## 1. Files Overview

| File | Purpose | Key Contents |
|------|---------|--------------|
| `transform-prereqs-validation-prompt.md` | Human‑oriented base validation prompt with embedded (core) JSON Schema, canonical item IDs, example output. | Core schema (simplified), example JSON, checklist. |
| `transform-prereqs-validation-prompt-copilot.md` | Copilot / agent optimized prompt (no embedded schema) referencing the authoritative standalone schema; adds modes (full, diff, focus, remediation, summary, packages-chunk). | Prompt template block, mode semantics, change control table. |
| `transform-prereqs.schema.json` | Standalone authoritative JSON Schema (source of truth). | Extended fields: `hints`, `previousStatus`, `packagesCounts`, `chunkInfo`. |
| (scripts) `ai-foundry-agents/transform-agent/scripts/gather-prereq-signals.ps1`, `ai-foundry-agents/transform-agent/scripts/stage-prereq-prompt.ps1`, `ai-foundry-agents/transform-agent/scripts/validate-prereqs.ps1`, `ai-foundry-agents/transform-agent/scripts/merge-dependency-chunks.ps1` | Automation helpers for collecting evidence, staging prompt, validating JSON, merging chunked dependency outputs. | PowerShell automation. |

## 2. Schema Authority
The **single source of truth** for machine validation is:
```
docs/transformation/prerequisites/framework-upgrade/validation/transform-prereqs.schema.json
```
The base prompt's embedded JSON Schema is intentionally minimal for readability; always validate output against the standalone schema.

## 3. When to Use Which Prompt
| Scenario | Use This File |
|----------|---------------|
| Reading conceptual checks & categories | `transform-prereqs-validation-prompt.md` |
| Running actual AI validation (Copilot Chat) | `transform-prereqs-validation-prompt-copilot.md` |
| Programmatic JSON validation (CI/script) | `transform-prereqs.schema.json` |
| Automating signal capture / staging | PowerShell scripts in `ai-foundry-agents/transform-agent/scripts/` |

## 4. Output Contract (Summary)
Top‑level JSON keys (fixed order):
```
metadata, overall, versionSupport, applicability, toolchain, repoHealth, projectModel,
dependencies, languageRuntime, breakingChanges, configurationHosting, observability,
security, dataTier, testing, featureFlags, performanceBaseline, automation, riskRegister,
readinessChecklist, agentInputs, postUpgradeValidation, documentation, rollback
```
Statuses: `pass | warn | fail | na`
Precedence (overall.status): `fail > warn > pass > na` (ignore optional items for escalation).

## 5. Copilot Prompt Modes
| Mode | Purpose | Notes |
|------|---------|-------|
| full | Complete evaluation | Use for initial baseline.
| diff | Only changed categories/items | Requires `previousResult` in signals.
| focus:<category|itemId> | Narrow evaluation | All others become `status=na`.
| remediation | Only non‑pass items | For triage & PR comments.
| summary | Category statuses + non‑pass items | Lightweight gating.
| packages-chunk:n/total | Subset of large dependency list | Merge with `merge-dependency-chunks.ps1`.

## 6. Quickstart (Manual Copilot Flow)
1. Run: `ai-foundry-agents/transform-agent/scripts/gather-prereq-signals.ps1` → produces `upgrade-readiness-history/signals-raw.txt`.
2. Run: `ai-foundry-agents/transform-agent/scripts/stage-prereq-prompt.ps1 -Mode full` → creates `out/prereq-prompt-*.txt` with signals injected.
3. Open Copilot Chat, paste prompt file contents (including markers), submit.
4. Save returned JSON as `upgrade-readiness-history/<runId>.full.json` and copy to `latest.json`.
5. Validate: `ai-foundry-agents/transform-agent/scripts/validate-prereqs.ps1 -InputJson upgrade-readiness-history/latest.json -Strict`.
6. For incremental changes, re-run with `-Mode diff` (script param) and include previous JSON content.

## 7. Signal Collection (What to Include)
Minimum recommended signals inside the `<<REPO_SIGNALS_BEGIN>>` block:
- SDK list: `dotnet --list-sdks`
- Build summary (Release)
- Test summary (pass/fail counts)
- Package list (top-level + truncated transitive)
- Representative `.csproj` entries (TargetFramework, PackageReference)
- Analyzer or security scan output (if available)
- Previous JSON result (for diff mode)

## 8. Large Dependency Sets Strategy
If >200 packages:
1. First run `summary` mode.
2. Request successive chunks: `packages-chunk:1/4`, `:2/4`, etc.
3. Merge with: `ai-foundry-agents/transform-agent/scripts/merge-dependency-chunks.ps1`.

## 9. Validation and Gating
Example gating logic (conceptual):
- Fail pipeline if `overall.status == fail`.
- Warn pipeline (non-block) if `overall.status == warn` but no critical categories (e.g., `security`, `toolchain`) are `fail`.
- Optionally require zero `fail` + no more than N `warn` to proceed to automated upgrade.

## 10. Change Control Rules
| Change Type | schemaVersion Bump | Notes |
|-------------|--------------------|-------|
| Add optional field | Minor (or patch if purely informational) | Update schema + docs.
| Add required field | Major | Provide migration guidance.
| Remove optional unused field | Minor | Note in change log.
| Enum narrowing/removal | Major | Breaking.
| New top-level category | Major (unless behind feature flag) | Add to prompts & schema.

## 11. Frequently Asked Questions
**Q: Why separate schema from the prompt?**  To prevent silent drift and enable programmatic validators.
**Q: What if Copilot returns prose + JSON?**  Reply: _“Return only valid JSON per schema—no extra text.”_
**Q: How to handle secret leakage?**  The prompt mandates redaction (`<redacted>`). Pre‑scrub logs before inclusion.
**Q: Do I need all categories every run?**  Full mode does; focus/diff/remediation can reduce cost.

## 12. Example Diff Mode Signals Block
```text
<<REPO_SIGNALS_BEGIN>>
previousResult: { ... trimmed previous JSON ... }
changes: Upgraded Serilog, added perf baseline load test metrics
buildOutput: Build succeeded 0 Error(s) 12 Warning(s)
packageListTop: (truncated)
<<REPO_SIGNALS_END>>
```

## 13. Repository Conventions
- History folder (create if not present): `upgrade-readiness-history/`
- Naming: `<YYYY-MM-DD-HHMMSS>.<mode>.json` + `latest.json`
- Never manually edit archived JSON after capture—produce a new run instead.

## 14. Automation Ideas
| Idea | Benefit |
|------|---------|
| GitHub Action to collect signals + artifact | Consistent evidence snapshot |
| Pre-commit hook to forbid editing history JSON | Ensures append-only audit trail |
| Dashboard aggregating readiness trend | Visual progression for stakeholders |
| SARIF generator for warnings/failures | Inline code scanning integration |

## 15. Maintenance Checklist (Before Bumping schemaVersion)
- [ ] Update standalone schema file
- [ ] Run validator script against sample outputs
- [ ] Update change log in base prompt (and/or dedicated CHANGELOG)
- [ ] Confirm Copilot prompt still accurate (modes, field names)
- [ ] Communicate required migration steps to dependent pipelines

## 16. Troubleshooting
| Symptom | Likely Cause | Resolution |
|---------|--------------|-----------|
| Missing categories in output | Prompt truncated or mode misused | Re-run in `full` mode. |
| Overall shows pass but a category fail exists | Prompt instructions not reinforced | Add precedence reminder & re-run. |
| Copilot hallucinated file names | Insufficient file list evidence | Include authoritative file list snippet. |
| Excessive JSON size | Very large package list | Use chunk modes + merge script. |

## 17. Next Extensions (Planned Placeholders)
- `supplyChain` (SBOM & provenance) – integrate CycloneDX scan
- `aotReadiness` – summarize Native AOT blockers
- `trimming` – aggregate ILLink warnings & safe suppression counts

## 18. License / Attribution
Internal draft © 2025. Update distribution policy before external publication.

---
**Quick Copy Snippet (add to PR template):**
```
### Upgrade Readiness
Overall Status: (pending)
Latest Report: upgrade-readiness-history/latest.json
Blocking Failures: (list if any)
Warnings Summary: (top 3)
```

---
For improvements or questions, open an issue referencing the relevant run ID and schemaVersion.
