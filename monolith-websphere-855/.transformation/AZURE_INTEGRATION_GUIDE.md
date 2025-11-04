# Azure Integration Guide: Cloud-Native Patterns

**Project:** Customer Order Services  
**Focus:** Microsoft Entra ID, Azure Key Vault, App Configuration, OpenTelemetry  
**Document Version:** 1.0  
**Last Updated:** November 4, 2025

---

## Table of Contents

1. [Microsoft Entra ID Authentication](#1-microsoft-entra-id-authentication)
2. [Azure Key Vault Integration](#2-azure-key-vault-integration)
3. [Azure App Configuration](#3-azure-app-configuration)
4. [OpenTelemetry & Application Insights](#4-opentelemetry--application-insights)
5. [Complete Configuration Example](#5-complete-configuration-example)

---

## 1. Microsoft Entra ID Authentication

### 1.1 Create App Registration

**PowerShell:**

```powershell
# Login to Azure
az login

# Create app registration
az ad app create `
    --display-name "Customer Order API" `
    --sign-in-audience AzureADMyOrg `
    --web-redirect-uris "https://ca-customer-order-api.azurecontainerapps.io/login/oauth2/code/azure"

# Get Application (client) ID
$clientId = az ad app list --display-name "Customer Order API" --query "[0].appId" -o tsv

# Create client secret
$secret = az ad app credential reset --id $clientId --query password -o tsv

# Create app roles
az ad app update --id $clientId --app-roles @app-roles.json
```

**File:** `app-roles.json`

```json
[
  {
    "allowedMemberTypes": ["User"],
    "description": "Read access to orders",
    "displayName": "Orders.Read",
    "isEnabled": true,
    "value": "Orders.Read"
  },
  {
    "allowedMemberTypes": ["User"],
    "description": "Write access to orders",
    "displayName": "Orders.Write",
    "isEnabled": true,
    "value": "Orders.Write"
  }
]
```

---

### 1.2 Spring Security Configuration

**Dependencies:**

```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
    <version>5.7.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Configuration (`application.yml`):**

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
        app-id-uri: api://customer-order-api
        authorization-clients:
          graph:
            scopes: https://graph.microsoft.com/.default
```

**Security Config Class:**

```java
package org.pwte.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new CustomJwtConverter()))
            )
            .csrf(csrf -> csrf.disable()); // Use for stateless APIs
        
        return http.build();
    }
}
```

**Custom JWT Converter:**

```java
package org.pwte.example.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("APPROLE_" + role))
            .collect(Collectors.toList());
    }
}
```

**Controller with Role-Based Access:**

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPROLE_Orders.Read')")
    public OrderDTO getOrder(@PathVariable Long id) {
        return orderService.findOrder(id);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('APPROLE_Orders.Write')")
    public OrderDTO createOrder(@RequestBody @Valid OrderDTO order) {
        return orderService.createOrder(order);
    }
}
```

**Effort:** 80 hours (35 hours with AI assistance)

---

## 2. Azure Key Vault Integration

### 2.1 Create Key Vault

```powershell
# Create Key Vault
az keyvault create `
    --name kv-customer-order-prod `
    --resource-group rg-customer-order-prod `
    --location eastus2 `
    --enable-rbac-authorization

# Get Key Vault URI
$kvUri = az keyvault show `
    --name kv-customer-order-prod `
    --query properties.vaultUri -o tsv

# Grant Managed Identity access
az role assignment create `
    --assignee <managed-identity-principal-id> `
    --role "Key Vault Secrets User" `
    --scope /subscriptions/<subscription-id>/resourceGroups/rg-customer-order-prod/providers/Microsoft.KeyVault/vaults/kv-customer-order-prod
```

### 2.2 Store Secrets

```powershell
# Database password
az keyvault secret set `
    --vault-name kv-customer-order-prod `
    --name DB-PASSWORD `
    --value "<generated-password>"

# Entra ID client secret
az keyvault secret set `
    --vault-name kv-customer-order-prod `
    --name AZURE-CLIENT-SECRET `
    --value "<client-secret>"
```

### 2.3 Spring Boot Integration

**Dependencies:**

```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
    <version>5.7.0</version>
