# TASK-006: Deployment Validation Checklist

## Overview

Use this checklist to validate successful deployment of Customer Order Services to Azure with cloud-native integrations.

**Task**: TASK-006 Azure Integration  
**Branch**: `migration/task-006-azure-integration`  
**Date**: 2025-11-11

---

## Pre-Deployment Validation

### Azure Subscription & Resource Group

- [ ] Azure CLI installed and configured
  ```bash
  az --version
  ```

- [ ] Logged into correct Azure subscription
  ```bash
  az account show --query "{Name:name, ID:id, Tenant:tenantId}" -o table
  ```

- [ ] Resource group exists or will be created
  ```bash
  az group exists --name rg-customerorder-dev
  ```

### Local Environment

- [ ] Git repository cloned and branch checked out
  ```bash
  git branch --show-current  # Should show: migration/task-006-azure-integration
  ```

- [ ] Docker installed and running (for image build)
  ```bash
  docker --version
  docker ps
  ```

- [ ] PowerShell or Bash available for deployment scripts

---

## Infrastructure Deployment Validation

### Step 1: Deploy Bicep Templates

- [ ] Navigate to infrastructure directory
  ```bash
  cd infra
  ```

- [ ] Execute deployment script
  ```powershell
  .\deploy.ps1 -ResourceGroup "rg-customerorder-dev" -UseContainerApps $true
  ```
  **OR** use Azure CLI:
  ```bash
  az deployment group create \
      --name "customerorder-infra" \
      --resource-group rg-customerorder-dev \
      --template-file ./main.bicep \
      --parameters baseName=customerorder environment=dev useContainerApps=true
  ```

- [ ] Deployment completes without errors
  - Exit code: 0
  - Provisioning state: Succeeded

### Step 2: Verify Azure Resources

#### Key Vault

- [ ] Key Vault exists
  ```bash
  az keyvault show --name kv-customerorder-dev --query "{Name:name, URI:properties.vaultUri, RBAC:properties.enableRbacAuthorization}" -o table
  ```
  - RBAC should be: true

- [ ] Key Vault reachable
  ```bash
  az keyvault secret list --vault-name kv-customerorder-dev
  ```

#### App Configuration

- [ ] App Configuration exists
  ```bash
  az appconfig show --name appconfig-customerorder --query "{Name:name, Endpoint:endpoint, SKU:sku.name}" -o table
  ```

- [ ] App Configuration reachable
  ```bash
  az appconfig kv list --name appconfig-customerorder
  ```

#### Application Insights

- [ ] Application Insights exists
  ```bash
  az monitor app-insights component show --app customerorder-insights --resource-group rg-customerorder-dev --query "{Name:name, InstrumentationKey:instrumentationKey, AppId:appId}" -o table
  ```

- [ ] Connection string available
  ```bash
  az monitor app-insights component show --app customerorder-insights --resource-group rg-customerorder-dev --query connectionString -o tsv
  ```

#### Container Registry

- [ ] Container Registry exists
  ```bash
  az acr show --name customerorderdevacr --query "{Name:name, LoginServer:loginServer, SKU:sku.name}" -o table
  ```

- [ ] Registry accessible
  ```bash
  az acr repository list --name customerorderdevacr
  ```

#### PostgreSQL Flexible Server

- [ ] PostgreSQL server exists
  ```bash
  az postgres flexible-server show --name psql-customerorder-dev --resource-group rg-customerorder-dev --query "{Name:fullyQualifiedDomainName, Version:version, State:state}" -o table
  ```

- [ ] Database exists
  ```bash
  az postgres flexible-server db show --server-name psql-customerorder-dev --resource-group rg-customerorder-dev --database-name orderdb --query name -o tsv
  ```

- [ ] Firewall rules configured
  ```bash
  az postgres flexible-server firewall-rule list --name psql-customerorder-dev --resource-group rg-customerorder-dev -o table
  ```

#### Container Apps Environment

