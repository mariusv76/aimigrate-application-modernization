# Docker Build and Deployment Guide

## Quick Start - Local Testing

### Prerequisites
- Docker Desktop installed and running
- Access to workspace root: `c:\AIMigrate\java\appmodernization-samples\monolith-websphere-855`

### Build and Run Locally

```powershell
# Navigate to workspace root
cd c:\AIMigrate\java\appmodernization-samples\monolith-websphere-855

# Start PostgreSQL and application
docker compose -f Deployment/docker-compose.yml up --build

# Wait for services to start (application takes ~2 minutes)
# Application available at: http://localhost:9080/CustomerOrderServicesWeb/

# View logs
docker compose -f Deployment/docker-compose.yml logs -f customerorder-api

# Stop services
docker compose -f Deployment/docker-compose.yml down
```

### Test Endpoints

```powershell
# Health check
curl http://localhost:9080/health

# Get customer endpoint (business customer should exist from init scripts)
curl http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Customer/1

# API documentation
curl http://localhost:9080/openapi
```

## Azure Container Registry Deployment

### Step 1: Build for Production

```powershell
# Build production image
docker build -f Deployment/Dockerfile.liberty -t customerorder-api:latest .

# Tag for ACR
docker tag customerorder-api:latest customerorderdevacr.azurecr.io/customerorder-api:latest
docker tag customerorder-api:latest customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
```

### Step 2: Configure Managed Identity Role Assignments

```powershell
# Get Container App managed identity principal ID
$principalId = az containerapp show `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --query identity.principalId -o tsv

# Grant Key Vault Secrets User role
az role assignment create `
  --role "Key Vault Secrets User" `
  --assignee $principalId `
  --scope "/subscriptions/45ecdd10-c932-4ba1-a3f7-10d52d7ce592/resourceGroups/rg-customerorder-dev/providers/Microsoft.KeyVault/vaults/kv-customerorder-dev"

# Grant App Configuration Data Reader role
az role assignment create `
  --role "App Configuration Data Reader" `
  --assignee $principalId `
  --scope "/subscriptions/45ecdd10-c932-4ba1-a3f7-10d52d7ce592/resourceGroups/rg-customerorder-dev/providers/Microsoft.AppConfiguration/configurationStores/appconfig-customerorder"

# Grant AcrPull role
az role assignment create `
  --role "AcrPull" `
  --assignee $principalId `
  --scope "/subscriptions/45ecdd10-c932-4ba1-a3f7-10d52d7ce592/resourceGroups/rg-customerorder-dev/providers/Microsoft.ContainerRegistry/registries/customerorderdevacr"
```

### Step 3: Push to ACR

```powershell
# Login to ACR
az acr login --name customerorderdevacr

# Push images
docker push customerorderdevacr.azurecr.io/customerorder-api:latest
docker push customerorderdevacr.azurecr.io/customerorder-api:v1.0.0
```

### Step 4: Update Container App

```powershell
# Update Container App with new image
az containerapp update `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --image customerorderdevacr.azurecr.io/customerorder-api:v1.0.0 `
  --set-env-vars `
    "AZURE_KEYVAULT_ENDPOINT=https://kv-customerorder-dev.vault.azure.net/" `
    "AZURE_APPCONFIGURATION_ENDPOINT=https://appconfig-customerorder.azconfig.io" `
    "AZURE_TENANT_ID=89cea9ee-73b0-4f74-9541-b8153dff5960" `
    "AZURE_CLIENT_ID=3d3c655f-43b5-4130-816c-74493287e79b"

# Get application URL
az containerapp show `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --query properties.configuration.ingress.fqdn -o tsv
```

## Troubleshooting

### View Container App Logs

```powershell
# Stream logs
az containerapp logs show `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --follow

# View recent logs
az containerapp logs show `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --tail 100
```

### Check OpenTelemetry Integration

```powershell
# Verify OTEL agent is loaded
az containerapp logs show `
  --name ca-customerorder-dev `
  --resource-group rg-customerorder-dev `
  --query "[?contains(message, 'opentelemetry')]"

# Check Application Insights connection
az monitor app-insights component show `
  --app customerorder-insights `
  --resource-group rg-customerorder-dev `
  --query connectionString -o tsv
```

### Local Debug Mode

```powershell
# Run container interactively to debug
docker run -it --rm `
  -p 9080:9080 `
  -e OTEL_TRACES_EXPORTER=logging `
  -e DB_HOST=host.docker.internal `
  -e DB_PORT=5432 `
  -e DB_NAME=orderdb `
  -e DB_USER=dbuser `
  -e DB_PASSWORD=dbpass123 `
  customerorder-api:latest
```

## Docker Image Details

### Base Image
- `icr.io/appcafe/open-liberty:24.0.0.11-full-java17-openj9`
- Full Liberty profile with all Jakarta EE 10 features
- OpenJ9 JVM for optimized container performance

### OpenTelemetry Agent
- Version: 2.10.0
- Location: `/opt/otel/opentelemetry-javaagent.jar`
- Auto-instrumentation for:
  - JAX-RS endpoints
  - JDBC database calls
  - EJB invocations
  - JMS messaging
  - HTTP client calls

### Environment Variables

| Variable | Purpose | Source |
|----------|---------|--------|
| `OTEL_JAVAAGENT_ENABLED` | Enable OTEL agent | Dockerfile |
| `OTEL_SERVICE_NAME` | Service identifier | Dockerfile/ContainerApp |
| `OTEL_TRACES_SAMPLER` | Trace sampling strategy | Dockerfile |
| `OTEL_TRACES_SAMPLER_ARG` | Sampling rate (0.1 = 10%) | Dockerfile |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | App Insights export | Container App |
| `AZURE_KEYVAULT_ENDPOINT` | Key Vault URI | Container App |
| `AZURE_APPCONFIGURATION_ENDPOINT` | App Config URI | Container App |
| `AZURE_TENANT_ID` | Entra ID tenant | Container App |
| `AZURE_CLIENT_ID` | Entra ID app client ID | Container App |

### Ports
- **9080**: HTTP (application)
- **9443**: HTTPS (if configured)

### Health Check
- Endpoint: `http://localhost:9080/health`
- Interval: 30 seconds
- Timeout: 5 seconds
- Start period: 120 seconds (application startup time)
- Retries: 3

## Build Optimization

### Multi-Stage Build Benefits
1. **Stage 1 (Builder)**: Compiles application with Maven, downloads dependencies
2. **Stage 2 (OTEL Downloader)**: Downloads OpenTelemetry agent independently
3. **Stage 3 (Runtime)**: Only includes runtime artifacts (Liberty + app + agent)

### Size Optimization
- Uses Alpine-based images where possible
- Leverages Docker layer caching
- `.dockerignore` excludes build artifacts and dev files
- Final image: ~1.2 GB (Liberty + JRE + App + OTEL)

## Security Considerations

### Non-Root User
- Container runs as user `1001` (Liberty default)
- All files owned by `1001:0` (user:root group)

### Managed Identity
- Uses `DefaultAzureCredential` for authentication
- No secrets in environment variables (except database password retrieved from Key Vault)
- RBAC-based access to Azure resources

### Network Security
- Application Insights uses OTLP over HTTPS
- Key Vault/App Configuration accessed via HTTPS
- Container Apps provides built-in TLS termination

## Next Steps

1. Test local build with Docker Compose
2. Configure managed identity role assignments
3. Push to ACR and deploy to Container Apps
4. Validate telemetry in Application Insights
5. Test configuration retrieval from Key Vault/App Configuration