</dependency>
```

**Configuration:**

```yaml
spring:
  config:
    import:
      - optional:azureconfig:${AZURE_APP_CONFIG_ENDPOINT}
      - optional:secrets:${AZURE_KEY_VAULT_URI}
  
  cloud:
    azure:
      keyvault:
        secret:
          endpoint: ${AZURE_KEY_VAULT_URI}
          credential:
            managed-identity-enabled: true
```

**Usage in Code:**

```java
@Service
public class DataSourceConfiguration {
    
    @Value("${DB-PASSWORD}")  // Automatically resolved from Key Vault
    private String dbPassword;
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://...");
        ds.setUsername("dbadmin");
        ds.setPassword(dbPassword);  // From Key Vault
        return ds;
    }
}
```

**Effort:** 40 hours (18 hours with AI assistance)

---

## 3. Azure App Configuration

### 3.1 Create App Configuration Store

```powershell
# Create App Configuration
az appconfig create `
    --name appconfig-customer-order-prod `
    --resource-group rg-customer-order-prod `
    --location eastus2 `
    --sku Standard

# Set configuration values
az appconfig kv set `
    --name appconfig-customer-order-prod `
    --key "DB_HOST" `
    --value "psql-customer-order-prod.postgres.database.azure.com" `
    --yes

az appconfig kv set `
    --name appconfig-customer-order-prod `
    --key "DB_NAME" `
    --value "orders" `
    --yes

# Feature flags
az appconfig feature set `
    --name appconfig-customer-order-prod `
    --feature "NewCheckoutFlow" `
    --label "Production" `
    --yes
```

### 3.2 Spring Boot Integration

**Dependencies:**

```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-appconfiguration-config</artifactId>
    <version>5.7.0</version>
</dependency>
```

**Configuration:**

```yaml
spring:
  config:
    import: optional:azureconfig:${AZURE_APP_CONFIG_ENDPOINT}
  
  cloud:
    azure:
      appconfiguration:
        stores:
          - endpoint: ${AZURE_APP_CONFIG_ENDPOINT}
            monitoring:
              enabled: true
              refresh-interval: 30s
            feature-flags:
              enabled: true
```

**Feature Flag Usage:**

```java
import com.azure.spring.cloud.feature.manager.FeatureManager;

@Service
public class CheckoutService {
    
    private final FeatureManager featureManager;
    
    @Autowired
    public CheckoutService(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }
    
    public void processCheckout(Order order) {
        if (featureManager.isEnabled("NewCheckoutFlow")) {
            // New implementation
            processCheckoutV2(order);
        } else {
            // Legacy implementation
            processCheckoutV1(order);
        }
    }
}
```

**Effort:** 40 hours (20 hours with AI assistance)

---

## 4. OpenTelemetry & Application Insights

### 4.1 Create Application Insights

```powershell
# Create Application Insights
az monitor app-insights component create `
    --app appinsights-customer-order `
    --location eastus2 `
    --resource-group rg-customer-order-prod `
    --workspace /subscriptions/<subscription-id>/resourceGroups/rg-customer-order-prod/providers/Microsoft.OperationalInsights/workspaces/log-customer-order-prod

# Get connection string
$connectionString = az monitor app-insights component show `
    --app appinsights-customer-order `
    --resource-group rg-customer-order-prod `
    --query connectionString -o tsv
```

### 4.2 OpenTelemetry Configuration

**Dependencies:**

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>

<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-opentelemetry-exporter</artifactId>
    <version>1.0.0-beta.14</version>
</dependency>
```

**Configuration (`application.yml`):**

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% in dev, reduce in production
  
  otlp:
    tracing:
      endpoint: ${APPLICATIONINSIGHTS_CONNECTION_STRING}

otel:
  sdk:
    disabled: false
  traces:
    exporter: otlp
  metrics:
    exporter: otlp
  logs:
    exporter: otlp
  resource:
    attributes:
      service.name: customer-order-api
      service.version: ${APP_VERSION:1.0.0}
      deployment.environment: ${ENVIRONMENT:production}

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

### 4.3 Custom Instrumentation

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
public class OrderService {
    
    private final Tracer tracer;
    
    @Autowired
    public OrderService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Order processOrder(Long orderId) {
        Span span = tracer.spanBuilder("processOrder")
            .setAttribute("order.id", orderId)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            Order order = loadOrder(orderId);
            span.setAttribute("order.total", order.getTotal().doubleValue());
            span.addEvent("Order loaded successfully");
            
            validateOrder(order);
            return order;
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### 4.4 Kusto Queries for Azure Monitor

**Query 1: Request Performance**

```kql
requests
| where timestamp > ago(1h)
| summarize 
    avg_duration_ms = avg(duration),
    p95_duration_ms = percentile(duration, 95),
    p99_duration_ms = percentile(duration, 99),
    request_count = count()
    by operation_Name