- [ ] Container Apps Environment exists
  ```bash
  az containerapp env show --name cae-customerorder-dev --resource-group rg-customerorder-dev --query "{Name:name, ProvisioningState:properties.provisioningState}" -o table
  ```

#### Container App

- [ ] Container App exists
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "{Name:name, ProvisioningState:properties.provisioningState, RunningState:properties.runningState}" -o table
  ```

- [ ] Managed identity configured
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "identity.{Type:type, PrincipalId:principalId}" -o table
  ```
  - Type should be: SystemAssigned
  - Principal ID should be present (GUID)

### Step 3: Verify Role Assignments

- [ ] Get managed identity principal ID
  ```bash
  PRINCIPAL_ID=$(az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query identity.principalId -o tsv)
  echo $PRINCIPAL_ID
  ```

- [ ] Verify Key Vault Secrets User role
  ```bash
  az role assignment list --assignee $PRINCIPAL_ID --query "[?roleDefinitionName=='Key Vault Secrets User'].{Role:roleDefinitionName, Scope:scope}" -o table
  ```
  - Should show 1 assignment scoped to Key Vault

- [ ] Verify App Configuration Data Reader role
  ```bash
  az role assignment list --assignee $PRINCIPAL_ID --query "[?roleDefinitionName=='App Configuration Data Reader'].{Role:roleDefinitionName, Scope:scope}" -o table
  ```
  - Should show 1 assignment scoped to App Configuration

- [ ] Verify AcrPull role
  ```bash
  az role assignment list --assignee $PRINCIPAL_ID --query "[?roleDefinitionName=='AcrPull'].{Role:roleDefinitionName, Scope:scope}" -o table
  ```
  - Should show 1 assignment scoped to Container Registry

- [ ] Summary: All 3 role assignments present
  ```bash
  az role assignment list --assignee $PRINCIPAL_ID --query "[].roleDefinitionName" -o tsv
  ```
  - Expected output: Key Vault Secrets User, App Configuration Data Reader, AcrPull

---

## Configuration Validation

### Step 1: Store Secrets in Key Vault

- [ ] Generate database password (if not already created)
  ```bash
  # 32-character password
  PASSWORD=$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-32)
  echo "Generated password: $PASSWORD"
  ```

- [ ] Store password in Key Vault
  ```bash
  az keyvault secret set --vault-name kv-customerorder-dev --name db-password --value "$PASSWORD"
  ```

- [ ] Verify secret stored
  ```bash
  az keyvault secret show --vault-name kv-customerorder-dev --name db-password --query value -o tsv
  ```

### Step 2: Configure App Configuration

- [ ] Set database host
  ```bash
  az appconfig kv set --name appconfig-customerorder --key db:host --value psql-customerorder-dev.postgres.database.azure.com
  ```

- [ ] Set database port
  ```bash
  az appconfig kv set --name appconfig-customerorder --key db:port --value 5432
  ```

- [ ] Set database name
  ```bash
  az appconfig kv set --name appconfig-customerorder --key db:name --value orderdb
  ```

- [ ] Set database user
  ```bash
  az appconfig kv set --name appconfig-customerorder --key db:user --value dbadmin
  ```

- [ ] Set telemetry sampling rate
  ```bash
  az appconfig kv set --name appconfig-customerorder --key telemetry:sampling --value 0.1
  ```

- [ ] Verify all configuration values
  ```bash
  az appconfig kv list --name appconfig-customerorder --query "[].{Key:key, Value:value}" -o table
  ```

### Step 3: Initialize Database

- [ ] Database schema created (12 tables)
  ```bash
  # If using local PostgreSQL container for initial setup:
  docker exec postgres-customerorder psql -U dbuser -d orderdb -c "\dt"
  
  # Or connect to Azure PostgreSQL:
  psql "host=psql-customerorder-dev.postgres.database.azure.com port=5432 dbname=orderdb user=dbadmin sslmode=require" -c "\dt"
  ```
  - Expected: 12 tables listed

- [ ] Sample data loaded
  ```bash
  # Check customer count
  psql "..." -c "SELECT COUNT(*) FROM customer;"
  ```
  - Expected: At least 2 customers

