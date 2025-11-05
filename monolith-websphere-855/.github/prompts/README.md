# Migration Task Prompts - Index

This directory contains comprehensive prompts for each migration task in the Customer Order Services modernization project.

## Overview

Each prompt file provides detailed, step-by-step instructions for an AI coding agent to execute a specific migration task. The prompts follow a consistent structure and include:

- Task overview and dependencies
- Detailed implementation steps
- Progress tracking requirements
- Validation criteria
- Documentation requirements
- Success criteria

## Task Sequence

The tasks must be executed in order due to dependencies:

### Phase 0: Foundation
No task prompts - manual setup and assessment

### Phase 1: Testing Foundation & Framework Upgrade

| Task | Prompt File | Description | Estimated Effort |
|------|-------------|-------------|------------------|
| **TASK-001** | [migration-prompt.md](migration-prompt.md) | Java 8 → Java 17 Upgrade | 40h → 20h (AI) |
| **TASK-002** | [TASK-002-test-foundation-prompt.md](TASK-002-test-foundation-prompt.md) | Build comprehensive test suite (unit + integration) | 120h → 52h (AI) |
| **TASK-003** | [TASK-003-jakarta-migration-prompt.md](TASK-003-jakarta-migration-prompt.md) | Migrate javax.* → jakarta.* namespace | 60h → 25h (AI) |
| **TASK-004** | [TASK-004-jackson-upgrade-prompt.md](TASK-004-jackson-upgrade-prompt.md) | Upgrade Jackson & replace IBM JSON | 90h → 40h (AI) |

**Phase 1 Total:** 310h → 137h (56% reduction)

### Phase 2: Azure Integration & Cloud-Native Patterns

| Task | Prompt File | Description | Estimated Effort |
|------|-------------|-------------|------------------|
| **TASK-005** | [TASK-005-database-migration-prompt.md](TASK-005-database-migration-prompt.md) | Migrate DB2 → PostgreSQL | 140h → 70h (AI) |
| **TASK-006** | [TASK-006-azure-integration-prompt.md](TASK-006-azure-integration-prompt.md) | Integrate Azure services (Entra ID, Key Vault, App Config) | 496h → 236h (AI) |

**Phase 2 Total:** 636h → 306h (52% reduction)

### Phase 3: Containerization & Deployment

| Task | Prompt File | Description | Estimated Effort |
|------|-------------|-------------|------------------|
| **TASK-007** | [TASK-007-containerization-prompt.md](TASK-007-containerization-prompt.md) | Containerize & deploy to Azure Container Apps | 564h → 277h (AI) |

**Phase 3 Total:** 564h → 277h (51% reduction)

## Total Migration Effort

| Metric | Manual | AI-Assisted | Savings |
|--------|--------|-------------|---------|
| **Total Hours** | 1,510h | 720h | 790h (52%) |
| **Timeline** | ~19 weeks | ~9 weeks | ~10 weeks |
| **Cost** | $151,000 | $72,000 | $79,000 (52%) |

*Assumes 2 developers @ 40 hrs/week @ $100/hr*

## Using These Prompts

### For AI Coding Agents

1. **Read the prompt file completely** before starting
2. **Follow the exact sequence** of steps outlined
3. **Create all required tracking files** (plan, progress, todos)
4. **Commit frequently** with proper commit messages
5. **Update progress tracking** after every significant step
6. **Generate comprehensive documentation** at completion
7. **Present findings** to user for approval before proceeding

### For Human Developers

1. **Review the prompt** to understand task scope
2. **Use as a checklist** to track progress
3. **Follow best practices** outlined in each prompt
4. **Leverage AI tools** (GitHub Copilot) as suggested
5. **Document deviations** from the plan in progress files

## Prompt Structure

Each task prompt follows this structure:

```
1. Mission Statement
2. Task Overview
   - Task ID
   - Priority
   - Dependencies
   - Estimated Effort
   - Success Criteria
3. Detailed Instructions
   Phase 1: Planning & Preparation
   Phase 2: Branch & Progress Setup
   Phase 3: Implementation
   Phase 4: Validation & Testing
   Phase 5: Documentation & Summary
   Phase 6: Handoff
4. Work Execution Guidelines
   - Branch Management
   - Progress Tracking
   - Todo Management
   - Commit Message Format
   - Error Handling
   - File Organization
5. Expected Deliverables
6. Success Criteria (Checklist)
7. Constraints & Guidelines
8. Tool-Specific Tips (GitHub Copilot, OpenRewrite, etc.)
9. Start Command
```

## File Organization

Each task creates the following structure:

```
.transformation/
├── tasks/
│   └── TASK-XXX-plan.md
├── progress/
│   └── TASK-XXX-progress.md
├── todos/
│   └── TASK-XXX-todos.md
├── summaries/
│   └── TASK-XXX-summary.md
└── diffs/
    └── TASK-XXX/
        ├── file1.diff.md
        ├── file2.diff.md
        └── ...
```

## Branch Strategy

Each task creates a dedicated branch:

```
main
├── migration/task-001-java-17-upgrade
│   └── migration/task-002-test-foundation
│       └── migration/task-003-jakarta-migration
│           └── migration/task-004-jackson-upgrade
│               └── migration/task-005-database-migration
│                   └── migration/task-006-azure-integration
│                       └── migration/task-007-containerization
```

Branches are created sequentially, each based on the completed previous task.

## Success Criteria

A task is considered complete when:

- [ ] All implementation steps executed
- [ ] All modules compile successfully
- [ ] All tests pass
- [ ] Progress file shows 100% completion
- [ ] All todos checked off
- [ ] Summary document created
- [ ] Diff documentation generated
- [ ] User review completed and approved

## Support & Questions

For questions about:
- **Prompt content:** Review the transformation documentation in `.transformation/`
- **Azure services:** See `.transformation/AZURE_INTEGRATION_GUIDE.md`
- **Framework migration:** See `.transformation/FRAMEWORK_MIGRATION_GUIDE.md`
- **Platform migration:** See `.transformation/PLATFORM_MIGRATION_GUIDE.md`

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-04 | Initial creation of all task prompts |

---

**Note:** These prompts are designed for AI coding agents but can be used by human developers as comprehensive task checklists. Each prompt is self-contained and includes all necessary context and instructions.
