# Detailed Cost Estimates: Customer Order Services Migration

**Project:** WebSphere to Azure Migration  
**Assessment Date:** November 4, 2025  
**Currency:** USD  
**Document Version:** 1.0

---

## Table of Contents

1. [Development Effort Breakdown](#1-development-effort-breakdown)
2. [AI Assistance Impact Analysis](#2-ai-assistance-impact-analysis)
3. [Azure Infrastructure Costs](#3-azure-infrastructure-costs)
4. [Total Cost of Ownership (3-Year)](#4-total-cost-of-ownership-3-year)
5. [Cost Optimization Strategies](#5-cost-optimization-strategies)
6. [Break-Even Analysis](#6-break-even-analysis)

---

## 1. Development Effort Breakdown

### 1.1 Detailed Task Catalog

**Labor Rate:** $100/hour (blended developer rate)  
**Team Size:** 2 developers  
**Working Hours:** 40 hours/week per developer

| Phase | Task Category | Manual Hours | AI-Assisted Hours | AI Savings | Manual Cost | AI Cost | Savings |
|-------|--------------|--------------|-------------------|------------|-------------|---------|---------|
| **Phase 0: Pre-Migration** |
| | Environment setup | 16 | 8 | 50% | $1,600 | $800 | $800 |
| | Dependency scan | 8 | 4 | 50% | $800 | $400 | $400 |
| | Code complexity analysis | 8 | 4 | 50% | $800 | $400 | $400 |
| | Database schema analysis | 16 | 10 | 38% | $1,600 | $1,000 | $600 |
| | WebSphere config inventory | 12 | 8 | 33% | $1,200 | $800 | $400 |
| **Phase 0 Subtotal** | | **60** | **34** | **43%** | **$6,000** | **$3,400** | **$2,600** |
| | | | | | | | |
| **Phase 1: Testing & Framework** |
| | Generate domain model tests | 40 | 16 | 60% | $4,000 | $1,600 | $2,400 |
| | Generate service layer tests | 40 | 16 | 60% | $4,000 | $1,600 | $2,400 |
| | Generate REST API tests | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Configure test frameworks | 12 | 6 | 50% | $1,200 | $600 | $600 |
| | Java 8 → 17 upgrade | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | javax.* → jakarta.* | 60 | 25 | 58% | $6,000 | $2,500 | $3,500 |
| | Jackson 1.7 → 2.17 | 40 | 18 | 55% | $4,000 | $1,800 | $2,200 |
| | IBM JSON → Jackson | 50 | 22 | 56% | $5,000 | $2,200 | $2,800 |
| | Maven compiler update | 8 | 4 | 50% | $800 | $400 | $400 |
| | Fix compilation errors | 60 | 30 | 50% | $6,000 | $3,000 | $3,000 |
| | Run tests & fix failures | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| **Phase 1 Subtotal** | | **430** | **197** | **54%** | **$43,000** | **$19,700** | **$23,300** |
| | | | | | | | |
| **Phase 2: Azure Integration** |
| | DB2 → PostgreSQL migration | 80 | 40 | 50% | $8,000 | $4,000 | $4,000 |
| | OpenJPA → Hibernate | 60 | 30 | 50% | $6,000 | $3,000 | $3,000 |
| | Externalize datasource config | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Managed Identity for DB | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Entra ID integration | 80 | 35 | 56% | $8,000 | $3,500 | $4,500 |
| | Key Vault integration | 40 | 18 | 55% | $4,000 | $1,800 | $2,200 |
| | OpenTelemetry instrumentation | 60 | 25 | 58% | $6,000 | $2,500 | $3,500 |
| | Application Insights setup | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Health check endpoints | 32 | 16 | 50% | $3,200 | $1,600 | $1,600 |
| | Graceful shutdown handling | 24 | 12 | 50% | $2,400 | $1,200 | $1,200 |
| **Phase 2 Subtotal** | | **496** | **236** | **52%** | **$49,600** | **$23,600** | **$26,000** |
| | | | | | | | |
| **Phase 3: Deployment** |
| | Create Dockerfile | 32 | 16 | 50% | $3,200 | $1,600 | $1,600 |
| | Optimize container image | 24 | 12 | 50% | $2,400 | $1,200 | $1,200 |
| | Configure ACR | 16 | 8 | 50% | $1,600 | $800 | $800 |
| | Bicep/Terraform templates | 80 | 40 | 50% | $8,000 | $4,000 | $4,000 |
| | GitHub Actions CI/CD | 60 | 30 | 50% | $6,000 | $3,000 | $3,000 |
| | Managed identity setup | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Deploy to Container Apps | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Auto-scaling configuration | 32 | 16 | 50% | $3,200 | $1,600 | $1,600 |
| | Load testing & tuning | 80 | 40 | 50% | $8,000 | $4,000 | $4,000 |
| | Security scanning | 40 | 20 | 50% | $4,000 | $2,000 | $2,000 |
| | Production deployment | 60 | 30 | 50% | $6,000 | $3,000 | $3,000 |
| | Documentation & runbooks | 60 | 25 | 58% | $6,000 | $2,500 | $3,500 |
| **Phase 3 Subtotal** | | **564** | **277** | **51%** | **$56,400** | **$27,700** | **$28,700** |
| | | | | | | | |
| **GRAND TOTAL** | | **1,550** | **744** | **52%** | **$155,000** | **$74,400** | **$80,600** |

---

### 1.2 AI Tooling Costs

| Tool | Cost Model | Monthly Cost | Duration | Total Cost |
|------|-----------|--------------|----------|------------|
| **AppCAT (Assessment)** | Included with Azure subscription | $0 | 1 week | **$0** |
| **GitHub Copilot App Modernization** | Requires GitHub Copilot subscription | See below | 3 months | See below |
| **GitHub Copilot Business** | $19/user/month | $38 (2 users) | 3 months | $114 |
| **Azure OpenAI (GPT-4)** | Pay-as-you-go (optional for custom agents) | ~$200/month | 3 months | $600 |
| **TOTAL AI TOOLING** | | | | **$714** |

**Notes:**

- **AppCAT** (Azure Migrate application and code assessment for Java) is a **free CLI tool** included with Azure subscriptions
- **GitHub Copilot App Modernization** is a free VS Code/IntelliJ extension, but requires a GitHub Copilot subscription (Pro, Pro+, Business, or Enterprise)
- GitHub Copilot Business ($19/user/month) includes both Copilot chat/code completion AND the App Modernization extension

**Net Development Savings:** $80,600 - $714 = **$79,886 (51.5% reduction)**

---

### 1.3 Task Automation Potential

**Primary Automation Engine:** AppCAT (assessment) + GitHub Copilot (code transformation)

| Task Type | Manual Hours | AI-Assisted Hours | Automation % | Primary Tool |
|-----------|--------------|-------------------|--------------|--------------|
| **Assessment & Analysis** | 120 | 24 | 80% | AppCAT |
| **Test Generation** | 120 | 52 | 57% | GitHub Copilot |
| **Code Refactoring** | 250 | 115 | 54% | GitHub Copilot + OpenRewrite |
| **Configuration Migration** | 120 | 60 | 50% | GitHub Copilot |
| **Infrastructure as Code** | 160 | 80 | 50% | GitHub Copilot |
| **Documentation** | 120 | 24 | 80% | AppCAT Reports + Copilot |
| **Boilerplate Code** | 180 | 72 | 60% | GitHub Copilot |
| **Complex Logic** | 600 | 315 | 48% | GitHub Copilot (manual review) |

**Key Insight:** AppCAT provides maximum automation for assessment (80%), while GitHub Copilot accelerates test generation and boilerplate (57-60%). Complex business logic requires more manual review (48% automation).

---

## 2. AI Assistance Impact Analysis

### 2.1 Productivity Multipliers by Phase

| Phase | Manual Velocity | AI-Assisted Velocity | Multiplier |
|-------|----------------|---------------------|-----------|
| Phase 0: Setup | 15 hrs/week/dev | 26 hrs/week/dev | 1.73x |
| Phase 1: Testing & Framework | 14 hrs/week/dev | 31 hrs/week/dev | 2.18x |
| Phase 2: Azure Integration | 15 hrs/week/dev | 30 hrs/week/dev | 2.00x |
| Phase 3: Deployment | 16 hrs/week/dev | 31 hrs/week/dev | 1.94x |
| **Average** | **15 hrs/week/dev** | **29 hrs/week/dev** | **1.93x** |

**Interpretation:** AI assistance nearly doubles effective developer productivity (93% increase).

---

### 2.2 Quality Assurance for AI-Generated Code

**Recommended QA Process:**

| Stage | Activity | Effort (Hours) | Cost |
|-------|---------|----------------|------|
| **Code Review** | Peer review of AI suggestions | 60 | $6,000 |
| **Unit Testing** | Validate generated code | Included | - |
| **Integration Testing** | End-to-end validation | Included | - |
| **Security Scan** | Static analysis (SonarQube, Snyk) | 16 | $1,600 |
| **Performance Testing** | Load testing | Included | - |
| **TOTAL QA OVERHEAD** | | **76** | **$7,600** |

**Adjusted Net Savings:** $79,886 - $7,600 = **$72,286 (46.6% reduction after QA)**

---

## 3. Azure Infrastructure Costs

### 3.1 Monthly Operating Costs (Production + Staging)

**Region:** East US 2  
**Pricing Date:** November 2025

| Service | Tier/SKU | Quantity | Unit Cost | Monthly Cost | Annual Cost |
|---------|----------|----------|-----------|--------------|-------------|
| **Compute** |
| Azure Container Apps (Prod) | 2 vCPU, 4 GB RAM | 3 replicas | $0.000024/sec | $1,866 | $22,392 |
| Azure Container Apps (Staging) | 1 vCPU, 2 GB RAM | 2 replicas | $0.000012/sec | $622 | $7,464 |
| **Database** |
| PostgreSQL Flexible (Prod) | Standard_D8s_v4 (8 vCPU, 32 GB) | 1 instance | $0.456/hr | $333 | $3,996 |
| PostgreSQL Flexible (Staging) | Standard_D2s_v4 (2 vCPU, 8 GB) | 1 instance | $0.114/hr | $83 | $996 |
| **Storage & Registry** |
| Azure Container Registry | Standard (100 GB) | 1 registry | $20/month | $20 | $240 |
| PostgreSQL Storage | 256 GB (prod) + 64 GB (staging) | 320 GB | $0.115/GB | $37 | $444 |
| Backup Storage (Blob LRS) | 500 GB | - | $0.018/GB | $9 | $108 |
| **Observability** |
| Application Insights | 50 GB ingestion/month | - | $2.30/GB | $115 | $1,380 |
| Log Analytics Workspace | 30 GB ingestion/month | - | $2.76/GB | $83 | $996 |
| **Security & Configuration** |
| Azure Key Vault | 10K operations/month | 1 vault | $0.03/10K ops | $5 | $60 |
| Azure App Configuration | 1M requests/month | 1 instance | $1.20/day | $36 | $432 |
| **Networking** |
| Load Balancer (Standard) | 1 LB | - | $0.025/hr | $18 | $216 |
| Data Egress | 100 GB/month | - | $0.087/GB | $9 | $108 |
| **TOTAL MONTHLY** | | | | **$3,236** | **$38,832** |

---

### 3.2 Environment-Specific Breakdown

| Environment | Monthly Cost | Annual Cost | Purpose |
|-------------|--------------|-------------|---------|
| **Production** | $2,532 | $30,384 | Customer-facing workload |
| **Staging** | $704 | $8,448 | Pre-production testing |
| **TOTAL** | **$3,236** | **$38,832** | |

---

### 3.3 Cost Scaling Scenarios

**Scenario 1: Low Traffic (Current Estimate)**
- Concurrent Users: 500
- Requests/sec: 100
- Monthly Cost: **$3,236**

**Scenario 2: Medium Traffic (2x growth)**
- Concurrent Users: 1,000
- Requests/sec: 200
- Container replicas: 6 (prod), 2 (staging)
- Database SKU: Same (plenty of headroom)
- Monthly Cost: **$4,358** (+35%)

**Scenario 3: High Traffic (5x growth)**
- Concurrent Users: 2,500
- Requests/sec: 500
- Container replicas: 10 (prod), 3 (staging)
- Database SKU: Standard_D16s_v4 (16 vCPU, 64 GB)
- Monthly Cost: **$7,124** (+120%)

---

## 4. Total Cost of Ownership (3-Year)

### 4.1 On-Premises (WebSphere) TCO

| Cost Category | Year 1 | Year 2 | Year 3 | 3-Year Total |
|---------------|--------|--------|--------|--------------|
| **Infrastructure** |
| Servers (2x physical) | $60,000 | $0 | $0 | $60,000 |
| Storage (SAN) | $25,000 | $5,000 | $5,000 | $35,000 |
| Network equipment | $12,000 | $2,000 | $2,000 | $16,000 |
| Data center space/power | $5,000 | $5,000 | $5,000 | $15,000 |
| **Software Licensing** |
| WebSphere App Server | $50,000 | $50,000 | $50,000 | $150,000 |
| IBM DB2 Database | $25,000 | $25,000 | $25,000 | $75,000 |
| **Operations** |
| System admin (50% FTE) | $60,000 | $60,000 | $60,000 | $180,000 |
| **Maintenance & Support** |
| Hardware maintenance | $0 | $15,000 | $15,000 | $30,000 |
| Software support (20%) | $15,000 | $15,000 | $15,000 | $45,000 |
| **Backup & DR** |
| Backup solution | $8,000 | $8,000 | $8,000 | $24,000 |
| **Upgrades & Patching** |
| Quarterly patching effort | $12,000 | $12,000 | $12,000 | $36,000 |
| **Security** |
| SSL certificates | $1,000 | $1,000 | $1,000 | $3,000 |
| Security scanning tools | $3,000 | $3,000 | $3,000 | $9,000 |
| **ANNUAL TOTAL** | **$276,000** | **$201,000** | **$201,000** | **$678,000** |

---

### 4.2 Azure (Container Apps) TCO

| Cost Category | Year 1 | Year 2 | Year 3 | 3-Year Total |
|---------------|--------|--------|--------|--------------|
| **Initial Migration** |
| Development effort | $74,400 | $0 | $0 | $74,400 |
| **Azure Infrastructure** |
| Container Apps + PostgreSQL | $38,832 | $38,832 | $38,832 | $116,496 |
| **Operations** |
| System admin (10% FTE) | $12,000 | $12,000 | $12,000 | $36,000 |
| **Monitoring & Support** |
| Azure Support (Developer) | $960 | $960 | $960 | $2,880 |
| **Maintenance** |
| Dependency updates | $8,000 | $8,000 | $8,000 | $24,000 |
| Feature enhancements | $12,000 | $12,000 | $12,000 | $36,000 |
| **Security** |
| Entra ID (included) | $0 | $0 | $0 | $0 |
| Managed certificates | $0 | $0 | $0 | $0 |
| **ANNUAL TOTAL** | **$146,192** | **$71,792** | **$71,792** | **$289,776** |

---

### 4.3 Cost Comparison Summary

| Metric | On-Premises | Azure | Savings |
|--------|-------------|-------|---------|
| **Year 1** | $276,000 | $146,192 | $129,808 (47%) |
| **Year 2** | $201,000 | $71,792 | $129,208 (64%) |
| **Year 3** | $201,000 | $71,792 | $129,208 (64%) |
| **3-YEAR TOTAL** | **$678,000** | **$289,776** | **$388,224 (57%)** |

**ROI:** **422%** ($388,224 savings / $74,400 migration investment + 14 months = $92,192)

---

## 5. Cost Optimization Strategies

### 5.1 Immediate Optimizations (< 1 week effort)

| Optimization | Savings (Monthly) | Savings (Annual) | Effort (Hours) |
|--------------|------------------|------------------|----------------|
| **Azure Reserved Instances (PostgreSQL)** | $83 | $996 | 2 |
| **Auto-scale min replicas (off-hours)** | $400 | $4,800 | 8 |
| **Log retention: 30d → 7d** | $120 | $1,440 | 4 |
| **Container image optimization** | $20 | $240 | 12 |
| **TOTAL** | **$623/mo** | **$7,476/yr** | **26 hrs** |

**ROI:** $7,476 / ($100/hr * 26 hrs) = **287% annual ROI**

---

### 5.2 Advanced Optimizations (2-4 weeks effort)

| Optimization | Savings (Monthly) | Savings (Annual) | Effort (Hours) |
|--------------|------------------|------------------|----------------|
| **Spot instances (staging)** | $300 | $3,600 | 16 |
| **CDN for static assets** | $50 | $600 | 24 |
| **Database query optimization** | $100 | $1,200 | 40 |
| **Compress telemetry data** | $80 | $960 | 20 |
| **TOTAL** | **$530/mo** | **$6,360/yr** | **100 hrs** |

**ROI:** $6,360 / ($100/hr * 100 hrs) = **64% annual ROI**

---

### 5.3 Multi-Year Cost Projections

| Scenario | Year 1 | Year 2 | Year 3 | 3-Year Total |
|----------|--------|--------|--------|--------------|
| **Baseline (No Optimization)** | $146,192 | $71,792 | $71,792 | $289,776 |
| **With Immediate Optimizations** | $138,720 | $64,320 | $64,320 | $267,360 |
| **With All Optimizations** | $132,360 | $57,960 | $57,960 | $248,280 |
| **TOTAL SAVINGS** | **$13,832** | **$13,832** | **$13,832** | **$41,496** |

---

## 6. Break-Even Analysis

### 6.1 Break-Even Calculation

**Initial Investment (AI-Assisted):** $74,400  
**Monthly Savings vs. On-Premises:** $10,817 (Year 1 avg)

**Break-Even Point:** $74,400 / $10,817 = **6.9 months**

**Timeline:**
- Month 0-3: Migration project ($74,400)
- Month 4-10: Payback period (recovering investment)
- Month 11+: Net positive cash flow

---

### 6.2 Cumulative Cash Flow

| Month | On-Prem Cost | Azure Cost | Migration Cost | Cumulative Savings |
|-------|-------------|------------|----------------|-------------------|
| 0 | $0 | $0 | $74,400 | -$74,400 |
| 1 | $23,000 | $3,236 | $0 | -$54,636 |
| 2 | $23,000 | $3,236 | $0 | -$34,872 |
| 3 | $23,000 | $3,236 | $0 | -$15,108 |
| 4 | $23,000 | $3,236 | $0 | $4,656 |
| 5 | $23,000 | $3,236 | $0 | $24,420 |
| 6 | $23,000 | $3,236 | $0 | $44,184 |
| **7 (Break-even)** | $23,000 | $3,236 | $0 | $63,948 |
| 12 | $23,000 | $3,236 | $0 | $162,876 |
| 24 | $23,000 | $3,236 | $0 | $400,752 |
| 36 | $23,000 | $3,236 | $0 | $638,628 |

---

## Summary

### Key Financial Metrics

| Metric | Value |
|--------|-------|
| **Total Migration Cost (AI-Assisted)** | $74,400 |
| **Development Cost Savings (vs. Manual)** | $72,286 (46.6%) |
| **3-Year Infrastructure Savings** | $388,224 (57%) |
| **3-Year TCO (Azure)** | $289,776 |
| **3-Year TCO (On-Premises)** | $678,000 |
| **Break-Even Point** | 7 months |
| **3-Year ROI** | 422% |
| **Annual Operational Savings** | ~$129,000 (after Year 1) |

### Recommendation

**PROCEED with Azure migration** - Financial case is overwhelmingly positive with 7-month payback period and $388K in 3-year savings.

---

**Document Control:**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-04 | AI Migration Team | Initial cost analysis |

