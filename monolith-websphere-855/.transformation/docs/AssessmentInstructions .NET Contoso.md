# Generate Comprehensive .NET Migration Assessment Documentation

Create a complete .NET Framework to .NET 8 migration assessment for the project with the following specifications:

## Context & Project Overview

- **Application**: ContosoUniversity
- **Current State**: .NET Framework 4.8, ASP.NET MVC 5, Entity Framework Core 3.1.32
- **Target State**: .NET 8, ASP.NET Core 8, Azure cloud-native deployment

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
3. Recommended Action Plan
4. Success Metrics
5. Cost Estimates with breakdown tables
6. Risk Assessment matrix
7. Decision Points with 4 migration options
8. Next Steps timeline

### 2. Detailed Technical Assessment (`.transformation\MIGRATION_ASSESSMENT.md`)

Generate comprehensive technical documentation with:

- Complete .NET upgrade path analysis
- Azure modernization strategy with service mapping
- Build system evaluation (cross-platform limitations)
- Unit testing assessment (currently 0% coverage)
- Security and compliance considerations
- Performance impact analysis

**Include specific sections:**

- Dependency analysis with package upgrade paths
- Dependency migration steps (for instance EF Core)
- Azure service comparisons and recommendations
- Incompatible services/components migration strategy

### 3. Step-by-Step Migration Guide (`.transformation\{strategy}-MIGRATION_GUIDE.md`)

For each of the strategies Generate as detailed implementation guide assuming GHCP assistance:

- Estimated timeline broken into phases (2 weeks per phase)
- Specific PowerShell/CLI commands for each step
- Before/after code examples for all conversions
- Azure resource creation scripts
- Configuration file examples (appsettings.json, Dockerfile, etc.)
- Migration Tooling to use (Azure Migrate Modernization / Bandish / GHCP)
- Testing and validation procedures
- Troubleshooting section with common issues

**Phase Structure:**

- Phase 0: Pre-Migration (Environment setup, EF Core upgrade)
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

## Content Requirements

### Technical Accuracy

- Use actual Azure service names and pricing (as of the current month)
- Include real PowerShell/CLI commands that work
- Provide working code examples for conversions
- Reference official Microsoft documentation links
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
- DO reference the "docs\transformation\architecture\transformation-agent-design.md" section 6. Transformation Strategy by Use Case/strategy for available guidance, apply the reference to the KB if aligned
- DO create TODO's for you to track the progress
- DO add a final TODO to execute the vscode command
  
```
vscode.commands.executeCommand('archTransform.saveAssessmentReport', reportContent)
```

Where reportContent is a summary of what you achieved
