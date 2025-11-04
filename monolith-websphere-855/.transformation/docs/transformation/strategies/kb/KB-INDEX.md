# Knowledge Base Index

This index summarizes all strategy playbooks referenced in Section 6 of the Transformation Agent Design.

| KB | Domain | Primary Signals | Azure Modernization | Bandish | Custom Tooling | Summary |
| --- | --- | --- | --- | --- | --- | --- |
| [Storage Abstraction](storage/README.md) | Application / Storage | Direct System.IO/java.io/S3 usage | ✅ | ✅ | | Introduce abstraction + Blob adapter, remove direct filesystem/object store calls |
| [Email Modernization](email/README.md) | Application / Messaging | SMTP client usage, inline credentials | ✅ | ✅ | | Migrate SMTP to ACS Email with identity + configuration externalization |
| [Secrets Externalization](secrets/README.md) | Security / Config | Plaintext secrets, inline connection strings | ✅ | ✅ | | Move secrets to Key Vault, inject references via config providers |
| [SDK Replacement](sdk-replacement/README.md) | Application / Dependencies | Proprietary or non-Azure SDK imports | ✅ | ✅ | | Map and replace legacy SDKs with Azure SDK equivalents |
| [Logging & Observability](logging/README.md) | Observability | Console prints, missing correlation | ✅ | ✅ | | Standardize structured logging + OpenTelemetry pipelines |
| [Identity Migration](identity/README.md) | Security / Identity | LDAP/AD calls, custom token issuers | ✅ | ✅ | | Transition to Entra ID with MSAL / Managed Identity |
| [Messaging (SQS to Service Bus)](messaging/README.md) | Integration / Messaging | AWS SQS client usage | ✅ | ✅ | | Replace SQS with Service Bus, add settlement & DLQ patterns |
| [Events (CloudWatch to Grid)](events/README.md) | Integration / Events | CloudWatch/EventBridge rules | ✅ | ✅ | | Rewire event publishing to Event Grid with schema mapping |
| [Configuration Externalization](config-externalization/README.md) | Config Management | Large local config files | ✅ | ✅ | | Externalize config to App Config + Key Vault, enable feature flags |
| [Framework Upgrade](framework-upgrade/README.md) | Platform / Runtime | Out-of-support framework versions | ✅ | ✅ | | Upgrade to supported LTS versions, resolve deprecated APIs |
| [Database Migration](KB-Database-Migration.md) | Data / Storage | DB2, Oracle, SQL Server on-prem | ✅ | ✅ | | Migrate to Azure SQL DB/MI/PostgreSQL with Managed Identity authentication |
| [Caching Migration](KB-Caching-Migration.md) | Performance / Caching | In-memory or local Redis cache | ✅ | ✅ | | Replace with Azure Cache for Redis using Managed Identity and StackExchange.Redis |
| [Serverless Migration](serverless-migration/README.md) | Serverless / Compute | AWS Lambda functions (.NET/Java/Node.js/Python) | ❌ | ❌ | ✅ | Migrate Lambda functions to Azure Functions; transform handler signatures, triggers, and bindings; convert SAM to Bicep/ARM |
| [Container Orchestration Migration](container-migration/README.md) | Container / Kubernetes | AWS EKS clusters and workloads | ❌ | ❌ | ✅ | Migrate Kubernetes workloads from EKS to AKS; translate IRSA to Workload Identity; convert AWS-specific manifests and resources; migrate ECR to ACR |

Legend: Tool support columns indicate which automation approaches can assist with each scenario. "✅" indicates the tool/approach supports this migration type; "❌" indicates not supported; blank cells indicate the tool is not required/applicable. **Azure Modernization** = Azure Modernization analyzers; **Bandish** = Bandish orchestrated workflows; **Custom Tooling** = Specialized agents/tools required. Future columns may add Effort, Risk, and Confidence scoring.
