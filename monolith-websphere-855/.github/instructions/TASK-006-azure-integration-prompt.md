# Migration Task Prompt: Azure Integration & Cloud-Native Patterns

## Mission
You are an AI agent tasked with performing **TASK-006** for the Customer Order Services application: **Implement Azure Cloud-Native Patterns** including Microsoft Entra ID, Key Vault, App Configuration, and OpenTelemetry.

## Task Overview
**Task ID:** TASK-006  
**Task Name:** Azure Integration (Cloud-Native Patterns)  
**Priority:** 🟡 HIGH  
**Dependencies:** All Phase 1 and TASK-005 (Database Migration) completed  
**Estimated Effort:** 496 hours manual / 236 hours AI-assisted  
**Success Criteria:** All Azure services integrated, secrets externalized, authentication working, observability configured

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/AZURE_INTEGRATION_GUIDE.md` (all sections)
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 2, Tasks 2.3-2.10
   - Review `.transformation/PLATFORM_MIGRATION_GUIDE.md` Azure sections

2. **Create detailed execution plan**
   - Document Azure resources needed (Key Vault, App Config, Application Insights)
   - Plan authentication strategy (Entra ID integration)
   - Plan secrets management approach
   - Document configuration externalization strategy
   - List observability requirements
   - Save plan to `.vscode/transformation/TASK-006/plan.md`

3. **Get user confirmation** before proceeding

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-006-azure-integration`
   - Branch from: current branch (agent-test or your working branch)
   - Commit message: "chore: create branch for Azure integration task"

