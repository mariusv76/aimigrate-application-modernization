# Transformation Documentation Hub

**Last Updated:** 2025-10-23  
**Purpose:** Central repository for .NET and Java modernization patterns, upgrade strategies, knowledge base, and tooling guidance.

---

## 📖 Table of Contents

- [Overview](#overview)
- [Directory Structure](#directory-structure)
- [Getting Started](#getting-started)
- [Documentation Index](#documentation-index)
- [Contributing](#contributing)
- [Related Resources](#related-resources)

---

## Overview

This directory contains comprehensive documentation for modernizing and transforming .NET and Java applications, with primary focus on:

- **Framework Upgrades** (.NET 8 LTS → .NET 9 STS, Java version upgrades)
- **Azure Migration Strategies** (on-prem/AWS/GCP → Azure)
- **Modernization Patterns** (secrets externalization, observability, identity, messaging, storage)
- **Cross-Cloud Platform Migrations** (AWS Lambda→Azure Functions, EKS→AKS, GCP→Azure)
- **Testing & Validation** (establishing upgrade baselines, evidence generation)
- **Lessons Learned** (knowledge base of migration challenges and solutions)

This documentation supports:

- **Transformation Agents** (AI Foundry orchestration)
- **GitHub Copilot** (prompt-driven developer assistance)
- **Manual Migration Teams** (step-by-step playbooks)
- **Cross-Cloud Migrations** (AWS/GCP to Azure platform transitions)

### Supported Languages & Platforms

- **.NET** (.NET 8→9 upgrades, ASP.NET Core modernization, Azure SDK adoption)
- **Java** (Java LTS upgrades, Spring Boot modernization, Jakarta EE migrations)
- **Cross-Platform** (Docker, Kubernetes, serverless migrations)

### Migration Scenarios

- **Azure Modernization** - Refactor on-prem/legacy apps for Azure native services
- **AWS to Azure** - Lambda→Functions, EKS→AKS, S3→Blob, SQS→Service Bus
- **GCP to Azure** - Cloud Run→Container Apps, GKE→AKS, Cloud Functions→Azure Functions
- **Framework Upgrades** - .NET 8→9, Java version migrations, dependency modernization

---

## Directory Structure

```text
docs/transformation/
├── README.md                           ← You are here
├── architecture/                       ← Agent design & visual specs
│   ├── transformation-agent-design.md  ← Core agent architecture
│   └── diagrams/                       ← Architecture diagrams
├── prerequisites/                      ← Upgrade readiness requirements
│   └── framework-upgrade/              ← .NET version upgrade guidance
│       ├── transform-prereqs.md        ← .NET 8→9 prerequisites checklist
│       └── validation/                 ← Machine-readable validation
│           ├── transform-prereqs.schema.json            ← Authoritative JSON schema
│           ├── transform-prereqs-validation-prompt.md   ← Base validation prompt
│           ├── transform-prereqs-validation-prompt-copilot.md ← Copilot variant
│           └── README-prereq-validation.md              ← Operational guide
├── testing/                            ← Testing strategies & prompts
│   └── transform-test-generation-prompts.md ← Unit test baseline guide
├── strategies/                         ← Migration playbooks by scenario
│   └── kb/                             ← Knowledge base (strategy playbooks)
│       ├── KB-INDEX.md                 ← Master strategy index
│       ├── storage/                    ← Blob/Files migration (.NET/Java)
│       ├── email/                      ← SMTP → ACS Email (.NET/Java)
│       ├── secrets/                    ← Key Vault externalization (.NET/Java)
│       ├── sdk-replacement/            ← Proprietary → Azure SDK (.NET/Java)
│       ├── logging/                    ← OTEL & App Insights (.NET/Java)
│       ├── identity/                   ← Entra ID migration (.NET/Java)
│       ├── messaging/                  ← Service Bus patterns (.NET/Java)
│       ├── events/                     ← Event Grid integration (.NET/Java)
│       ├── config-externalization/     ← App Configuration (.NET/Java)
│       ├── framework-upgrade/          ← TFM & LTS strategies (.NET/Java)
│       ├── KB-Database-Migration.md    ← Database modernization (.NET/Java)
│       ├── KB-Caching-Migration.md     ← Azure Cache for Redis (.NET/Java)
│       ├── KB-Serverless-Migration.md  ← Lambda→Functions (.NET/Java)
│       └── KB-Container-Migration.md   ← EKS→AKS, Docker→Kubernetes
└── lessons/                            ← Real-world migration lessons
    └── migration-knowledge-base.md     ← Documented challenges & solutions
```

---

## Getting Started

### For .NET Developers

1. **Upgrade Planning:**
   - Start with [`prerequisites/framework-upgrade/transform-prereqs.md`](prerequisites/framework-upgrade/transform-prereqs.md)
   - Review readiness checklist (24 categories)
   - Run validation using [`validation/`](prerequisites/framework-upgrade/validation/) tools

2. **Test Baseline:**
   - Follow [`testing/transform-test-generation-prompts.md`](testing/transform-test-generation-prompts.md)
   - Generate unit tests with GitHub Copilot
   - Establish pre-migration evidence

3. **Migration Strategies:**
   - Browse [`strategies/kb/KB-INDEX.md`](strategies/kb/KB-INDEX.md)
   - Select relevant playbooks (storage, identity, messaging, etc.)
   - Apply step-by-step recipes for .NET

4. **Learn from Others:**
   - Check [`lessons/migration-knowledge-base.md`](lessons/migration-knowledge-base.md)
   - Review documented issues and solutions
   - Contribute your own findings

### For Java Developers

1. **Framework Assessment:**
   - Review [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md) Section 6.2 (Java Transformation Strategy)
   - Identify applicable modernization patterns
   - Check Java-specific migration scenarios

2. **Migration Strategies:**
   - Browse [`strategies/kb/KB-INDEX.md`](strategies/kb/KB-INDEX.md)
   - Focus on Java-specific implementations:
     - Storage (Azure Storage v12 SDK, Reactor patterns)
     - Messaging (Spring AMQP→Service Bus, ActiveMQ→Service Bus)
     - Secrets (Key Vault JCA provider, azure-identity)
     - Identity (LDAP→Entra ID, OAuth2/OIDC)
     - Database (Oracle→PostgreSQL, Managed Identity auth)
   - Apply OpenRewrite recipes where available

3. **Cross-Cloud Migrations:**
   - AWS to Azure: Lambda→Functions, EKS→AKS, S3→Blob, SQS→Service Bus
   - GCP to Azure: Cloud Run→Container Apps, GKE→AKS
   - Reference [`strategies/kb/serverless-migration/`](strategies/kb/serverless-migration/) and [`strategies/kb/container-migration/`](strategies/kb/container-migration/)

4. **Testing & Validation:**
   - Adapt .NET test generation patterns to Java (JUnit, AssertJ, Mockito)
   - Establish pre-migration test baseline
   - Validate post-migration functional equivalence

### For Transformation Agents

1. **Architecture Context:**
   - Read [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md)
   - Understand strategy mapping tables:
     - Section 6.1: .NET Transformation Strategy
     - Section 6.2: Java Transformation Strategy
     - Section 6.3: Cross-Cloud Platform Migrations
   - Review visual diagrams in [`architecture/diagrams/`](architecture/diagrams/)

2. **Orchestration:**
   - Index all KB playbooks from [`strategies/kb/`](strategies/kb/)
   - Support both .NET and Java language patterns
   - Consume validation schemas for gating
   - Generate PRs with evidence references

3. **Knowledge Augmentation:**
   - Query [`lessons/migration-knowledge-base.md`](lessons/migration-knowledge-base.md)
   - Surface relevant patterns during planning
   - Suggest proactive mitigations
   - Adapt strategies for cross-cloud scenarios (AWS→Azure, GCP→Azure)

---

## Documentation Index

### Core Guides

| Document | Purpose | Audience | Language |
|----------|---------|----------|----------|
| [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md) | Agent design, .NET & Java strategy tables, cross-cloud migrations, orchestration patterns | Architects, Agent Developers | Multi-language |
| [`prerequisites/framework-upgrade/transform-prereqs.md`](prerequisites/framework-upgrade/transform-prereqs.md) | .NET 8→9 prerequisites checklist (24 sections) | Developers, DevOps | .NET |
| [`testing/transform-test-generation-prompts.md`](testing/transform-test-generation-prompts.md) | GitHub Copilot unit test baseline guide | Developers | .NET (adaptable to Java) |
| [`strategies/kb/KB-INDEX.md`](strategies/kb/KB-INDEX.md) | Master index of migration playbooks | All | .NET & Java |
| [`lessons/migration-knowledge-base.md`](lessons/migration-knowledge-base.md) | Real-world migration lessons learned | All | Multi-language |

### Validation & Automation

| Document | Purpose | Audience |
|----------|---------|----------|
| [`prerequisites/framework-upgrade/validation/transform-prereqs.schema.json`](prerequisites/framework-upgrade/validation/transform-prereqs.schema.json) | Authoritative JSON Schema for validation output | CI/CD, Agents |
| [`prerequisites/framework-upgrade/validation/README-prereq-validation.md`](prerequisites/framework-upgrade/validation/README-prereq-validation.md) | Operational guide for validation workflow | DevOps, Developers |
| [`prerequisites/framework-upgrade/validation/transform-prereqs-validation-prompt-copilot.md`](prerequisites/framework-upgrade/validation/transform-prereqs-validation-prompt-copilot.md) | Copilot-optimized validation prompt with modes | Developers |

### Supporting Scripts

| Script | Location | Purpose |
|--------|----------|---------|
| `gather-prereq-signals.ps1` | `../../ai-foundry-agents/transform-agent/scripts/` | Collect upgrade readiness evidence |
| `stage-prereq-prompt.ps1` | `../../ai-foundry-agents/transform-agent/scripts/` | Assemble validation prompt with signals |
| `validate-prereqs.ps1` | `../../ai-foundry-agents/transform-agent/scripts/` | Validate JSON against schema |
| `merge-dependency-chunks.ps1` | `../../ai-foundry-agents/transform-agent/scripts/` | Merge chunked dependency evaluations |

---

## Contributing

### Documentation Standards

- **Markdown Lint:** All `.md` files must pass `markdownlint` validation
- **Schema Versioning:** Changes to `transform-prereqs.schema.json` require `schemaVersion` bump
- **KB Template:** New lessons in `lessons/` must follow structured template (migration path, issue, root cause, solution, docs link, discovery date)
- **Cross-References:** Use relative paths from document location (e.g., `../strategies/kb/storage/README.md`)

### Adding New Content

1. **New Strategy Playbook:**
   - Add to `strategies/kb/<category>/README.md`
   - Update `strategies/kb/KB-INDEX.md`
   - Link from `architecture/transformation-agent-design.md` strategy tables

2. **New Migration Lesson:**
   - Follow template in `lessons/migration-knowledge-base.md`
   - Include: migration path, issue, root cause, solution, official docs, discovery date
   - Tag with technology keywords for searchability

3. **New Validation Category:**
   - Update `transform-prereqs.schema.json` with new top-level category
   - Bump `schemaVersion` (major if breaking, minor if additive)
   - Add to `transform-prereqs.md` checklist
   - Update Copilot prompts if needed

### Review Process

- All changes require markdown lint pass
- Schema changes require validation script updates
- New playbooks require architecture doc reference
- Lessons require real-world evidence (not hypothetical)

---

## Related Resources

### Internal Repositories

- **Transformation Agent Code:** `../../ai-foundry-agents/transform-agent/` (operational code, bandish orchestration, automation scripts)
- **Automation Scripts:** `../../ai-foundry-agents/transform-agent/scripts/` (PowerShell helpers)
- **Platform Infrastructure:** `../../platform/` (deployment templates)

### External Documentation

- [Microsoft .NET Upgrade Guide](https://learn.microsoft.com/en-us/dotnet/core/porting/)
- [Azure Architecture Center](https://learn.microsoft.com/en-us/azure/architecture/)
- [Azure Well-Architected Framework](https://learn.microsoft.com/en-us/azure/well-architected/)
- [GitHub Copilot Best Practices](https://docs.github.com/en/copilot)
- [OpenTelemetry .NET](https://opentelemetry.io/docs/languages/net/)

### Community & Support

- Open issues in repository for documentation feedback
- Tag with `documentation` or `transformation` labels
- Reference `runId` or `schemaVersion` when reporting validation issues

---

### Quick Links by Scenario

### ".NET: I need to upgrade .NET 8 to .NET 9"

1. Read: [`prerequisites/framework-upgrade/transform-prereqs.md`](prerequisites/framework-upgrade/transform-prereqs.md)
2. Validate: [`validation/README-prereq-validation.md`](prerequisites/framework-upgrade/validation/README-prereq-validation.md)
3. Test: [`testing/transform-test-generation-prompts.md`](testing/transform-test-generation-prompts.md)

### "Java: I'm upgrading to a newer Java LTS version"

1. Review: [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md) Section 6.2
2. Playbooks: [`strategies/kb/framework-upgrade/`](strategies/kb/framework-upgrade/)
3. Tools: OpenRewrite recipes, dependency updates (pom.xml/build.gradle)

### "I'm migrating from AWS to Azure"

1. Browse: [`strategies/kb/KB-INDEX.md`](strategies/kb/KB-INDEX.md)
2. Focus on:
   - **Compute**: `serverless-migration/` (Lambda→Functions), `container-migration/` (EKS→AKS)
   - **Storage**: `storage/` (S3→Blob)
   - **Messaging**: `messaging/` (SQS→Service Bus, ActiveMQ→Service Bus)
   - **Secrets**: `secrets/` (Secrets Manager→Key Vault)
   - **Identity**: `identity/` (IAM→Managed Identity)
3. Reference: [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md) Section 6.3 (Cross-Cloud Platform Migrations)

### "I'm migrating from GCP to Azure"

1. Platform Migrations:
   - Cloud Run → Container Apps: [`strategies/kb/serverless-migration/`](strategies/kb/serverless-migration/)
   - GKE → AKS: [`strategies/kb/container-migration/`](strategies/kb/container-migration/)
   - Cloud Functions → Azure Functions: [`strategies/kb/serverless-migration/`](strategies/kb/serverless-migration/)
2. Review: Section 6.3 in [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md)

### "I hit a weird migration issue"

1. Search: [`lessons/migration-knowledge-base.md`](lessons/migration-knowledge-base.md)
2. If not found: Document it using the template
3. Share: Contribute back to knowledge base

### "I'm building a transformation agent"

1. Start: [`architecture/transformation-agent-design.md`](architecture/transformation-agent-design.md)
2. Index: All KB playbooks in [`strategies/kb/`](strategies/kb/)
3. Language Support: Implement both .NET and Java transformation patterns
4. Cross-Cloud: Support AWS→Azure and GCP→Azure migrations
5. Integrate: Validation schemas from [`validation/`](prerequisites/framework-upgrade/validation/)

---

## Strategy Coverage by Language

### .NET Strategies

| Category | Playbooks | Use Cases |
|----------|-----------|-----------|
| **Storage** | `storage/` | File I/O→Blob Storage, Local files→Azure Files, S3→Blob |
| **Messaging** | `messaging/` | MSMQ/RabbitMQ→Service Bus, SQS→Service Bus |
| **Identity** | `identity/` | AD→Entra ID, Custom auth→Entra ID |
| **Secrets** | `secrets/` | Config secrets→Key Vault, AWS Secrets Manager→Key Vault |
| **Observability** | `logging/` | log4net/Serilog→OTEL, Windows Event Log→App Insights |
| **Communication** | `email/` | SmtpClient→ACS Email SDK |
| **Configuration** | `config-externalization/` | Connection strings→App Config + Key Vault |
| **Data** | `KB-Database-Migration.md` | SQL Server→Azure SQL, legacy DB→managed services |
| **Caching** | `KB-Caching-Migration.md` | Local cache→Azure Cache for Redis |
| **Compute** | `KB-Serverless-Migration.md`, `KB-Container-Migration.md` | Lambda→Functions, Docker→AKS |

### Java Strategies

| Category | Playbooks | Use Cases |
|----------|-----------|-----------|
| **Storage** | `storage/` | File I/O→Blob Storage (v12 SDK, Reactor), S3→Blob |
| **Messaging** | `messaging/` | Spring RabbitMQ→Service Bus, ActiveMQ→Service Bus, SQS→Service Bus |
| **Identity** | `identity/` | LDAP→Entra ID (OAuth2/OIDC), AD→Entra ID |
| **Secrets** | `secrets/` | JKS certificates→Key Vault JCA, AWS Secrets→Key Vault, plaintext→Key Vault |
| **Observability** | `logging/` | SLF4J + OTEL instrumentation, file logging→console |
| **Communication** | `email/` | javax.mail→ACS Email SDK |
| **Configuration** | `config-externalization/` | Connection strings→Managed Identity, Spring Cloud Azure config |
| **Data** | `KB-Database-Migration.md` | Oracle→PostgreSQL, MySQL/Cassandra/MongoDB→managed services |
| **Compute** | `KB-Serverless-Migration.md`, `KB-Container-Migration.md` | Lambda→Functions, EKS→AKS, Docker Compose→AKS |

### Cross-Platform Strategies

| Category | Playbooks | Platforms |
|----------|-----------|-----------|
| **Serverless** | `KB-Serverless-Migration.md` | AWS Lambda→Functions, GCP Cloud Functions→Functions, App Runner→Container Apps |
| **Container Orchestration** | `KB-Container-Migration.md` | EKS→AKS, GKE→AKS, Docker Compose→AKS, Fargate→Container Apps |
| **Compute** | TBD | EC2→VM, Elastic Beanstalk→App Service |

---

**Maintained by:** AI Migration Engineering  
**Feedback:** Open issues with `documentation` tag  
**Last Major Update:** 2025-10-23 (Added Java strategies, cross-cloud migrations)  
**Languages Supported:** .NET, Java, Cross-Platform (Docker, Kubernetes)