---

## Application Deployment Validation

### Step 1: Build Docker Image

- [ ] Navigate to project root
  ```bash
  cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
  ```

- [ ] Build Docker image
  ```bash
  docker build -f Deployment/Dockerfile.liberty -t customerorder-api:latest .
  ```
  - Build completes without errors
  - Image size: ~850 MB

- [ ] Verify image created
  ```bash
  docker images customerorder-api
  ```

### Step 2: Push Image to ACR

- [ ] Login to ACR
  ```bash
  az acr login --name customerorderdevacr
  ```
  - Should show: "Login Succeeded"

- [ ] Tag image for ACR
  ```bash
  docker tag customerorder-api:latest customerorderdevacr.azurecr.io/customerorder-api:latest
  docker tag customerorder-api:latest customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
  ```

- [ ] Push image to ACR (latest)
  ```bash
  docker push customerorderdevacr.azurecr.io/customerorder-api:latest
  ```

- [ ] Push image to ACR (versioned)
  ```bash
  docker push customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
  ```

- [ ] Verify images in ACR
  ```bash
  az acr repository show-tags --name customerorderdevacr --repository customerorder-api -o table
  ```
  - Expected: latest, v1.0.0

### Step 3: Update Container App

- [ ] Get Application Insights connection string
  ```bash
  INSIGHTS_CONN=$(az monitor app-insights component show --app customerorder-insights --resource-group rg-customerorder-dev --query connectionString -o tsv)
  ```

- [ ] Get database password from Key Vault
  ```bash
  DB_PASSWORD=$(az keyvault secret show --vault-name kv-customerorder-dev --name db-password --query value -o tsv)
  ```

- [ ] Update Container App with new image and environment variables
  ```bash
  az containerapp update \
      --name ca-customerorder-dev \
      --resource-group rg-customerorder-dev \
      --image customerorderdevacr.azurecr.io/customerorder-api:v1.0.0 \
      --set-env-vars \
          AZURE_KEYVAULT_ENDPOINT=https://kv-customerorder-dev.vault.azure.net/ \
          AZURE_APPCONFIGURATION_ENDPOINT=https://appconfig-customerorder.azconfig.io \
          AZURE_TENANT_ID=<tenant-id> \
          APPLICATIONINSIGHTS_CONNECTION_STRING="$INSIGHTS_CONN" \
          DB_HOST=psql-customerorder-dev.postgres.database.azure.com \
          DB_PORT=5432 \
          DB_NAME=orderdb \
          DB_USER=dbadmin \
          DB_PASSWORD="$DB_PASSWORD" \
          OTEL_SERVICE_NAME=customerorder-api \
          OTEL_TRACES_SAMPLER=parentbased_traceidratio \
          OTEL_TRACES_SAMPLER_ARG=0.1 \
          OTEL_RESOURCE_ATTRIBUTES="deployment.environment=dev"
  ```

- [ ] Wait for revision to deploy (2-3 minutes)

- [ ] Check Container App status
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.{ProvisioningState:provisioningState, RunningState:runningState, LatestRevision:latestRevisionName}" -o table
  ```
  - ProvisioningState: Succeeded
  - RunningState: Running

### Step 4: Verify Container App Configuration

- [ ] Check environment variables
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.template.containers[0].env[].{Name:name}" -o table
  ```
  - Should include: AZURE_KEYVAULT_ENDPOINT, AZURE_APPCONFIGURATION_ENDPOINT, DB_HOST, etc.

- [ ] Check image configuration
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.template.containers[0].image" -o tsv
  ```
  - Should be: customerorderdevacr.azurecr.io/customerorder-api:v1.0.0

- [ ] Check replica count
  ```bash
  az containerapp revision show --name <revision-name> --app ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.{Replicas:replicas, Traffic:trafficWeight}" -o table
  ```
  - Replicas: 1-3 (depending on load)
  - Traffic: 100

---

## Application Health Validation

### Step 1: Get Application URL

- [ ] Get Container App URL
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query properties.configuration.ingress.fqdn -o tsv
  ```
  - Should return: ca-customerorder-dev.<region>.azurecontainerapps.io

