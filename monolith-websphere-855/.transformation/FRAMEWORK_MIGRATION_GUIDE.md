# Framework Migration Guide: Java EE 7 → Jakarta EE 10 / Spring Boot

**Project:** Customer Order Services  
**Migration Path:** Java 8 + Java EE 7 → Java 17 + Spring Boot 3.2  
**Document Version:** 1.0  
**Last Updated:** November 4, 2025

---

## Table of Contents

1. [Overview](#1-overview)
2. [Java Version Upgrade (8 → 17)](#2-java-version-upgrade-8--17)
3. [Option A: Jakarta EE 10 Migration](#3-option-a-jakarta-ee-10-migration)
4. [Option B: Spring Boot 3.2 Migration (Recommended)](#4-option-b-spring-boot-32-migration-recommended)
5. [Dependency Upgrades](#5-dependency-upgrades)
6. [Testing Migration](#6-testing-migration)

---

## 1. Overview

### 1.1 Migration Options Comparison

| Criteria | Jakarta EE 10 | Spring Boot 3.2 | Winner |
|----------|---------------|-----------------|--------|
| **Code Changes** | Minimal (namespace only) | Moderate (EJB→Spring) | Jakarta EE |
| **Azure Integration** | Manual setup | Spring Cloud Azure | **Spring Boot** |
| **Container Size** | Larger (full server) | Smaller (Tomcat) | **Spring Boot** |
| **Community Support** | Declining | Very strong | **Spring Boot** |
| **Learning Curve** | Low (familiar) | Medium | Jakarta EE |
| **Long-term Viability** | ⚠️ Legacy path | ✅ Modern standard | **Spring Boot** |
| **Monthly Cost (Azure)** | ~$500 higher | Baseline | **Spring Boot** |

**Recommendation:** **Spring Boot 3.2** for better Azure alignment and lower TCO

---

## 2. Java Version Upgrade (8 → 17)

### Step 2.1: Update Maven Compiler Configuration

**File:** `pom.xml` (all modules)

```xml
<!-- Before -->
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>

<!-- After -->
<properties>
    <java.version>17</java.version>
    <maven.compiler.release>17</maven.compiler.release>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>17</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Step 2.2: Apply OpenRewrite Java 8 → 17 Migration

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.15.0</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.java.migrate.Java8toJava17</recipe>
        </activeRecipes>
    </configuration>
</plugin>
```

```powershell
mvn rewrite:run
```

**Automated Changes:**
- ✅ Update deprecated APIs
- ✅ Remove unnecessary type witnesses
- ✅ Modernize string concatenation
- ✅ Use `var` where applicable

**Effort:** 40 hours (20 hours with OpenRewrite)

---

## 4. Option B: Spring Boot 3.2 Migration (Recommended)

### Step 4.1: Replace Parent POM

**Before (Java EE):**
```xml
<project>
    <groupId>org.pwte.example</groupId>
    <artifactId>CustomerOrderServices</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>ejb</packaging>
</project>
```

**After (Spring Boot):**
```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>org.pwte.example</groupId>
    <artifactId>customer-order-services</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud-azure.version>5.7.0</spring-cloud-azure.version>
    </properties>
</project>
```

### Step 4.2: Add Spring Boot Dependencies

```xml
<dependencies>
    <!-- Web & REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Data/JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Azure Integration -->
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-starter</artifactId>
    </dependency>
    
    <!-- Security (Entra ID) -->
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
    </dependency>
    
    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-spring-boot-starter</artifactId>
        <version>2.0.0</version>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### Step 4.3: Convert EJB to Spring Service

**✅ AppCAT Assessment:** Detects all `@Stateless` EJB patterns and reports story points

**✅ GitHub Copilot App Modernization:** Provides predefined migration tasks for EJB→Spring conversion

- Extension identifies EJB patterns from AppCAT findings
- Automated code transformation to Spring `@Service` components
- Handles dependency injection, transaction annotations, and lifecycle callbacks
- GitHub Copilot assists with complex business logic transformations

**Before (EJB):**
```java
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@RolesAllowed("SecureShopper")
public class CustomerOrderServicesImpl implements CustomerOrderServices {
    
    @PersistenceContext
    protected EntityManager em;
    
    @Resource SessionContext ctx;
    
    public Order loadOrder(long orderId) {
        return em.find(Order.class, orderId);
    }
}
```

**After (Spring):**
```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.persistence.EntityManager;

@Service
@Transactional
@PreAuthorize("hasRole('SecureShopper')")
public class CustomerOrderService {
    
    private final EntityManager entityManager;
    
    public CustomerOrderService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Order loadOrder(long orderId) {
        return entityManager.find(Order.class, orderId);
    }
}
```

**Mapping Guide:**

| Java EE Annotation | Spring Equivalent | Notes |
|--------------------|------------------|-------|
| `@Stateless` | `@Service` | Spring manages lifecycle |
| `@PersistenceContext` | Constructor injection | Better testability |
| `@Resource` | `@Autowired` or constructor | Prefer constructor |
| `@RolesAllowed` | `@PreAuthorize("hasRole()")` | Spring Security |
| `@TransactionAttribute` | `@Transactional` | Spring Transactions |

### Step 4.4: Convert JAX-RS to Spring MVC

**✅ AppCAT Assessment:** Detects all JAX-RS `@Path` endpoints and reports story points

**✅ GitHub Copilot App Modernization:** Provides predefined migration tasks for JAX-RS→Spring MVC conversion

- Extension identifies REST endpoints from AppCAT findings
- Automated code transformation to Spring `@RestController` components
- Converts HTTP method annotations (@GET → @GetMapping, @POST → @PostMapping)
- GitHub Copilot assists with request/response object transformations

**Before (JAX-RS):**
```java
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/orders")
public class CustomerOrderResource {
    
    @EJB
    private CustomerOrderServices orderService;
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id) {
        Order order = orderService.loadOrder(id);
        return Response.ok(order).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(Order order) {
        Order created = orderService.createOrder(order);
        return Response.status(201).entity(created).build();
    }
}
```

**After (Spring MVC):**
```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final CustomerOrderService orderService;
    
    public OrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable long id) {
        Order order = orderService.loadOrder(id);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody @Valid Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

**Mapping Guide:**

| JAX-RS | Spring MVC | Notes |
|--------|-----------|-------|
| `@Path` | `@RequestMapping` | Class and method level |
| `@GET` | `@GetMapping` | Shorthand annotation |
| `@POST` | `@PostMapping` | Shorthand annotation |
| `@PathParam` | `@PathVariable` | URL path variables |
| `@QueryParam` | `@RequestParam` | Query string params |
| `@Consumes` | `consumes` attribute | In `@RequestMapping` |
| `@Produces` | `produces` attribute | In `@RequestMapping` |
| `Response` | `ResponseEntity` | Spring wrapper |

### Step 4.5: Convert persistence.xml to application.yml

**Before (`persistence.xml`):**
```xml
<persistence-unit name="CustomerOrderServices">
    <jta-data-source>jdbc/orderds</jta-data-source>
    <class>org.pwte.example.domain.Order</class>
    <properties>
        <property name="openjpa.jdbc.DBDictionary" value="db2" />
    </properties>
</persistence-unit>
```

**After (`application.yml`):**
```yaml
spring:
  application:
    name: customer-order-api
  
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
      hibernate.format_sql: true
    show-sql: false
  
  cloud:
    azure:
      active-directory:
        enabled: true
        profile:
          tenant-id: ${AZURE_TENANT_ID}
        credential:
          client-id: ${AZURE_CLIENT_ID}
          client-secret: ${AZURE_CLIENT_SECRET}

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
```

### Step 4.6: Create Spring Boot Application Class

**New File:** `src/main/java/org/pwte/example/CustomerOrderApplication.java`

```java
package org.pwte.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CustomerOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CustomerOrderApplication.class, args);
    }
}
```

**Effort:** 120 hours (50 hours with GitHub Copilot)

---

## 5. Dependency Upgrades

### Step 5.1: Replace Jackson 1.7.1 with 2.17.0

**Before (IBM JSON + Old Jackson):**
```java
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;

JSONObject jo = new JSONObject();
jo.put("orderId", order.getOrderId());
```

**After (Jackson 2.17):**
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

ObjectMapper mapper = new ObjectMapper();
ObjectNode jo = mapper.createObjectNode();
jo.put("orderId", order.getOrderId());

// OR better: use POJOs with annotations
@Data
public class OrderDTO {
    private Long orderId;
    private String status;
    private List<LineItemDTO> lineItems;
}

// Spring MVC auto-serializes
@GetMapping("/{id}")
public OrderDTO getOrder(@PathVariable Long id) {
    return orderService.findOrder(id);
}
```

**Migration Tool:** Find/Replace + GitHub Copilot

```powershell
# Find all IBM JSON usages
rg "com\.ibm\.json\.java" --type java

# GitHub Copilot prompt:
# "Convert this IBM JSON code to Jackson 2.17 ObjectMapper"
```

**Effort:** 60 hours (25 hours with AI assistance)

---

## 6. Testing Migration

### Step 6.1: Add Spring Boot Test Framework

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

### Step 6.2: Create Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\": 1}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists());
    }
}
```

**Effort:** 80 hours (30 hours with GitHub Copilot test generation)

---

## Summary

**Total Effort (Spring Boot Path):**

| Phase | Manual Hours | AI-Assisted Hours |
|-------|--------------|-------------------|
| Java 8 → 17 Upgrade | 40 | 20 |
| EJB → Spring Services | 120 | 50 |
| JAX-RS → Spring MVC | 80 | 35 |
| Jackson Upgrade | 60 | 25 |
| Testing | 80 | 30 |
| **TOTAL** | **380** | **160** |

**Timeline:** 4-5 weeks (2 developers, 58% efficiency gain with AI)

**Next Document:** [Azure Integration Guide](./AZURE_INTEGRATION_GUIDE.md)

