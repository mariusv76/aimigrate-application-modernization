# Transformation Agent Diagram Specifications

This document provides specifications for all diagrams referenced in the Transformation Agent Design Document. Each diagram includes both Mermaid syntax (for markdown rendering) and a description for creating enhanced versions in Draw.io or other tools.

## Table of Contents

- [Transformation Agent Diagram Specifications](#transformation-agent-diagram-specifications)
  - [Table of Contents](#table-of-contents)
  - [1. High-Level Architecture Diagram](#1-high-level-architecture-diagram)
  - [2. Agent Interaction Flow \[WIP\]](#2-agent-interaction-flow-wip)
    - [2.1 Assessment \& Planning Phase](#21-assessment--planning-phase)
    - [2.2 Migration Execution \& Validation Phase](#22-migration-execution--validation-phase)
  - [3. Transformation Strategy Decision Tree \[WIP\]](#3-transformation-strategy-decision-tree-wip)
  - [4. Tool Orchestration Architecture](#4-tool-orchestration-architecture)
  - [5. Evidence \& Lineage Flow](#5-evidence--lineage-flow)
  - [6. Security \& Identity Model](#6-security--identity-model)

---

## 1. High-Level Architecture Diagram

**Purpose:** Show overall agent architecture and Azure service integration

**Design Doc Reference:** Section 3.1 - Platform Architecture

```mermaid
---
config:
  layout: elk
---
flowchart LR
 subgraph subGraph0["Client Layer"]
        IDE["VS Code / IDE"]
        ExtTools["External Tools<br>Visual Studio, Bandish"]
  end
 subgraph subGraph1["Agent Runtime"]
        Agent["AI Foundry Agent Runtime"]
        Tools["Tool Orchestrator"]
        MCP["MCP Server<br>Progress Tracking"]
  end
 subgraph subGraph2["AI & Intelligence"]
        AOAI["Azure OpenAI / AI Foundry"]
        KBStore["Knowledge Base<br>Azure Blob Storage"]
        IntakeStorage["Intake Agent Storage<br>ASR Reports"]
  end
 subgraph subGraph3["Data & State"]
        Cosmos["Cosmos DB<br>Thread Storage & Workload Metadata"]
        KeyVault["Azure Key Vault<br>Secrets &amp; Config"]
        BlobStorage["Azure Blob Storage<br>Plans & Evidence"]
  end
 subgraph Integration["Integration"]
        GitHub["GitHub Repos"]
        DevOps["Azure DevOps"]
  end
 subgraph Validation["Validation & Quality"]
        QualityGates["Quality Gates<br>Build, Test, Security"]
        Standards["Standards Profile<br>ASR Compliance"]
  end
 subgraph Observability["Observability"]
        AppInsights["Application Insights"]
        LogAnalytics["Log Analytics Workspace"]
        OTEL["OpenTelemetry Collector"]
  end
 subgraph subGraph6["Security & Networking"]
        MI["Managed Identity"]
        EntraID["Entra ID"]
        VNet["VNet + Private Endpoints"]
        NSG["Network Security Groups"]
  end
    IDE --> Agent
    ExtTools --> MCP
    Agent --> Tools & AOAI & Cosmos & KeyVault & OTEL & MI & VNet & MCP
    Agent --> IntakeStorage & BlobStorage
    Tools --> KBStore
    MCP --> Cosmos
    subGraph1 -.-> Integration
    Agent --> QualityGates & Standards
    QualityGates --> GitHub
    OTEL --> AppInsights & LogAnalytics
    MI --> EntraID
    VNet --> NSG
    style Agent fill:#0078d4,stroke:#005a9e,color:#fff
    style MCP fill:#ff922b,stroke:#e67700
    style AOAI fill:#BBDEFB,stroke:#2962FF
    style KBStore fill:#C8E6C9,stroke:#00C853
    style Cosmos fill:#E1BEE7,stroke:#AA00FF
    style QualityGates fill:#51cf66,stroke:#2b8a3e
    style GitHub fill:#24292e,stroke:#000,color:#fff
```

---

## 2. Agent Interaction Flow [WIP]

**Purpose:** Visualize the complete agent workflow from project intake through migration execution to evidence publication

**Design Doc Reference:** Section 3.2 - Application Architecture

### 2.1 Assessment & Planning Phase

```mermaid
sequenceDiagram
    actor User
    participant IDE as VS Code/IDE
    participant TA as Transformation Agent
    participant Cosmos as CosmosDB
    participant Intake as Intake Agent Storage
    participant ASR as Assessment Report (ASR)
    participant Repo as Git Repository
    participant KB as Knowledge Base
    participant Blob as Azure Blob Storage

    autonumber
    User->>IDE: Request TA to start transformation
    IDE->>TA: Initiate transformation session
    
    TA->>User: Request project/workload ID
    User-->>TA: Provide workload ID
    
    TA->>Cosmos: Retrieve workload metadata
    Cosmos-->>TA: Workload info (repos, language, deps)
    
    TA->>Intake: Retrieve ASR for workload
    Intake-->>TA: ASR metadata & location
    
    alt ASR Status: Completed/Ready
        TA->>Intake: Download ASR file
        Intake-->>TA: ASR JSON/Markdown
        TA->>TA: Store ASR in agent workspace
    else ASR Not Ready
        TA->>User: ASR not ready, cannot proceed
    end
    
    TA->>Cosmos: Get Git repo location(s)
    Cosmos-->>TA: Repo URLs & credentials
    
    TA->>Repo: Clone repository(ies)
    Repo-->>TA: Source code
    
    TA->>TA: Validate buildability
    TA->>TA: Run existing tests (baseline)
    TA->>TA: CVE scan dependencies
    
    TA->>TA: Analyze code using ASR guidance
    TA->>KB: Match code patterns to strategies
    KB-->>TA: Applicable KB playbooks
    TA->>ASR: Validate KB selections vs. standards profile
    ASR-->>TA: Mandatory/optional compliance rules
    
    TA->>User: Present migration strategy (markdown)
    Note over TA,User: Strategy includes:<br/>- KB playbooks selected<br/>- Transformations planned<br/>- Effort estimate<br/>- Risk assessment
    
    loop Plan Refinement
        User->>TA: Request adjustments
        TA->>KB: Validate adjustments against KB
        TA->>User: Updated strategy
    end
    
    User->>TA: Accept migration plan
    
    TA->>TA: Validate plan feasibility
    TA->>TA: Check for conflicts
    TA->>TA: Generate effort estimate
    
    TA->>TA: Create rollback plan
    
    TA->>Blob: Save migration plan
    TA->>Blob: Save rollback plan
    Note over Blob: Stored in project container
    
    TA->>TA: Generate tool-specific scripts
    Note over TA: Creates:<br/>- VS Code tasks.json<br/>- Bandish YAML<br/>- Migration checklist
    
    TA->>Repo: Update copilot-instructions.md
    Note over Repo: Includes:<br/>- Company guidance from ASR<br/>- MCP status reporting<br/>- KB article references
    
    TA->>User: Migration plan ready - proceed to execution
```

### 2.2 Migration Execution & Validation Phase

```mermaid
sequenceDiagram
    actor User
    participant IDE as VS Code/IDE
    participant TA as Transformation Agent
    participant MCP as TA MCP Server
    participant Cosmos as CosmosDB
    participant Tools as Migration Tools
    participant Repo as Git Repository
    participant ASR as Assessment Report (ASR)
    participant QG as Quality Gates
    participant Blob as Azure Blob Storage

    autonumber 20
    Note over User,Tools: Continuing from Assessment Phase...<br/>Migration plan is ready
    
    User->>Tools: Execute migration tasks
    Note over User,Tools: User works in:<br/>- VS Code<br/>- Visual Studio<br/>- Bandish<br/>- Other tools
    
    loop Migration Execution
        Tools->>MCP: Report task progress
        MCP->>Cosmos: Save progress checkpoint
        MCP->>TA: Update completion status
        TA->>IDE: Display progress
    end
    
    User->>TA: Migration complete
    TA->>User: Request reviewed PR(s)
    User-->>TA: Select PR(s) for validation
    
    TA->>Repo: Retrieve PR diffs
    Repo-->>TA: Code changes
    
    TA->>TA: Perform diff analysis
    TA->>ASR: Validate migration addressed ASR findings
    ASR-->>TA: Compliance validation results
    
    TA->>QG: Run post-migration validation
    QG->>Repo: Build migrated code
    QG->>Repo: Run tests
    QG->>QG: Security scan
    QG->>QG: Performance check
    QG-->>TA: Validation results
    
    TA->>TA: Generate upgrade report
    Note over TA: Report includes:<br/>- Migration summary<br/>- ASR findings addressed<br/>- Test results<br/>- Code coverage delta<br/>- Security findings
    
    TA->>TA: Package evidence artifacts
    Note over TA: Evidence bundle:<br/>- Bill of Materials (BoM)<br/>- RBAC requirements<br/>- Dependency list<br/>- Test results<br/>- Upgrade report
    
    TA->>User: Display upgrade report
    
    TA->>Blob: Publish evidence to storage
    TA->>Cosmos: Update project status
    Cosmos-->>TA: Status updated
    
    TA->>User: Notify stakeholders
    Note over User: Transformation complete!<br/>Evidence published
```

---

## 3. Transformation Strategy Decision Tree [WIP]

**Purpose:** Help users identify which KB playbook applies to their scenario

**Design Doc Reference:** Section 6 - Transformation Strategy by Use Case

```mermaid
---
config:
  layout: elk
---
graph LR
    Start([Start: Code Pattern Detected])
    
    Start --> LangChoice{Language?}
    
    LangChoice -->|.NET| DotNetDomain{Domain?}
    LangChoice -->|Java| JavaDomain{Domain?}
    
    DotNetDomain -->|Application Layer| DotNetApp{Pattern Type?}
    DotNetDomain -->|Integration & Messaging| DotNetInteg{Pattern Type?}
    DotNetDomain -->|Data & Configuration| DotNetData{Pattern Type?}
    
    JavaDomain -->|Application Layer| JavaApp{Pattern Type?}
    JavaDomain -->|Integration & Messaging| JavaInteg{Pattern Type?}
    JavaDomain -->|Data & Configuration| JavaData{Pattern Type?}
    
    DotNetApp -->|File I/O| StorageDotNet[Storage Abstraction KB<br/>.NET]
    DotNetApp -->|SMTP Client| EmailDotNet[Email Modernization KB<br/>.NET]
    DotNetApp -->|Plaintext Secrets| SecretsDotNet[Secrets Externalization KB<br/>.NET]
    DotNetApp -->|Proprietary SDK| SDKDotNet[SDK Replacement KB<br/>.NET]
    DotNetApp -->|Console Logging| LoggingDotNet[Logging & Observability KB<br/>.NET]
    DotNetApp -->|LDAP/AD Auth| IdentityDotNet[Identity Migration KB<br/>.NET]
    
    DotNetInteg -->|S3 Client| StorageDotNet
    DotNetInteg -->|SQS Usage| MessagingDotNet[Messaging Modernization KB<br/>.NET]
    DotNetInteg -->|CloudWatch Events| EventsDotNet[Events Modernization KB<br/>.NET]
    
    DotNetData -->|Connection Strings| ConfigDotNet[Configuration Externalization KB<br/>.NET]
    DotNetData -->|Unsupported Framework| FrameworkDotNet[Framework Upgrade KB<br/>.NET]
    
    JavaApp -->|File I/O| StorageJava[Storage Abstraction KB<br/>Java]
    JavaApp -->|javax.mail| EmailJava[Email Modernization KB<br/>Java]
    JavaApp -->|Plaintext Secrets| SecretsJava[Secrets Externalization KB<br/>Java]
    JavaApp -->|Proprietary SDK| SDKJava[SDK Replacement KB<br/>Java]
    JavaApp -->|System.out.println| LoggingJava[Logging & Observability KB<br/>Java]
    JavaApp -->|LDAP/Kerberos| IdentityJava[Identity Migration KB<br/>Java]
    
    JavaInteg -->|S3 Client| StorageJava
    JavaInteg -->|SQS Usage| MessagingJava[Messaging Modernization KB<br/>Java]
    JavaInteg -->|CloudWatch Events| EventsJava[Events Modernization KB<br/>Java]
    
    JavaData -->|Connection Strings| ConfigJava[Configuration Externalization KB<br/>Java]
    JavaData -->|Unsupported JDK| FrameworkJava[Framework Upgrade KB<br/>Java]
    
    style Start fill:#0078d4,stroke:#005a9e,color:#fff
    style StorageDotNet fill:#50e6ff,stroke:#0078d4
    style StorageJava fill:#50e6ff,stroke:#0078d4
    style EmailDotNet fill:#fcd116,stroke:#d39d09
    style EmailJava fill:#fcd116,stroke:#d39d09
    style SecretsDotNet fill:#ff6b6b,stroke:#c92a2a
    style SecretsJava fill:#ff6b6b,stroke:#c92a2a
```

---

## 4. Tool Orchestration Architecture

**Purpose:** Show how agent orchestrates various tools and analyzers

**Design Doc Reference:** Section 4 - Functional Requirements (Tool Reuse & Orchestration)

```mermaid
graph TB
    subgraph "Transformation Agent Core"
        Orchestrator[Tool Orchestrator]
        Planner[Modernization Planner]
        Validator[Validation Engine]
        MCP[MCP Server<br/>Progress Tracking]
    end
    
    subgraph "Knowledge Sources"
        KB[Knowledge Base<br/>Playbooks]
        Bandish[Bandish Specs<br/>Task Definitions]
        Profile[Standards Profile<br/>ASR Compliance]
        IntakeASR[Intake Agent Storage<br/>ASR Reports]
    end
    
    subgraph "IDE Tools"
        Copilot[GitHub Copilot]
        VSCode[VS Code Extensions]
        VisualStudio[Visual Studio]
        LSP[Language Servers<br/>Roslyn, Java LSP]
    end
    
    subgraph "Azure Modernization Tools"
        AzModAnalyzer[Azure Modernization<br/>Analyzer]
        AppCAT[AppCAT CLI]
    end
    
    subgraph "Language-Specific Tools"
        Roslyn[Roslyn Analyzers<br/>.NET]
        OpenRewrite[OpenRewrite Recipes<br/>Java]
        CodeFix[Code Fix Providers]
    end
    
    subgraph "Build & Test Tools"
        MSBuild[MSBuild / dotnet CLI]
        Maven[Maven / Gradle]
        TestRunner[Test Runners<br/>xUnit, JUnit]
    end
    
    subgraph "Data & Storage"
        Cosmos[Cosmos DB<br/>Workload Metadata]
        BlobStorage[Azure Blob Storage<br/>Plans & Evidence]
    end
    
    subgraph "Code Repository"
        Git[Git Operations]
        PR[Pull Request API]
    end
    
    Planner --> KB
    Planner --> Bandish
    Planner --> Profile
    Planner --> IntakeASR
    Planner --> Cosmos
    Planner --> Orchestrator
    
    Orchestrator --> Copilot
    Orchestrator --> VSCode
    Orchestrator --> VisualStudio
    Orchestrator --> LSP
    Orchestrator --> AzModAnalyzer
    Orchestrator --> AppCAT
    Orchestrator --> Roslyn
    Orchestrator --> OpenRewrite
    Orchestrator --> CodeFix
    Orchestrator --> MCP
    
    MCP --> Cosmos
    MCP --> BlobStorage
    
    Orchestrator --> Git
    Orchestrator --> Validator
    
    Validator --> MSBuild
    Validator --> Maven
    Validator --> TestRunner
    Validator --> Profile
    
    Validator --> PR
    Validator --> BlobStorage
    
    style Orchestrator fill:#0078d4,stroke:#005a9e,color:#fff
    style Planner fill:#50e6ff,stroke:#0078d4
    style Validator fill:#51cf66,stroke:#2b8a3e
    style MCP fill:#ff922b,stroke:#e67700
    style KB fill:#fcd116,stroke:#d39d09
    style Bandish fill:#ff922b,stroke:#e67700
    style Profile fill:#e67700,stroke:#d39d09
```

---

## 5. Evidence & Lineage Flow

**Purpose:** Show output artifact generation and traceability

**Design Doc Reference:** Section 7 - Evidence Lineage & Reporting

```mermaid
graph LR
    subgraph "Input"
        Source[Source Code Repository]
        Profile[Standards Profile<br/>ASR Compliance]
        ASR[Assessment Report<br/>ASR from Intake]
        WorkloadMeta[Workload Metadata<br/>from Cosmos DB]
    end
    
    subgraph "Analysis Phase"
        Discovery[Discovery & Assessment]
        KBMatch[KB Pattern Matching]
        ASRValidation[ASR Compliance Check]
        Plan[Modernization Plan]
    end
    
    subgraph "Transformation Phase"
        Refactor[Code Refactoring]
        Config[Config Externalization]
        SecretMigration[Secret Migration]
        MCP[MCP Progress Tracking]
    end
    
    subgraph "Validation Phase"
        Build[Compilation]
        Test[Test Execution]
        Security[Security Scan]
        ASRCheck[ASR Findings Validation]
    end
    
    subgraph "Evidence Artifacts"
        PR[Pull Request<br/>with Citations]
        ModReport[Modernization Report<br/>Markdown/JSON]
        BoM[Bill of Materials<br/>Dependencies]
        RBAC[RBAC Minimal<br/>IAM Requirements]
        Changelog[Change Log<br/>Lineage]
        README[Updated README<br/>Deployment Guide]
    end
    
    subgraph "Storage & Distribution"
        BlobStorage[Azure Blob Storage<br/>Plans & Evidence]
        CosmosDB[Cosmos DB<br/>Status Updates]
        GitHub[GitHub PR]
        SharePoint[SharePoint Site]
        Email[Email Notification]
    end
    
    Source --> Discovery
    Profile --> Discovery
    ASR --> Discovery
    WorkloadMeta --> Discovery
    
    Discovery --> KBMatch
    KBMatch --> ASRValidation
    ASRValidation --> Plan
    
    Plan --> BlobStorage
    Plan --> Refactor
    Plan --> Config
    Plan --> SecretMigration
    
    Refactor --> MCP
    Config --> MCP
    SecretMigration --> MCP
    MCP --> CosmosDB
    
    Refactor --> Build
    Config --> Build
    SecretMigration --> Build
    
    Build --> Test
    Test --> Security
    Security --> ASRCheck
    
    ASRCheck --> PR
    ASRCheck --> ModReport
    ASRCheck --> BoM
    ASRCheck --> RBAC
    ASRCheck --> Changelog
    ASRCheck --> README
    
    PR --> GitHub
    ModReport --> BlobStorage
    ModReport --> SharePoint
    ModReport --> Email
    BoM --> BlobStorage
    RBAC --> BlobStorage
    README --> GitHub
    Changelog --> BlobStorage
    
    Plan -.->|Cited in| PR
    KBMatch -.->|Referenced in| ModReport
    Refactor -.->|Tracked in| Changelog
    ASR -.->|Validated in| ASRCheck
    MCP -.->|Checkpoints in| CosmosDB
    
    style Plan fill:#0078d4,stroke:#005a9e,color:#fff
    style MCP fill:#ff922b,stroke:#e67700
    style PR fill:#24292e,stroke:#000,color:#fff
    style ModReport fill:#fcd116,stroke:#d39d09
    style Security fill:#ff6b6b,stroke:#c92a2a
    style ASRCheck fill:#51cf66,stroke:#2b8a3e
```

---

## 6. Security & Identity Model

**Purpose:** Visualize security architecture and identity flows

**Design Doc Reference:** Section 8 - Security, Compliance & Observability

```mermaid
graph TB
    subgraph "Identity & Authentication"
        User[User/Service Principal]
        EntraID[Entra ID]
        MI[Managed Identity<br/>System or User-Assigned]
        RBAC[Azure RBAC]
        WorkloadID[Workload Identity<br/>Federation]
    end
    
    subgraph "Transformation Agent"
        Agent[Agent Runtime]
        MCP[MCP Server]
        Secrets[Secrets Handler]
        TokenMgr[Token Manager]
    end
    
    subgraph "Azure Resources"
        KV[Key Vault]
        Cosmos[Cosmos DB]
        Blob[Blob Storage]
        AOAI[Azure OpenAI]
        AppInsights[App Insights]
    end
    
    subgraph "External Integration"
        GitHub[GitHub API]
        GitHubApp[GitHub App<br/>MI Federation]
    end
    
    subgraph "Network Security"
        VNet[Virtual Network]
        PE[Private Endpoints]
        NSG[Network Security Groups]
        Firewall[Azure Firewall]
    end
    
    subgraph "Compliance & Governance"
        Policy[Azure Policy]
        DLP[Data Loss Prevention]
        ContentSafety[Content Safety]
        PromptShield[Prompt Shield]
        ZeroSecrets[Zero Secrets Policy<br/>No PATs/API Keys]
    end
    
    User --> EntraID
    EntraID --> Agent
    Agent --> MI
    Agent --> MCP
    MI --> RBAC
    MI --> WorkloadID
    
    Agent --> TokenMgr
    TokenMgr --> Secrets
    
    RBAC --> KV
    RBAC --> Cosmos
    RBAC --> Blob
    RBAC --> AOAI
    RBAC --> AppInsights
    
    MCP --> Cosmos
    MCP --> MI
    
    Secrets --> KV
    KV -.->|Config Only<br/>No Credentials| Agent
    
    WorkloadID --> GitHubApp
    GitHubApp --> GitHub
    Agent --> GitHubApp
    
    Agent --> VNet
    VNet --> PE
    PE --> KV
    PE --> Cosmos
    PE --> Blob
    VNet --> NSG
    VNet --> Firewall
    
    Agent --> Policy
    Agent --> DLP
    Agent --> ContentSafety
    Agent --> ZeroSecrets
    AOAI --> PromptShield
    
    style EntraID fill:#0078d4,stroke:#005a9e,color:#fff
    style MI fill:#50e6ff,stroke:#0078d4
    style KV fill:#ff6b6b,stroke:#c92a2a
    style ContentSafety fill:#51cf66,stroke:#2b8a3e
    style PE fill:#ff922b,stroke:#e67700
    style ZeroSecrets fill:#51cf66,stroke:#2b8a3e
    style GitHubApp fill:#24292e,stroke:#000,color:#fff
    style MCP fill:#ff922b,stroke:#e67700
```
