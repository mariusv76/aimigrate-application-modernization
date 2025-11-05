# TASK-002 Execution Plan: Test Foundation

**Task ID:** TASK-002  
**Task Name:** Testing Foundation (Unit + Integration Tests)  
**Created:** 2025-11-05  
**Status:** Planning  
**Dependency:** TASK-001 (Java 17 Upgrade) ✅ COMPLETED

---

## Executive Summary

This task builds a comprehensive test foundation for the Customer Order Services application with:
- **Target Coverage:** ≥70% code coverage
- **Testing Frameworks:** JUnit 5, Mockito, AssertJ, REST Assured
- **Scope:** Unit tests (domain + service) + Integration tests (REST endpoints)
- **Estimated Effort:** 52 hours with AI assistance (vs 120 hours manual)

---

## Codebase Analysis

### Domain Model Classes (9 classes)
Location: `CustomerOrderServices/ejbModule/org/pwte/example/domain/`

1. **AbstractCustomer.java** - Base class for customer types
2. **Address.java** - Customer address entity
3. **BusinessCustomer.java** - Business customer subclass
4. **Category.java** - Product category entity
5. **LineItem.java** - Order line item entity
6. **LineItemId.java** - Composite key for LineItem
7. **Order.java** - Customer order entity
8. **Product.java** - Product catalog entity
9. **ResidentialCustomer.java** - Residential customer subclass

**Testing Strategy:**
- Constructor validation
- Getter/setter coverage
- Equals/hashCode contracts
- JPA relationship integrity
- Business rule validation

### Service Layer Classes (1+ classes)
Location: `CustomerOrderServices/ejbModule/org/pwte/example/service/`

1. **CustomerOrderServicesImpl.java** - Main business service

**Testing Strategy:**
- Mock EntityManager interactions
- Test transaction boundaries
- Validate business logic
- Test error handling
- Mock security contexts (@RolesAllowed)

### REST Endpoint Classes (3 resources)
Location: `CustomerOrderServicesWeb/src/org/pwte/example/resources/`

1. **CategoryResource.java** - Category CRUD endpoints
2. **CustomerOrderResource.java** - Order management endpoints
3. **ProductResource.java** - Product catalog endpoints

**Testing Strategy:**
- HTTP method coverage (GET, POST, PUT, DELETE)
- Response code validation (200, 201, 400, 404, 500)
- JSON serialization/deserialization
- Authentication/authorization tests

---

## Testing Framework Configuration

### Dependencies to Add

```xml
<!-- JUnit 5 BOM -->
<dependency>
  <groupId>org.junit</groupId>
  <artifactId>junit-bom</artifactId>
  <version>5.10.1</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>

<!-- Mockito -->
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <version>5.8.0</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-junit-jupiter</artifactId>
  <version>5.8.0</version>
  <scope>test</scope>
</dependency>

<!-- AssertJ -->
<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>3.25.1</version>
  <scope>test</scope>
</dependency>

<!-- REST Assured (for integration tests) -->
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <version>5.4.0</version>
  <scope>test</scope>
</dependency>
```

### Maven Plugins