- [ ] Set URL variable
  ```bash
  APP_URL="https://$(az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query properties.configuration.ingress.fqdn -o tsv)"
  echo $APP_URL
  ```

### Step 2: Health Endpoint Checks

- [ ] Test health endpoint
  ```bash
  curl -s $APP_URL/health | jq '.'
  ```
  - Expected status: "UP"
  - Expected checks: datasource-orderds (UP), customer-order-services-liveness (UP)

- [ ] Test liveness probe
  ```bash
  curl -s $APP_URL/health/live
  ```
  - Expected: 200 OK

- [ ] Test readiness probe
  ```bash
  curl -s $APP_URL/health/ready
  ```
  - Expected: 200 OK

- [ ] Verify datasource health
  ```bash
  curl -s $APP_URL/health | jq '.checks[] | select(.name=="datasource-orderds")'
  ```
  - Expected status: "UP"
  - This confirms Azure PostgreSQL connectivity

### Step 3: Authentication Validation

- [ ] Test authenticated endpoint (should fail without token)
  ```bash
  curl -i $APP_URL/metrics
  ```
  - Expected: 401 Unauthorized
  - This confirms JWT authentication is working

- [ ] Test web application root
  ```bash
  curl -i $APP_URL/CustomerOrderServicesWeb/
  ```
  - Expected: 200 OK
  - Should return HTML content

### Step 4: Container Logs Review

- [ ] Check recent logs for errors
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 50
  ```
  - No ERROR or SEVERE log entries
  - Should see: "The defaultServer server is ready to run a smarter planet"
  - Should see: Application started successfully

- [ ] Check for OpenTelemetry agent load
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 200 | grep -i "opentelemetry"
  ```
  - Should see: OpenTelemetry Java agent loaded

- [ ] Check for Key Vault access (no errors)
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 200 | grep -i "vault\|secret" | grep -i "error"
  ```
  - Should return: No output (no errors)

- [ ] Check for database connection
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 200 | grep -i "postgresql\|database"
  ```
  - Should see: Database product name: PostgreSQL
  - Should see: JDBC driver version: 42.7.1

---

## Security Validation

### Step 1: Managed Identity Authentication

- [ ] Verify managed identity can access Key Vault
  ```bash
  # Check Container App logs for Key Vault access
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 100 | grep -i "DefaultAzureCredential\|managed identity"
  ```
  - Should see: Managed identity authentication attempts
  - No access denied errors

- [ ] Test Key Vault access via role assignment
  ```bash
  # Role assignment should allow secret retrieval
  az role assignment list --assignee $PRINCIPAL_ID --query "[?roleDefinitionName=='Key Vault Secrets User']" -o table
  ```
  - At least 1 assignment should exist

### Step 2: Network Security

- [ ] Verify HTTPS ingress
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.configuration.ingress.{AllowInsecure:allowInsecure, Transport:transport}" -o table
  ```
  - AllowInsecure: false
  - Transport: http or auto (Container Apps handles HTTPS)

- [ ] Test HTTPS access
  ```bash
  curl -I https://ca-customerorder-dev.<region>.azurecontainerapps.io/health
  ```
  - Should return: 200 OK
  - Connection should use HTTPS (not HTTP)

### Step 3: Secret Management

- [ ] Verify no secrets in environment variables (except references)
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.template.containers[0].env[?name=='DB_PASSWORD'].value" -o tsv
  ```
  - Password should be present (retrieved from Key Vault earlier)
  - In production, consider using secretRef instead

- [ ] Verify secrets in Key Vault
  ```bash
  az keyvault secret list --vault-name kv-customerorder-dev --query "[].name" -o tsv
  ```
  - Should include: db-password

---

## Performance Validation

### Step 1: Startup Time