5. **Initialize progress tracking**
   - Create `.transformation/progress/TASK-006-progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.transformation/todos/TASK-006-todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation - Azure Services Setup

6. **Provision Azure Resources**
   - Create Azure Key Vault:
     ```powershell
     az keyvault create --name kv-customerorder-dev --resource-group rg-customerorder-dev
     ```
   - Create Azure App Configuration:
     ```powershell
     az appconfig create --name appconfig-customerorder --resource-group rg-customerorder-dev --sku Standard
     ```
   - Create Application Insights:
     ```powershell
     az monitor app-insights component create --app customerorder-insights --location eastus --resource-group rg-customerorder-dev
     ```
   - Document connection strings and keys in progress file
   - Commit: "docs: document Azure resource provisioning"

7. **Add Spring Cloud Azure Dependencies**
   - Add to `pom.xml`:
     ```xml
     <dependencyManagement>
       <dependencies>
         <dependency>
           <groupId>com.azure.spring</groupId>
           <artifactId>spring-cloud-azure-dependencies</artifactId>
           <version>5.7.0</version>
           <type>pom</type>
           <scope>import</scope>
         </dependency>
       </dependencies>
     </dependencyManagement>
     
     <dependencies>
       <!-- Entra ID -->
       <dependency>
         <groupId>com.azure.spring</groupId>
         <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
       </dependency>
       
       <!-- Key Vault -->
       <dependency>
         <groupId>com.azure.spring</groupId>
         <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
       </dependency>
       
       <!-- App Configuration -->
       <dependency>
         <groupId>com.azure.spring</groupId>
         <artifactId>spring-cloud-azure-starter-appconfiguration</artifactId>
       </dependency>
       
       <!-- OpenTelemetry -->
       <dependency>
         <groupId>io.opentelemetry.instrumentation</groupId>
         <artifactId>opentelemetry-spring-boot-starter</artifactId>
         <version>2.0.0</version>
       </dependency>
     </dependencies>
     ```
   - Commit: "build: add Spring Cloud Azure dependencies"

### Phase 4: Authentication - Microsoft Entra ID

8. **Create Entra ID App Registration**
   - Use Azure Portal or CLI to create app registration
   - Document: Client ID, Tenant ID, Client Secret
   - Create app roles: Orders.Read, Orders.Write
   - Commit: "docs: document Entra ID app registration details"

9. **Configure Spring Security with Entra ID**
   - Create `application.yml`:
     ```yaml
     spring:
       cloud:
         azure:
           active-directory:
             enabled: true
             profile:
               tenant-id: ${AZURE_TENANT_ID}
             credential:
               client-id: ${AZURE_CLIENT_ID}
               client-secret: ${AZURE_CLIENT_SECRET}
     ```
   - Create `SecurityConfig.java`:
     ```java
     @Configuration
     @EnableWebSecurity
     @EnableMethodSecurity
     public class SecurityConfig {
         @Bean
         public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
             http
                 .authorizeHttpRequests(auth -> auth
                     .requestMatchers("/actuator/health").permitAll()
                     .anyRequest().authenticated()
                 )
                 .oauth2ResourceServer(oauth2 -> oauth2.jwt());
             return http.build();
         }
     }
     ```
   - Commit: "feat: implement Entra ID authentication"

10. **Replace @RolesAllowed with @PreAuthorize**
    - Convert EJB security:
      ```java
      // BEFORE
      @RolesAllowed("SecureShopper")
      public Order loadOrder(long orderId) { ... }
      
      // AFTER
      @PreAuthorize("hasAuthority('APPROLE_Orders.Read')")
      public Order loadOrder(long orderId) { ... }
      ```
    - Commit after each class: "refactor: migrate security annotations in {ClassName}"

### Phase 5: Secrets Management - Azure Key Vault

11. **Store Secrets in Key Vault**
    - Store database credentials:
      ```powershell
      az keyvault secret set --vault-name kv-customerorder-dev --name db-password --value <password>
      ```
    - Configure Managed Identity access
    - Commit: "docs: document Key Vault secrets configuration"

12. **Configure Key Vault Integration**
    - Update `application.yml`:
      ```yaml
      spring:
        cloud:
          azure:
            keyvault:
              secret:
                enabled: true
                endpoint: https://kv-customerorder-dev.vault.azure.net/
        datasource:
          password: ${db-password}  # Resolved from Key Vault
      ```
    - Commit: "config: integrate Azure Key Vault for secrets"

### Phase 6: Configuration - Azure App Configuration

13. **Upload Configuration to App Configuration**
    - Store environment-specific config
    - Use labels for environments (dev, staging, prod)
    - Commit: "docs: document App Configuration setup"

14. **Configure App Configuration Integration**
    - Add bootstrap configuration
    - Enable dynamic refresh
    - Commit: "config: integrate Azure App Configuration"

### Phase 7: Observability - OpenTelemetry & Application Insights

15. **Configure OpenTelemetry**
    - Add `application.yml`:
      ```yaml
      management:
        endpoints:
          web:
            exposure:
              include: health,info,metrics,prometheus
        endpoint:
          health:
            show-details: when-authorized
            probes:
              enabled: true
        metrics:
          export:
            azure-application-insights:
              enabled: true
              instrumentation-key: ${APPLICATIONINSIGHTS_CONNECTION_STRING}
      ```
    - Commit: "config: configure OpenTelemetry and Application Insights"

16. **Add Health Checks**
    - Implement liveness and readiness probes
    - Commit: "feat: add Kubernetes health check endpoints"

### Phase 8: Validation & Testing

17. **Build and test** all modules
18. **Test authentication** with Entra ID
19. **Verify secrets** loaded from Key Vault
20. **Test observability** - check Application Insights

### Phase 9: Documentation & Summary

21. Generate migration summary with Azure integration details
22. Create diff documentation
23. Final handoff report

## Expected Deliverables

1. ✅ Azure resources provisioned (Key Vault, App Config, Insights)
2. ✅ Spring Cloud Azure dependencies added
3. ✅ Entra ID authentication implemented
4. ✅ Key Vault integration working
5. ✅ App Configuration integration working
6. ✅ OpenTelemetry configured
7. ✅ Health checks implemented
8. ✅ All tests passing
9. ✅ Documentation complete

## Success Criteria

- [ ] Authentication via Entra ID working
- [ ] All secrets stored in Key Vault
- [ ] Configuration externalized to App Configuration
- [ ] Telemetry flowing to Application Insights
- [ ] Health endpoints responding
- [ ] All tests passing
- [ ] Documentation complete

## Start Command

Begin by reading transformation docs, creating execution plan, and awaiting approval.

---

**Ready to begin? Start with step 1.**