```xml
<!-- Surefire for JUnit 5 -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.3</version>
</plugin>

<!-- JaCoCo for code coverage -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>jacoco-check</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>PACKAGE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.70</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

---

## Execution Phases

### Phase 1: Setup (Estimated: 4 hours)
1. ✅ Create feature branch `migration/task-002-test-foundation`
2. ✅ Initialize progress tracking files
3. ✅ Configure JUnit 5 + Mockito dependencies in parent POM
4. ✅ Configure Surefire plugin
5. ✅ Configure JaCoCo coverage plugin
6. ✅ Create test directory structure

### Phase 2: Domain Model Tests (Estimated: 16 hours)
Generate unit tests for 9 domain classes:

1. **AbstractCustomerTest.java** (2 hours)
   - Test abstract customer properties
   - Test customer type hierarchy

2. **AddressTest.java** (1.5 hours)
   - Test address construction
   - Test validation (zip, state, country)
   - Test equals/hashCode

3. **BusinessCustomerTest.java** (2 hours)
   - Test business-specific properties
   - Test inheritance from AbstractCustomer

4. **CategoryTest.java** (1.5 hours)
   - Test category hierarchy
   - Test relationship with products

5. **LineItemTest.java** (2 hours)
   - Test composite key (LineItemId)
   - Test product-order relationship
   - Test quantity/price calculations

6. **LineItemIdTest.java** (1 hour)
   - Test composite key equals/hashCode
   - Test serialization

7. **OrderTest.java** (2.5 hours)
   - Test order-customer relationship
   - Test line item collection
   - Test order totals calculation

8. **ProductTest.java** (2 hours)
   - Test product-category relationship
   - Test price validation

9. **ResidentialCustomerTest.java** (1.5 hours)
   - Test residential-specific properties
   - Test inheritance from AbstractCustomer

**Commit Strategy:** One commit per test class

### Phase 3: Service Layer Tests (Estimated: 16 hours)

1. **CustomerOrderServicesImplTest.java** (16 hours)
   - Mock EntityManager
   - Test all business methods:
     - `loadCustomer()`
     - `loadProduct()`
     - `loadCategory()`
     - `updateProduct()`
     - `addProductToCategory()`
     - `removeProductFromCategory()`
     - `loadOrder()`
     - `saveOrder()`
   - Test transaction boundaries
   - Test security (@RolesAllowed)
   - Test error handling (EntityNotFoundException, etc.)

**Commit Strategy:** Commit after each logical group of related tests

### Phase 4: REST Integration Tests (Estimated: 12 hours)

1. **CategoryResourceTest.java** (4 hours)
   - GET /categories (200)
   - GET /categories/{id} (200, 404)
   - POST /categories (201, 400)
   - PUT /categories/{id} (200, 404, 400)
   - DELETE /categories/{id} (204, 404)

2. **CustomerOrderResourceTest.java** (4 hours)
   - GET /orders/{id} (200, 404)
   - POST /orders (201, 400)
   - PUT /orders/{id} (200, 404, 400)
   - Test customer association
   - Test line items

3. **ProductResourceTest.java** (4 hours)
   - GET /products (200)
   - GET /products/{id} (200, 404)
   - POST /products (201, 400)
   - PUT /products/{id} (200, 404, 400)
   - DELETE /products/{id} (204, 404)

**Commit Strategy:** One commit per resource test class

### Phase 5: Coverage Validation (Estimated: 4 hours)
1. Run full test suite
2. Generate JaCoCo report
3. Analyze coverage gaps
4. Add targeted tests for uncovered code
5. Re-run until ≥70% achieved

---

## Test Directory Structure

```
CustomerOrderServices/
├── ejbModule/              (production code)
└── src/
    ├── main/java/          (if needed)
    └── test/java/
        └── org/pwte/example/
            ├── domain/
            │   ├── AbstractCustomerTest.java
            │   ├── AddressTest.java
            │   ├── BusinessCustomerTest.java
            │   ├── CategoryTest.java
            │   ├── LineItemTest.java
            │   ├── LineItemIdTest.java
            │   ├── OrderTest.java
            │   ├── ProductTest.java
            │   └── ResidentialCustomerTest.java
            └── service/
                └── CustomerOrderServicesImplTest.java

CustomerOrderServicesWeb/
├── src/                    (production code)
└── src/test/java/
    └── org/pwte/example/
        └── resources/
            ├── CategoryResourceTest.java
            ├── CustomerOrderResourceTest.java
            └── ProductResourceTest.java
```

---

## Success Criteria Checklist

- [ ] JUnit 5 + Mockito + AssertJ configured in CustomerOrderServices module
- [ ] JUnit 5 + REST Assured configured in CustomerOrderServicesWeb module
- [ ] JaCoCo configured with 70% threshold
- [ ] 9 domain model test classes created
- [ ] 1 service test class created
- [ ] 3 REST resource test classes created
- [ ] All tests passing (`mvn clean test` succeeds)
- [ ] Code coverage ≥70% (JaCoCo report)
- [ ] No skipped or ignored tests without documentation
- [ ] 15-20 commits with proper conventional commit messages
- [ ] Progress tracked after each milestone
- [ ] Summary document completed
- [ ] Diff documentation for key test files

---

## Risk Mitigation

### Risk: Low Coverage Despite Many Tests
**Mitigation:** Focus on high-value business logic in services, not just getters/setters

### Risk: Integration Tests Require Running Server
**Mitigation:** Use embedded test containers or mock JAX-RS context

### Risk: Complex JPA Mocking
**Mitigation:** Use Mockito argument captors and verify interactions

### Risk: Time Overrun
**Mitigation:** Prioritize service and integration tests over exhaustive domain tests

---

## GitHub Copilot Prompts

### For Domain Tests:
```
Generate comprehensive JUnit 5 tests for this JPA entity class.
Include tests for:
- All constructors
- Getters and setters
- equals() and hashCode() methods
- JPA relationships
- Validation logic
Use AssertJ for assertions.
```

### For Service Tests:
```
Generate JUnit 5 + Mockito tests for this EJB service class.
Mock the EntityManager and test all business methods.
Include happy path and error cases.
Use AssertJ for assertions.
```

### For REST Tests:
```
Generate REST Assured integration tests for this JAX-RS resource.
Test all HTTP methods (GET, POST, PUT, DELETE).
Include success (200, 201) and error (400, 404) cases.
Verify JSON responses.
```

---

## Next Steps

**Awaiting User Approval**

Once approved, I will:
1. Create the feature branch
2. Initialize progress tracking
3. Configure test dependencies
4. Begin generating tests (domain → service → REST)
5. Validate coverage
6. Create summary documentation

**Estimated Completion:** 52 hours with AI assistance

---

**Status:** ⏸️ AWAITING APPROVAL  
**Ready to proceed:** YES  
**Blockers:** NONE