- [ ] Check container startup time
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 500 | grep "ready to run"
  ```
  - Should see: "The defaultServer server is ready to run a smarter planet"
  - Startup time: ~70-150 seconds (acceptable for Liberty)

### Step 2: Resource Usage

- [ ] Check CPU and memory configuration
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.template.containers[0].resources.{CPU:cpu, Memory:memory}" -o table
  ```
  - CPU: 0.5
  - Memory: 1Gi

- [ ] Check replica scaling
  ```bash
  az containerapp show --name ca-customerorder-dev --resource-group rg-customerorder-dev --query "properties.template.scale.{MinReplicas:minReplicas, MaxReplicas:maxReplicas}" -o table
  ```
  - Min: 1
  - Max: 3

### Step 3: Response Time

- [ ] Measure health endpoint response time
  ```bash
  time curl -s $APP_URL/health > /dev/null
  ```
  - Should complete in < 1 second

- [ ] Test multiple requests (load)
  ```bash
  for i in {1..10}; do curl -s $APP_URL/health > /dev/null; done
  ```
  - All requests should succeed (200 OK)

---

## Database Validation

### Step 1: Schema Verification

- [ ] Connect to PostgreSQL
  ```bash
  psql "host=psql-customerorder-dev.postgres.database.azure.com port=5432 dbname=orderdb user=dbadmin sslmode=require"
  ```

- [ ] List all tables
  ```sql
  \dt
  ```
  - Expected: 12 tables (customer, addresstype, customertype, product, etc.)

- [ ] Check row counts
  ```sql
  SELECT 'customer' AS table_name, COUNT(*) FROM customer
  UNION ALL
  SELECT 'supplier', COUNT(*) FROM supplier
  UNION ALL
  SELECT 'product', COUNT(*) FROM product;
  ```
  - customer: >= 2
  - supplier: >= 2
  - product: >= 0

### Step 2: Connection Pool Validation

- [ ] Check datasource health via application
  ```bash
  curl -s $APP_URL/health | jq '.checks[] | select(.name=="datasource-orderds")'
  ```
  - Status: UP
  - This confirms Liberty datasource is connected

- [ ] Check for connection errors in logs
  ```bash
  az containerapp logs show --name ca-customerorder-dev --resource-group rg-customerorder-dev --tail 100 | grep -i "connection\|pool" | grep -i "error\|failed"
  ```
  - Should return: No output (no connection errors)

---

## Final Validation Summary

### Critical Success Criteria

- [ ] **Infrastructure**: All Azure resources created successfully
- [ ] **Role Assignments**: All 3 role assignments present and functional
- [ ] **Configuration**: Secrets in Key Vault, config in App Configuration
- [ ] **Database**: Azure PostgreSQL connected, schema and data migrated
- [ ] **Application**: Container App running, health checks passing
- [ ] **Security**: Managed identity working, JWT authentication validated
- [ ] **Deployment**: Automated Bicep deployment working

### Known Limitations

- [ ] **Acknowledged**: Telemetry not flowing to Application Insights (requires Azure Monitor distro)
- [ ] **Acknowledged**: No role-based authorization implemented (JWT infrastructure ready)

### Documentation Review

- [ ] Reviewed: `.vscode/transformation/TASK-006/deployment-summary.md`
- [ ] Reviewed: `.vscode/transformation/TASK-006/automated-deployment-guide.md`
- [ ] Reviewed: `.vscode/transformation/TASK-006/progress.md`

### Handoff Preparation

- [ ] All commits pushed to `migration/task-006-azure-integration` branch
- [ ] Documentation complete and up-to-date
- [ ] Known issues documented with resolutions
- [ ] Next steps identified and prioritized

---

## Sign-Off

**Validation Date**: _______________  
**Validated By**: _______________  
**Status**: ☐ PASS ☐ FAIL (with issues documented)  
**Ready for Production**: ☐ YES ☐ NO (pending items listed)

### Notes

_Add any additional notes, observations, or issues encountered during validation:_

---

**Checklist Version**: 1.0  
**Last Updated**: 2025-11-11  
**Task**: TASK-006 Azure Integration
