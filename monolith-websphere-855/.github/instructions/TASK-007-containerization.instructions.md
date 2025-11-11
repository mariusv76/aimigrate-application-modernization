# Migration Task Prompt: Containerization & Azure Deployment

## Mission
You are an AI agent tasked with performing **TASK-007** for the Customer Order Services application: **Containerize Application and Deploy to Azure Container Apps** with full CI/CD pipeline.

## Task Overview
**Task ID:** TASK-007  
**Task Name:** Containerization & Azure Deployment  
**Priority:** 🟡 HIGH  
**Dependencies:** All previous tasks (TASK-001 through TASK-006) completed  
**Estimated Effort:** 564 hours manual / 277 hours AI-assisted  
**Success Criteria:** Application running in Azure Container Apps, CI/CD pipeline operational, monitoring configured

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/PLATFORM_MIGRATION_GUIDE.md` (containerization sections)
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 3
   - Review existing `Deployment/Dockerfile`

2. **Create detailed execution plan**
   - Document containerization strategy
   - Plan Azure Container Apps architecture
   - Design CI/CD pipeline
   - List infrastructure-as-code requirements
   - Save plan to `.vscode/transformation/TASK-007/plan.md`

3. **Get user confirmation** before proceeding

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-007-containerization`
   - Branch from: current branch (agent-test or your working branch)
   - Commit message: "chore: create branch for containerization task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-007/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-007/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation - Containerization

6. **Create Optimized Dockerfile**
   - Create multi-stage `Dockerfile`:
     ```dockerfile
     # Stage 1: Build
     FROM maven:3.9-eclipse-temurin-17 AS build
     WORKDIR /app
     COPY pom.xml .
     COPY */pom.xml ./
     RUN mvn dependency:go-offline
     COPY . .
     RUN mvn clean package -DskipTests
     
     # Stage 2: Runtime
     FROM eclipse-temurin:17-jre-alpine
     WORKDIR /app
     COPY --from=build /app/target/*.jar app.jar
     
     # Non-root user
     RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser
     USER appuser
     
     # Health check
     HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
       CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
     
     EXPOSE 8080
     ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
     ```
   - Commit: "feat: create optimized multi-stage Dockerfile"

7. **Create .dockerignore**
   - Add `.dockerignore`:
     ```
     target/
     .git/
     .gitignore
     *.md
     .transformation/
     .github/
     ```
   - Commit: "feat: add .dockerignore for optimized builds"

8. **Build and Test Container Locally**
   - Build image:
     ```powershell
     docker build -t customerorder-api:latest .
     ```
   - Run container:
     ```powershell
     docker run -p 8080:8080 `
       -e AZURE_TENANT_ID=$env:AZURE_TENANT_ID `
       -e AZURE_CLIENT_ID=$env:AZURE_CLIENT_ID `
       -e DB_HOST=host.docker.internal `
       customerorder-api:latest
     ```
   - Test health endpoint: `http://localhost:8080/actuator/health`
   - Document in progress file

### Phase 4: Infrastructure-as-Code

9. **Create Bicep Templates**
   - Create `infra/main.bicep`:
     ```bicep
     param location string = resourceGroup().location
     param environmentName string = 'dev'
     
     // Container Registry
     resource acr 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' = {
       name: 'acrcustomerorder${environmentName}'
       location: location
       sku: { name: 'Basic' }
       properties: { adminUserEnabled: true }
     }
     
     // Container Apps Environment
     resource containerAppEnv 'Microsoft.App/managedEnvironments@2023-05-01' = {
       name: 'cae-customerorder-${environmentName}'
       location: location
       properties: {
         appLogsConfiguration: {
           destination: 'azure-monitor'
         }
       }
     }
     
     // Container App
     resource containerApp 'Microsoft.App/containerApps@2023-05-01' = {
       name: 'ca-customerorder-api'
       location: location
       properties: {
         managedEnvironmentId: containerAppEnv.id
         configuration: {
           ingress: {
             external: true
             targetPort: 8080
           }
         }
         template: {
           containers: [{
             name: 'api'
             image: '${acr.properties.loginServer}/customerorder-api:latest'
             resources: {
               cpu: json('2.0')
               memory: '4Gi'
             }
           }]
           scale: {
             minReplicas: 1
             maxReplicas: 10
           }
         }
       }
     }
     ```
   - Commit: "feat: add Bicep templates for Azure infrastructure"

### Phase 5: CI/CD Pipeline