| order by p95_duration_ms desc
```

**Query 2: Failed Requests**

```kql
requests
| where timestamp > ago(24h) and success == false
| summarize count() by resultCode, operation_Name
| order by count_ desc
```

**Query 3: Distributed Traces**

```kql
dependencies
| where timestamp > ago(1h)
| where target contains "postgres"
| summarize avg(duration), count() by operation_Name
| order by avg_duration desc
```

**Effort:** 60 hours (25 hours with AI assistance)

---

## 5. Complete Configuration Example

**File:** `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: customer-order-api
  
  # Azure Configuration Import
  config:
    import:
      - optional:azureconfig:${AZURE_APP_CONFIG_ENDPOINT}
      - optional:secrets:${AZURE_KEY_VAULT_URI}
  
  # Database
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB-PASSWORD}  # From Key Vault
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
      hibernate.jdbc.lob.non_contextual_creation: true
      hibernate.format_sql: false
    show-sql: false
  
  # Azure Cloud Integration
  cloud:
    azure:
      # Entra ID Authentication
      active-directory:
        enabled: true
        profile:
          tenant-id: ${AZURE_TENANT_ID}
        credential:
          client-id: ${AZURE_CLIENT_ID}
          client-secret: ${AZURE-CLIENT-SECRET}  # From Key Vault
        app-id-uri: api://customer-order-api
      
      # Key Vault
      keyvault:
        secret:
          endpoint: ${AZURE_KEY_VAULT_URI}
          credential:
            managed-identity-enabled: true
      
      # App Configuration
      appconfiguration:
        stores:
          - endpoint: ${AZURE_APP_CONFIG_ENDPOINT}
            monitoring:
              enabled: true
              refresh-interval: 30s
            feature-flags:
              enabled: true

# Actuator & Health Checks
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
  
  # OpenTelemetry
  tracing:
    sampling:
      probability: 0.1  # 10% sampling in production
  
  otlp:
    tracing:
      endpoint: ${APPLICATIONINSIGHTS_CONNECTION_STRING}

# OpenTelemetry
otel:
  sdk:
    disabled: false
  traces:
    exporter: otlp
  metrics:
    exporter: otlp
  resource:
    attributes:
      service.name: customer-order-api
      service.version: ${APP_VERSION:1.0.0}
      deployment.environment: ${ENVIRONMENT:production}

# Logging
logging:
  level:
    root: INFO
    org.pwte.example: DEBUG
    org.springframework.security: DEBUG
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

**Environment Variables (Container Apps):**

```bash
AZURE_TENANT_ID=<tenant-id>
AZURE_CLIENT_ID=<client-id>
AZURE_KEY_VAULT_URI=https://kv-customer-order-prod.vault.azure.net/
AZURE_APP_CONFIG_ENDPOINT=https://appconfig-customer-order-prod.azconfig.io
APPLICATIONINSIGHTS_CONNECTION_STRING=<connection-string>
DB_HOST=psql-customer-order-prod.postgres.database.azure.com
DB_NAME=orders
DB_USERNAME=dbadmin
ENVIRONMENT=production
APP_VERSION=1.0.0
```

---

## Summary

**Total Effort:**

| Component | Manual Hours | AI-Assisted Hours |
|-----------|--------------|-------------------|
| Entra ID Authentication | 80 | 35 |
| Key Vault Integration | 40 | 18 |
| App Configuration | 40 | 20 |
| OpenTelemetry & Monitoring | 60 | 25 |
| **TOTAL** | **220** | **98** |

**Timeline:** 2-3 weeks (2 developers, 55% efficiency gain)

**Validation Checklist:**
- [ ] Authentication working with Entra ID tokens
- [ ] All secrets retrieved from Key Vault (zero hardcoded)
- [ ] Configuration externalized to App Config
- [ ] Distributed traces visible in Application Insights
- [ ] Health probes responding correctly
- [ ] Managed Identity configured for all Azure services

**Next Document:** [Detailed Cost Estimates](./DETAILED_COST_ESTIMATES.md)

