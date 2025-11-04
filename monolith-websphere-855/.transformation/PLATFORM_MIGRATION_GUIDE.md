# Platform Migration Guide: WebSphere to Azure Container Apps

**Project:** Customer Order Services  
**Migration Type:** WebSphere Traditional 8.5.5 → Azure Container Apps  
**Document Version:** 1.0  
**Last Updated:** November 4, 2025

---

## Table of Contents

1. [Overview](#1-overview)
2. [Pre-Migration Checklist](#2-pre-migration-checklist)
3. [Phase 0: Environment Setup & Analysis](#phase-0-environment-setup--analysis-week-1-2)
4. [Phase 1: Containerization Foundation](#phase-1-containerization-foundation-week-3-4)
5. [Phase 2: Azure Resource Provisioning](#phase-2-azure-resource-provisioning-week-5-6)
6. [Phase 3: Application Deployment](#phase-3-application-deployment-week-7-8)
7. [Phase 4: Validation & Cutover](#phase-4-validation--cutover-week-9-10)
8. [Rollback Procedures](#rollback-procedures)
9. [Troubleshooting Guide](#troubleshooting-guide)

---

## 1. Overview

### 1.1 Migration Strategy

**Approach:** Replatform (Lift-Modernize-Shift)  
**Pattern:** Monolith containerization with cloud-native enhancements  
**Assessment Tool:** **AppCAT** (Azure Migrate application and code assessment for Java)  
**Transformation Tool:** **GitHub Copilot App Modernization for Java** (VS Code/IntelliJ extension)  
**Supporting Tools:** OpenRewrite (code refactoring), Azure Database Migration Service

**AppCAT Coverage (Assessment):**

- ✅ **Automated Assessment:** Framework detection, CVE scanning, dependency analysis, WebSphere descriptor analysis
- ✅ **Detailed Reporting:** HTML reports with story points, issue categorization, file-level guidance
- ✅ **Azure Target Mapping:** Maps WebSphere patterns to Azure Container Apps/App Service recommendations

**GitHub Copilot App Modernization Coverage (Transformation):**

- ✅ **Code Transformation:** Java 8→17, javax→jakarta, EJB→Spring Boot, JAX-RS→Spring MVC
- ✅ **Predefined Migration Tasks:** Azure SQL, PostgreSQL, Redis, Managed Identity integration
- ✅ **Validation Loop:** Build-Project, Run-Test, CVE validation, consistency/completeness checks
- ✅ **AI-Assisted Fixes:** GitHub Copilot for complex transformations
- ✅ **Test Generation:** Automated unit test creation with coverage reporting
- ⚠️ **Database Migration:** Use Azure Database Migration Service (separate tool)

### 1.2 Architecture Transformation

```
┌─────────────────────────────────────────────────────────────────┐
│                        BEFORE (On-Premises)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Internet ──> IHS/Plugin ──> WebSphere App Server 8.5.5       │
│                                  │                               │
│                                  ├─> EJB Container (EAR)         │
│                                  │   └─> CustomerOrderServices  │
│                                  │                               │
│                                  ├─> Web Container (WAR)         │
│                                  │   └─> CustomerOrderWeb       │
│                                  │                               │
│                                  └─> JNDI ──> jdbc/orderds      │
│                                                  │                │
│                                                  v                │
│                                             DB2 Database          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       AFTER (Azure)                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Internet ──> Azure Container Apps Ingress (HTTPS)             │
│                       │                                          │
│                       v                                          │
│            ┌──────────────────────────┐                         │
│            │  Container Apps Environment│                        │
│            │  ┌─────────────────────┐  │                        │
│            │  │ customer-order-api  │  │ (3 replicas)          │
│            │  │  Spring Boot 3.2    │  │                        │
│            │  │  + OpenTelemetry    │  │                        │
│            │  │  + Managed Identity │  │                        │
│            │  └─────────────────────┘  │                        │
│            └──────────────────────────┘                         │
│                       │                                          │
│                       ├──> Azure PostgreSQL (Managed Identity)   │
│                       ├──> Azure Key Vault (secrets)             │
│                       ├──> Azure App Configuration               │
│                       ├──> Microsoft Entra ID (auth)             │
│                       └──> Application Insights (telemetry)      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 Key Transformations

| WebSphere Feature | Azure Equivalent | Transformation |
|------------------|------------------|----------------|
| **EJB Container** | Spring Boot Tomcat | Replace `@Stateless` with `@Service` |
| **JNDI DataSource** | Spring DataSource Config | Externalize to application.yml |
| **WebSphere Security** | Microsoft Entra ID OAuth | Implement Spring Security + MSAL |
| **PMI Monitoring** | Application Insights | OpenTelemetry instrumentation |
| **SystemOut.log** | Azure Monitor Logs | Structured logging (JSON) |
| **IBM HTTP Server** | Container Apps Ingress | Built-in load balancer + SSL |
| **WAS Config (XML)** | App Configuration + Key Vault | Environment variables + secrets |

---

## 2. Pre-Migration Checklist

### 2.1 Prerequisites

**Azure Subscription:**
- [ ] Contributor or Owner access
- [ ] Resource Group created: `rg-customer-order-prod`
- [ ] Region selected: (e.g., `East US 2`)

**Development Tools:**
- [ ] JDK 17 installed ([Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/download))
- [ ] Maven 3.9+ installed
- [ ] Docker Desktop installed and running
- [ ] Azure CLI installed (`az --version` ≥ 2.50)
- [ ] GitHub Copilot license activated
- [ ] IDE: IntelliJ IDEA Ultimate or VS Code + Java extensions

**Access & Credentials:**
- [ ] GitHub repository access (for CI/CD)
- [ ] Azure Container Registry created or permission to create
- [ ] Azure subscription ID documented
- [ ] Service Principal or Managed Identity for deployments

**WebSphere Environment Analysis:**
- [ ] WebSphere server.xml exported
- [ ] Application configuration documented (datasources, security domains)
- [ ] JVM arguments captured (`-Xmx`, `-Xms`, GC settings)
- [ ] Current resource utilization measured (CPU, memory, concurrent users)

---

### 2.2 Risk Assessment

| Risk | Mitigation |
|------|------------|
| **Data loss during DB migration** | ✅ Automated backups, dry-run migrations |
| **Incompatible WebSphere APIs** | ✅ Early code scan, replace IBM JSON/JAX-RS |
| **Performance degradation** | ✅ Load testing before cutover |
| **Authentication integration complexity** | ✅ Entra ID POC in Phase 0 |
| **Unexpected downtime** | ✅ Maintain WebSphere for 30-day rollback window |

---

## Phase 0: Environment Setup & Analysis (Week 1-2)

### Step 0.1: Install Development Tools

**PowerShell Script (Windows):**

```powershell
# Install JDK 17 (Microsoft Build of OpenJDK)
winget install Microsoft.OpenJDK.17

# Verify installation
java -version
# Expected output: openjdk version "17.0.x" Microsoft Build

# Install Maven
winget install Apache.Maven

# Verify Maven
mvn -version

# Install Docker Desktop
winget install Docker.DockerDesktop

# Install Azure CLI
winget install Microsoft.AzureCLI

# Verify Azure CLI
az --version
az login
```

**Effort:** 2 hours

---

### Step 0.2: Export WebSphere Configuration

**Objective:** Document current server configuration for reference

```powershell
# On WebSphere server (if accessible)
cd /opt/IBM/WebSphere/AppServer/profiles/AppSrv01/config/cells/<cell>/nodes/<node>

# Export server configuration
/opt/IBM/WebSphere/AppServer/bin/wsadmin.sh -lang jython \
    -c "AdminConfig.extract('server.xml', 'c:/temp/websphere-config.xml')"

# Export datasource configuration
/opt/IBM/WebSphere/AppServer/bin/wsadmin.sh -lang jython \
    -f /path/to/exportDataSources.py

# Capture JVM arguments
cat /opt/IBM/WebSphere/AppServer/profiles/AppSrv01/config/cells/<cell>/nodes/<node>/servers/<server>/server.xml \
    | grep -A 20 "jvmEntries"
```

**Deliverable:** WebSphere configuration inventory (server.xml, datasources, security realms)

**Effort:** 8 hours

---

### Step 0.3: Analyze Current Application

**Run SonarQube Scan:**

```powershell
# Navigate to project root
cd c:\AIMigrate\java\appmodernization-samples\monolith-websphere-855

# Run SonarQube analysis
mvn clean verify sonar:sonar `
    -Dsonar.host.url=http://localhost:9000 `
    -Dsonar.login=<your-token>
```

**Run Dependency Vulnerability Scan:**

```powershell
# Using Dependabot (GitHub) or OWASP Dependency-Check
mvn org.owasp:dependency-check-maven:check
```

**Expected Findings:**
- 🔴 **7 critical vulnerabilities** (Jackson 1.7.1, Java EE 7)
- ⚠️ **Code complexity:** Moderate (avg cyclomatic complexity <10)
- ⚠️ **Code duplication:** Low (<5%)

**Deliverable:** Baseline code quality metrics, vulnerability report

**Effort:** 8 hours

---

### Step 0.4: Database Schema Analysis

**Export DB2 Schema:**

```bash
# Connect to DB2
db2 connect to ORDERS user <username>

# Export DDL for all tables
db2look -d ORDERS -e -o orders_schema.ddl

# Export row counts
db2 "SELECT tabname, card FROM syscat.tables WHERE tabschema = 'ORDERS'"

# Export table sizes
db2 "SELECT tabname, npages * pagesize / 1024 / 1024 AS size_mb 
     FROM syscat.tables 
     WHERE tabschema = 'ORDERS'"
```

**Expected Output:**
```
TABLE_NAME          ROW_COUNT    SIZE_MB
--------------------------------------------
ORDERS              50,000       25
LINEITEM            150,000      40
PRODUCT             5,000        8
CATEGORY            50           1
ABSTRACTCUSTOMER    10,000       15
```

**Deliverable:** Database schema DDL, data volume analysis

**Effort:** 16 hours

---

### Step 0.5: Create Azure Resource Group

```powershell
# Login to Azure
az login

# Set subscription
az account set --subscription "<your-subscription-id>"

# Create resource group
az group create `
    --name rg-customer-order-prod `
    --location eastus2 `
    --tags Environment=Production Project=CustomerOrder

# Verify
az group show --name rg-customer-order-prod --output table
```

**Effort:** 1 hour

---

## Phase 1: Containerization Foundation (Week 3-4)

### Step 1.1: Create Multi-Stage Dockerfile

**Location:** `c:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\Dockerfile`

```dockerfile
# Stage 1: Build application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy POM files first (for layer caching)
COPY CustomerOrderServicesProject/pom.xml ./
COPY CustomerOrderServices/pom.xml ./CustomerOrderServices/
COPY CustomerOrderServicesWeb/pom.xml ./CustomerOrderServicesWeb/
COPY CustomerOrderServicesApp/pom.xml ./CustomerOrderServicesApp/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY CustomerOrderServices/ ./CustomerOrderServices/
COPY CustomerOrderServicesWeb/ ./CustomerOrderServicesWeb/
COPY CustomerOrderServicesApp/ ./CustomerOrderServicesApp/

# Build application (Spring Boot JAR, not EAR)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/customer-order-services-*.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Environment variables (overridden at runtime)
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    APPLICATIONINSIGHTS_CONNECTION_STRING="" \
    AZURE_TENANT_ID="" \
    AZURE_CLIENT_ID="" \
    DB_HOST="" \
    DB_NAME="orders"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Build and Test Locally:**

```powershell
# Build Docker image
docker build -t customer-order-api:1.0.0 .

# Run container locally
docker run -d `
    -p 8080:8080 `
    -e DB_HOST=host.docker.internal `
    -e DB_NAME=orders `
    -e DB_USERNAME=test `
    -e DB_PASSWORD=test `
    --name customer-order-api `
    customer-order-api:1.0.0

# Check logs
docker logs -f customer-order-api

# Test health endpoint
curl http://localhost:8080/actuator/health

# Stop container
docker stop customer-order-api
docker rm customer-order-api
```

**Effort:** 32 hours (16 hours with GitHub Copilot)

---

### Step 1.2: Create .dockerignore

**Location:** `.dockerignore`

```
# Maven build artifacts
target/
**/target/

# IDE files
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# OS files
.DS_Store
Thumbs.db

# Git
.git/
.gitignore

# Documentation
*.md
docs/

# Test files
**/src/test/

# Logs
*.log
```

**Effort:** 1 hour

---

### Step 1.3: Optimize Container Image Size

**Before Optimization:**
```
REPOSITORY              TAG       SIZE
customer-order-api      1.0.0     450 MB
```

**Optimizations:**

1. Use Alpine Linux base image ✅ (already applied)
2. Multi-stage build ✅ (already applied)
3. Remove build tools from runtime ✅ (already applied)
4. Use `.dockerignore` ✅ (already applied)

**Additional Optimization (JLink for custom JRE):**

```dockerfile
# Stage 1.5: Create custom JRE with jlink
FROM eclipse-temurin:17-jdk-alpine AS jre-builder

# Install jlink
RUN jlink \
    --add-modules java.base,java.sql,java.naming,java.management,java.instrument,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /javaruntime

# Stage 2: Use custom JRE
FROM alpine:3.18

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=jre-builder /javaruntime $JAVA_HOME

# ... rest of Dockerfile
```

**After Optimization:**
```
REPOSITORY              TAG       SIZE
customer-order-api      1.0.0     220 MB  (51% reduction)
```

**Effort:** 24 hours (12 hours with AI assistance)

---

## Phase 2: Azure Resource Provisioning (Week 5-6)

### Step 2.1: Create Azure Container Registry (ACR)

```powershell
# Create ACR
az acr create `
    --resource-group rg-customer-order-prod `
    --name acrcustomerorderprod `
    --sku Standard `
    --location eastus2 `
    --admin-enabled false

# Enable anonymous pull (optional, for dev/test only)
# az acr update --name acrcustomerorderprod --anonymous-pull-enabled true

# Login to ACR
az acr login --name acrcustomerorderprod

# Tag and push image
docker tag customer-order-api:1.0.0 acrcustomerorderprod.azurecr.io/customer-order-api:1.0.0
docker push acrcustomerorderprod.azurecr.io/customer-order-api:1.0.0

# Verify image
az acr repository show `
    --name acrcustomerorderprod `
    --repository customer-order-api
```

**Effort:** 8 hours

---

### Step 2.2: Provision Azure Database for PostgreSQL

**Bicep Template (`infra/database.bicep`):**

```bicep
@description('PostgreSQL server name')
param serverName string = 'psql-customer-order-prod'

@description('Administrator username')
param administratorLogin string = 'dbadmin'

@description('Administrator password')
@secure()
param administratorPassword string

@description('Location for resources')
param location string = resourceGroup().location

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2022-12-01' = {
  name: serverName
  location: location
  sku: {
    name: 'Standard_D8s_v4'
    tier: 'GeneralPurpose'
  }
  properties: {
    version: '15'
    administratorLogin: administratorLogin
    administratorLoginPassword: administratorPassword
    storage: {
      storageSizeGB: 256
      autoGrow: 'Enabled'
    }
    backup: {
      backupRetentionDays: 14
      geoRedundantBackup: 'Enabled'
    }
    highAvailability: {
      mode: 'ZoneRedundant'
    }
    network: {
      publicNetworkAccess: 'Disabled'  // Use private endpoint
    }
  }
}

resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2022-12-01' = {
  parent: postgresServer
  name: 'orders'
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

output serverFqdn string = postgresServer.properties.fullyQualifiedDomainName
output databaseName string = database.name
```

**Deploy:**

```powershell
# Create Key Vault for secrets
az keyvault create `
    --name kv-customer-order-prod `
    --resource-group rg-customer-order-prod `
    --location eastus2 `
    --enable-rbac-authorization

# Generate strong password
$dbPassword = -join ((65..90) + (97..122) + (48..57) + (33,35,37,38,42) | Get-Random -Count 24 | % {[char]$_})

# Store password in Key Vault
az keyvault secret set `
    --vault-name kv-customer-order-prod `
    --name DB-ADMIN-PASSWORD `
    --value $dbPassword

# Deploy PostgreSQL
az deployment group create `
    --resource-group rg-customer-order-prod `
    --template-file infra/database.bicep `
    --parameters administratorPassword=$dbPassword

# Get server FQDN
az postgres flexible-server show `
    --resource-group rg-customer-order-prod `
    --name psql-customer-order-prod `
    --query fullyQualifiedDomainName -o tsv
```

**Effort:** 40 hours (20 hours with Azure portal guidance)

---

### Step 2.3: Migrate Database (DB2 → PostgreSQL)

**Using Azure Database Migration Service:**

```powershell
# Install PostgreSQL client tools
winget install PostgreSQL.PostgreSQL

# Export DB2 data to CSV
db2 connect to ORDERS
db2 "EXPORT TO c:\temp\orders.csv OF DEL SELECT * FROM ORDERS"
db2 "EXPORT TO c:\temp\lineitem.csv OF DEL SELECT * FROM LINEITEM"
# ... repeat for all tables

# Import to Azure PostgreSQL
$env:PGPASSWORD = (az keyvault secret show --vault-name kv-customer-order-prod --name DB-ADMIN-PASSWORD --query value -o tsv)

psql -h psql-customer-order-prod.postgres.database.azure.com -U dbadmin -d orders `
    -c "\COPY orders FROM 'c:\temp\orders.csv' CSV HEADER"

psql -h psql-customer-order-prod.postgres.database.azure.com -U dbadmin -d orders `
    -c "\COPY lineitem FROM 'c:\temp\lineitem.csv' CSV HEADER"
```

**Data Validation:**

```sql
-- Row count verification
SELECT 'ORDERS' AS table_name, COUNT(*) FROM orders
UNION ALL
SELECT 'LINEITEM', COUNT(*) FROM lineitem
UNION ALL
SELECT 'PRODUCT', COUNT(*) FROM product;

-- Expected:
-- ORDERS:          50,000
-- LINEITEM:        150,000
-- PRODUCT:         5,000
```

**Effort:** 80 hours (40 hours with Azure DMS automation)

---

### Step 2.4: Create Container Apps Environment

**Bicep Template (`infra/container-apps.bicep`):**

```bicep
param environmentName string = 'cae-customer-order-prod'
param location string = resourceGroup().location
param logAnalyticsWorkspaceName string = 'log-customer-order-prod'

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2022-10-01' = {
  name: logAnalyticsWorkspaceName
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 90
  }
}

resource containerAppEnv 'Microsoft.App/managedEnvironments@2023-05-01' = {
  name: environmentName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
    zoneRedundant: true
  }
}

output environmentId string = containerAppEnv.id
```

**Deploy:**

```powershell
az deployment group create `
    --resource-group rg-customer-order-prod `
    --template-file infra/container-apps.bicep
```

**Effort:** 16 hours

---

## Phase 3: Application Deployment (Week 7-8)

### Step 3.1: Create Managed Identity

```powershell
# Create user-assigned managed identity
az identity create `
    --resource-group rg-customer-order-prod `
    --name id-customer-order-api

# Get identity details
$identityId = az identity show `
    --resource-group rg-customer-order-prod `
    --name id-customer-order-api `
    --query id -o tsv

$principalId = az identity show `
    --resource-group rg-customer-order-prod `
    --name id-customer-order-api `
    --query principalId -o tsv

# Grant ACR pull permissions
az role assignment create `
    --assignee $principalId `
    --role AcrPull `
    --scope /subscriptions/<subscription-id>/resourceGroups/rg-customer-order-prod/providers/Microsoft.ContainerRegistry/registries/acrcustomerorderprod

# Grant Key Vault access
az role assignment create `
    --assignee $principalId `
    --role "Key Vault Secrets User" `
    --scope /subscriptions/<subscription-id>/resourceGroups/rg-customer-order-prod/providers/Microsoft.KeyVault/vaults/kv-customer-order-prod
```

**Effort:** 8 hours

---

### Step 3.2: Deploy Container App

**Bicep Template (`infra/app.bicep`):**

```bicep
param containerAppName string = 'ca-customer-order-api'
param location string = resourceGroup().location
param environmentId string
param containerImage string = 'acrcustomerorderprod.azurecr.io/customer-order-api:1.0.0'
param managedIdentityId string

resource containerApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: containerAppName
  location: location
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${managedIdentityId}': {}
    }
  }
  properties: {
    managedEnvironmentId: environmentId
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        targetPort: 8080
        transport: 'http'
        allowInsecure: false
        traffic: [
          {
            latestRevision: true
            weight: 100
          }
        ]
      }
      registries: [
        {
          server: 'acrcustomerorderprod.azurecr.io'
          identity: managedIdentityId
        }
      ]
      secrets: [
        {
          name: 'db-password'
          keyVaultUrl: 'https://kv-customer-order-prod.vault.azure.net/secrets/DB-ADMIN-PASSWORD'
          identity: managedIdentityId
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'customer-order-api'
          image: containerImage
          resources: {
            cpu: json('2.0')
            memory: '4Gi'
          }
          env: [
            {
              name: 'DB_HOST'
              value: 'psql-customer-order-prod.postgres.database.azure.com'
            }
            {
              name: 'DB_NAME'
              value: 'orders'
            }
            {
              name: 'DB_USERNAME'
              value: 'dbadmin'
            }
            {
              name: 'DB_PASSWORD'
              secretRef: 'db-password'
            }
            {
              name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
              value: reference(applicationInsights.id, '2020-02-02').ConnectionString
            }
          ]
          probes: [
            {
              type: 'Liveness'
              httpGet: {
                path: '/actuator/health/liveness'
                port: 8080
              }
              initialDelaySeconds: 30
              periodSeconds: 10
            }
            {
              type: 'Readiness'
              httpGet: {
                path: '/actuator/health/readiness'
                port: 8080
              }
              initialDelaySeconds: 10
              periodSeconds: 5
            }
          ]
        }
      ]
      scale: {
        minReplicas: 2
        maxReplicas: 10
        rules: [
          {
            name: 'http-scaling'
            http: {
              metadata: {
                concurrentRequests: '100'
              }
            }
          }
        ]
      }
    }
  }
}

output appUrl string = containerApp.properties.configuration.ingress.fqdn
```

**Deploy:**

```powershell
$environmentId = az containerapp env show `
    --resource-group rg-customer-order-prod `
    --name cae-customer-order-prod `
    --query id -o tsv

$identityId = az identity show `
    --resource-group rg-customer-order-prod `
    --name id-customer-order-api `
    --query id -o tsv

az deployment group create `
    --resource-group rg-customer-order-prod `
    --template-file infra/app.bicep `
    --parameters environmentId=$environmentId managedIdentityId=$identityId

# Get app URL
az containerapp show `
    --resource-group rg-customer-order-prod `
    --name ca-customer-order-api `
    --query properties.configuration.ingress.fqdn -o tsv
```

**Effort:** 40 hours (20 hours with Bicep templates)

---

### Step 3.3: Configure CI/CD Pipeline (GitHub Actions)

**Location:** `.github/workflows/deploy-prod.yml`

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:

env:
  AZURE_SUBSCRIPTION_ID: ${{ secrets.AZURE_SUBSCRIPTION_ID }}
  RESOURCE_GROUP: rg-customer-order-prod
  CONTAINER_APP_NAME: ca-customer-order-api
  ACR_NAME: acrcustomerorderprod
  IMAGE_NAME: customer-order-api

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Login to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}
      
      - name: Login to ACR
        run: az acr login --name ${{ env.ACR_NAME }}
      
      - name: Build and push Docker image
        run: |
          IMAGE_TAG=${{ env.ACR_NAME }}.azurecr.io/${{ env.IMAGE_NAME }}:${{ github.sha }}
          docker build -t $IMAGE_TAG .
          docker push $IMAGE_TAG
      
      - name: Deploy to Container Apps
        run: |
          az containerapp update \
            --name ${{ env.CONTAINER_APP_NAME }} \
            --resource-group ${{ env.RESOURCE_GROUP }} \
            --image ${{ env.ACR_NAME }}.azurecr.io/${{ env.IMAGE_NAME }}:${{ github.sha }}
      
      - name: Smoke Test
        run: |
          APP_URL=$(az containerapp show \
            --name ${{ env.CONTAINER_APP_NAME }} \
            --resource-group ${{ env.RESOURCE_GROUP }} \
            --query properties.configuration.ingress.fqdn -o tsv)
          
          curl -f https://$APP_URL/actuator/health || exit 1
```

**Effort:** 60 hours (30 hours with GitHub Actions templates)

---

## Phase 4: Validation & Cutover (Week 9-10)

### Step 4.1: Load Testing

**Using Azure Load Testing:**

```powershell
# Install Azure Load Testing CLI extension
az extension add --name load

# Create load test resource
az load create `
    --resource-group rg-customer-order-prod `
    --name load-customer-order-test `
    --location eastus2

# Upload JMeter test plan
az load test create `
    --load-test-resource load-customer-order-test `
    --resource-group rg-customer-order-prod `
    --test-id customer-order-load-test `
    --display-name "Customer Order API Load Test" `
    --test-plan load-test.jmx `
    --engine-instances 5

# Run load test
az load test start `
    --load-test-resource load-customer-order-test `
    --resource-group rg-customer-order-prod `
    --test-id customer-order-load-test
```

**Load Test Scenario (`load-test.jmx`):**
- **Target:** 1,000 concurrent users
- **Ramp-up:** 5 minutes
- **Duration:** 30 minutes
- **Expected p95:** <200ms
- **Expected throughput:** >500 RPS

**Effort:** 80 hours (40 hours with Azure Load Testing automation)

---

### Step 4.2: Production Cutover

**Cutover Checklist:**

- [ ] All smoke tests passing
- [ ] Load tests passing (p95 <200ms)
- [ ] Security scan completed (zero critical findings)
- [ ] Backup of WebSphere and DB2 completed
- [ ] DNS/load balancer ready to switch
- [ ] Rollback plan tested
- [ ] Stakeholders notified
- [ ] Maintenance window scheduled (e.g., Saturday 2 AM - 6 AM)

**Cutover Steps:**

```powershell
# 1. Final database sync (if using hybrid period)
# ... (database replication commands)

# 2. Stop WebSphere (prevents new writes)
# ... (WebSphere stop commands)

# 3. Final data migration
# ... (incremental data sync)

# 4. Switch DNS/load balancer to Azure
# Update DNS record to point to Container Apps URL
# Or update load balancer configuration

# 5. Monitor Azure application
az containerapp logs show `
    --resource-group rg-customer-order-prod `
    --name ca-customer-order-api `
    --follow

# 6. Validate
curl https://<container-app-url>/actuator/health
curl https://<container-app-url>/api/products | jq
```

**Effort:** 60 hours (30 hours with automation)

---

## Rollback Procedures

### Scenario: Critical Issue in Azure Environment

**RTO Target:** 4 hours

```powershell
# Step 1: Immediately revert DNS/load balancer (5 minutes)
# Update DNS to point back to WebSphere IHS/HTTP Server

# Step 2: Restart WebSphere (if stopped) (10 minutes)
/opt/IBM/WebSphere/AppServer/profiles/AppSrv01/bin/startServer.sh <servername>

# Step 3: Restore DB2 from backup (if database was migrated) (2 hours)
db2 restore database ORDERS from /backup/path taken at <timestamp>

# Step 4: Validate WebSphere environment (30 minutes)
curl http://<websphere-url>/CustomerOrderServicesWeb/api/products

# Step 5: Notify stakeholders
# Send email/Slack notification of rollback
```

**Rollback Success Criteria:**
- [ ] WebSphere application responding
- [ ] DB2 database operational
- [ ] All transactions processing normally
- [ ] User traffic restored

---

## Troubleshooting Guide

### Issue: Container App Not Starting

**Symptoms:**
- Container app status: "Provisioning failed"
- Logs show: "CrashLoopBackOff"

**Diagnosis:**

```powershell
# Check container logs
az containerapp logs show `
    --resource-group rg-customer-order-prod `
    --name ca-customer-order-api `
    --tail 100

# Common errors:
# 1. Database connection failure
# 2. Missing environment variables
# 3. Out of memory (Java heap)
```

**Resolution:**

```powershell
# Fix: Database connection
# Verify firewall rules allow Container Apps to PostgreSQL
az postgres flexible-server firewall-rule create `
    --resource-group rg-customer-order-prod `
    --name psql-customer-order-prod `
    --rule-name AllowContainerApps `
    --start-ip-address <container-apps-outbound-ip>

# Fix: Missing environment variables
# Update container app configuration
az containerapp update `
    --resource-group rg-customer-order-prod `
    --name ca-customer-order-api `
    --set-env-vars "DB_HOST=psql-customer-order-prod.postgres.database.azure.com"

# Fix: Out of memory
# Increase container memory
az containerapp update `
    --resource-group rg-customer-order-prod `
    --name ca-customer-order-api `
    --cpu 2.0 --memory 4.0Gi
```

---

### Issue: Slow Performance (p95 >500ms)

**Diagnosis:**

```powershell
# Check Application Insights
az monitor app-insights metrics show `
    --resource-group rg-customer-order-prod `
    --app <app-insights-name> `
    --metric requests/duration `
    --aggregation avg

# Check database performance
az postgres flexible-server show `
    --resource-group rg-customer-order-prod `
    --name psql-customer-order-prod
```

**Resolution:**

```powershell
# 1. Increase database SKU
az postgres flexible-server update `
    --resource-group rg-customer-order-prod `
    --name psql-customer-order-prod `
    --sku-name Standard_D16s_v4  # 16 vCPU, 64 GB RAM

# 2. Add database indexes
psql -h psql-customer-order-prod.postgres.database.azure.com -U dbadmin -d orders <<EOF
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_lineitem_order_id ON lineitem(order_id);
EOF

# 3. Enable connection pooling
# Update application.yml:
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

---

## Summary

**Total Effort Estimate:**

| Phase | Manual Hours | AI-Assisted Hours |
|-------|--------------|-------------------|
| Phase 0: Setup & Analysis | 60 | 34 |
| Phase 1: Containerization | 120 | 60 |
| Phase 2: Azure Provisioning | 160 | 80 |
| Phase 3: Deployment | 180 | 90 |
| Phase 4: Validation | 140 | 70 |
| **TOTAL** | **660** | **334** |

**Timeline:** 8-10 weeks (2 developers, 50% AI-assisted efficiency)

**Next Document:** [Framework Migration Guide](./FRAMEWORK_MIGRATION_GUIDE.md)