10. **Create GitHub Actions Workflow**
    - Create `.github/workflows/azure-deploy.yml`:
      ```yaml
      name: Build and Deploy to Azure
      
      on:
        push:
          branches: [main]
        workflow_dispatch:
      
      env:
        AZURE_CONTAINER_REGISTRY: acrcustomerorderdev
        IMAGE_NAME: customerorder-api
      
      jobs:
        build:
          runs-on: ubuntu-latest
          steps:
            - uses: actions/checkout@v4
            
            - name: Set up JDK 17
              uses: actions/setup-java@v4
              with:
                java-version: '17'
                distribution: 'temurin'
                cache: 'maven'
            
            - name: Build with Maven
              run: mvn clean package -DskipTests
            
            - name: Run tests
              run: mvn test
            
            - name: Login to Azure
              uses: azure/login@v1
              with:
                creds: ${{ secrets.AZURE_CREDENTIALS }}
            
            - name: Build and push image
              run: |
                az acr build --registry ${{ env.AZURE_CONTAINER_REGISTRY }} \
                  --image ${{ env.IMAGE_NAME }}:${{ github.sha }} \
                  --image ${{ env.IMAGE_NAME }}:latest \
                  .
        
        deploy:
          needs: build
          runs-on: ubuntu-latest
          steps:
            - uses: actions/checkout@v4
            
            - name: Login to Azure
              uses: azure/login@v1
              with:
                creds: ${{ secrets.AZURE_CREDENTIALS }}
            
            - name: Deploy infrastructure
              uses: azure/arm-deploy@v1
              with:
                subscriptionId: ${{ secrets.AZURE_SUBSCRIPTION_ID }}
                resourceGroupName: rg-customerorder-dev
                template: ./infra/main.bicep
            
            - name: Update Container App
              run: |
                az containerapp update \
                  --name ca-customerorder-api \
                  --resource-group rg-customerorder-dev \
                  --image ${{ env.AZURE_CONTAINER_REGISTRY }}.azurecr.io/${{ env.IMAGE_NAME }}:${{ github.sha }}
      ```
    - Commit: "feat: add GitHub Actions CI/CD pipeline"

11. **Configure GitHub Secrets**
    - Document required secrets:
      - `AZURE_CREDENTIALS`
      - `AZURE_SUBSCRIPTION_ID`
      - `AZURE_TENANT_ID`
      - `AZURE_CLIENT_ID`
      - `AZURE_CLIENT_SECRET`
    - Commit: "docs: document required GitHub secrets"

### Phase 6: Deployment

12. **Deploy to Azure**
    - Trigger GitHub Actions workflow
    - Monitor deployment progress
    - Verify application running in Azure
    - Document deployment URL
    - Commit: "docs: document Azure deployment"

13. **Configure Autoscaling**
    - Add autoscaling rules (CPU, HTTP requests)
    - Test autoscaling behavior
    - Commit: "config: configure autoscaling rules"

### Phase 7: Monitoring & Security

14. **Configure Monitoring**
    - Verify Application Insights integration
    - Create custom dashboards
    - Set up alerts
    - Commit: "config: configure monitoring and alerts"

15. **Security Scanning**
    - Scan container image for vulnerabilities
    - Scan dependencies (Dependabot)
    - Enable Microsoft Defender for Containers
    - Document security findings
    - Commit: "docs: document security scan results"

### Phase 8: Testing & Validation

16. **Load Testing**
    - Use Azure Load Testing or JMeter
    - Target: 1000 concurrent users, p95 <200ms
    - Document results
    - Commit: "docs: document load testing results"

17. **Disaster Recovery Drill**
    - Test backup/restore procedures
    - Verify RTO <4 hours
    - Document procedures
    - Commit: "docs: document DR procedures"

### Phase 9: Documentation & Summary

18. **Create Operations Runbook**
    - Deployment procedures
    - Rollback procedures
    - Monitoring and alerting
    - Troubleshooting guide
    - Commit: "docs: create operations runbook"

19. **Generate migration summary**
    - Full deployment details
    - Performance metrics
    - Cost analysis
    - Lessons learned

20. **Final handoff report**

## Expected Deliverables

1. ✅ Optimized multi-stage Dockerfile
2. ✅ Bicep infrastructure templates
3. ✅ GitHub Actions CI/CD pipeline
4. ✅ Application deployed to Azure Container Apps
5. ✅ Autoscaling configured
6. ✅ Monitoring dashboards created
7. ✅ Security scans completed
8. ✅ Load testing completed
9. ✅ Operations runbook created
10. ✅ Complete documentation

## Success Criteria

- [ ] Container builds successfully
- [ ] Application runs in Azure Container Apps
- [ ] CI/CD pipeline operational
- [ ] Autoscaling working (tested)
- [ ] Monitoring configured (Application Insights)
- [ ] Load test passed (p95 <200ms)
- [ ] Security scans show zero critical issues
- [ ] Operations runbook complete
- [ ] Documentation complete

## Start Command

Begin by reading transformation docs, creating execution plan, and awaiting approval.

---

**Ready to begin? Start with step 1.**
