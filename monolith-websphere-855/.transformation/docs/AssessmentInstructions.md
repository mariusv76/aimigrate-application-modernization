# Generate Comprehensive Java Assessment Documentation

Create a complete Java migration assessment for the project with the following specifications:

1. Review the transformation design document in `.transformation/docs/transformation/architecture/transformation-agent-design.md` section 6 for applicable transformation patterns and strategies.
2. Ensure alignment with the Azure Well-Architected framework
3. Ensure Authentication is accounted for using Microsoft Entra ID
4. Ensure OpenTelemetry is used for logging and diagnostics
5. Ensure Configuration is extenalized (both app configuration and usage of key vault)

## Context & Project Overview

- **Application**: Customer Order Project

## Required Files to Generate

### 1. Executive Summary (`.transformation\ASSESSMENT_SUMMARY.md`)

Generate a comprehensive executive summary with:

- Quick assessment results table with status indicators (✅❌⚠️)
- Critical findings categorized by severity (🔴⚠️)
- Phase-by-phase migration timeline
- Cost estimates: Development
- Risk assessment matrix with mitigation strategies
- Decision framework (YES/MAYBE/NO scenarios)
- Success metrics and ROI analysis
- Immediate next steps with checkboxes

**Structure:**

1. Quick Assessment Results (5 sections: Framework Upgrade, Package dependencies (Version and Vulnerabilities check), Azure Strategy, Build Verification, Unit Tests)
2. Critical Findings with color-coded priorities
3. Recommended Action Plan with justification and references to KB's/documents in order of priority considering dependencies (i.e. Platform first, then packages, then externalization)
4. Success Metrics
5. Cost Estimates with breakdown tables
6. Risk Assessment matrix
7. Decision Points with migration options
8. Next Steps timeline

### 2. Detailed Technical Assessment (`.transformation\MIGRATION_ASSESSMENT.md`)

Generate comprehensive technical documentation with:

- Complete Java upgrade path analysis
- Azure modernization strategy with service mapping
- Build system evaluation (cross-platform limitations)
- Unit testing assessment (currently 0% coverage)
- Security and compliance considerations
- Performance impact analysis

**Include specific sections:**

- Dependency analysis with package upgrade paths
- Dependency migration steps (for instance Sprint)
- Azure service comparisons and recommendations
- Incompatible services/components migration strategy

### 3. Step-by-Step Migration Guide (`.transformation\{component}-MIGRATION_GUIDE.md`)

For each of the components Generate as detailed implementation guide assuming GHCP assistance:

- Estimated timeline broken into phases (2 weeks per phase)
- Specific PowerShell/CLI commands for each step
- Before/after code examples for all conversions
- Azure resource creation scripts
- Configuration file examples (appsettings.json, Dockerfile, etc.)
- Migration Tooling to use (Azure Migrate Modernization / Bandish / GHCP) from the design document
- Testing and validation procedures
- Troubleshooting section with common issues

**Phase Structure:**

- Phase 0: Pre-Migration (Environment setup, Framework)
- Phase 1: Testing Foundation (Unit tests, coverage)
- Phase 2: Framework Migration
- Phase 3: Azure Integration (Cloud services)
- Phase 4: Deployment & Testing (Containerization, CI/CD)

### 4. Cost Analysis (`.transformation\DETAILED_COST_ESTIMATES.md`)

Generate detailed financial analysis with:

- Hour-by-hour development effort breakdown with and without AI Assistance
- Task categorization by AI automation potential
- Azure service pricing tiers and recommendations
- 3-year TCO comparison (on-premises vs Azure)
- Break-even analysis
- Cost optimization strategies
- Reserved instance recommendations
- Environment-specific cost scenarios (dev/staging/prod)
- Quality assurance procedures for AI-generated code
- Manual Migration Tasks
- Effort reduction calculations

Indicate how this can be adjusted for actual workload characteristics

## Content Requirements

### Technical Accuracy

- Use actual Azure service names and pricing (as of the current month)
- Include real PowerShell/CLI commands that work
- Provide working code examples for conversions
- Reference official Microsoft documentation links or Java documentation
- Use correct patterns for upgraded dependencies

### Financial Precision

- Use a $100 hourly rate for developers
- Include actual Azure pricing tiers
- Account for regional variations
- Consider scaling scenarios
- Include hidden costs (data transfer, operations)

### Timeline Realism

- Account for learning curves
- Include testing and validation time
- Add contingency buffers (10-15%)
- Consider team size variations (2 developers)
- Include project management overhead

### Risk Management

- Identify technical blockers
- Plan mitigation strategies
- Include rollback procedures
- Consider business continuity
- Address compliance requirements

## Formatting Standards

### Document Structure

- Use clear hierarchical headings (##, ###, ####)
- Include table of contents for long documents
- Add cross-references between documents
- Use consistent status indicators (✅❌⚠️🔴)

### Code Examples

- Provide both before/after samples
- Include full file paths in comments
- Use proper syntax highlighting
- Add explanatory comments
- Show configuration examples

### Tables and Data

- Use markdown tables for comparisons
- Include units in cost calculations
- Show ranges for estimates
- Use consistent currency formatting
- Add footnotes for assumptions

### Visual Elements

- Create ASCII architecture diagrams
- Use consistent emoji for status indicators
- Include progress checkboxes for action items
- Format commands in code blocks
- Highlight important warnings

## Quality Standards

### Completeness

- Cover all aspects of migration journey
- Address both technical and business concerns
- Include troubleshooting and support information
- Provide multiple migration path options
- Consider team skill level variations

### Accuracy

- Verify all technical procedures
- Test provided commands and scripts
- Validate cost calculations
- Check Azure service capabilities
- Confirm .NET 8 feature compatibility

### Usability

- Write for different audience levels (executives, developers, ops)
- Provide clear action items
- Include decision frameworks
- Offer multiple implementation options
- Enable incremental adoption

Important

- DO Generate all files with consistent cross-referencing, maintaining the executive summary as the entry point that links to detailed technical documents for implementation guidance.
- DO NOT modify any source code files unless approved or instructed by the user
- DO NOT try to create a specific file in one go, break the creation up into several tasks according to the main section. Write each section then continue with the next. Once the file is complete evaluate the correctness and coherance of the content and adjust if necassary
- DO reference the transformation design document at `.transformation/docs/transformation/architecture/transformation-agent-design.md` for transformation patterns and strategies
- DO create TODO's for you to track the progress
