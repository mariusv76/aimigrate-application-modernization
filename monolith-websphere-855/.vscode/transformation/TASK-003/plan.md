# TASK-003: Jakarta EE Namespace Migration - Execution Plan

**Task ID:** TASK-003  
**Task Name:** Migrate from javax.* to jakarta.* namespace  
**Created:** November 5, 2025  
**Status:** PLANNING  
**Estimated Effort:** 25 hours (AI-assisted)

---

## Executive Summary

This task migrates the Customer Order Services application from Java EE 7 (javax.* namespace) to Jakarta EE 10 (jakarta.* namespace). This is a critical modernization step required for compatibility with modern application servers and cloud platforms.

---

## Scope Analysis

### Current State Assessment

#### javax.* Package Usage Scan
Based on codebase analysis, the following javax.* packages are in use:

**JPA/Persistence (javax.persistence.*)**
- `javax.persistence.Entity`
- `javax.persistence.Id`
- `javax.persistence.Column`
- `javax.persistence.Table`
- `javax.persistence.GeneratedValue`
- `javax.persistence.GenerationType`
- `javax.persistence.FetchType`
- `javax.persistence.CascadeType`
- `javax.persistence.OneToMany`
- `javax.persistence.ManyToOne`
- `javax.persistence.ManyToMany`
- `javax.persistence.JoinColumn`
- `javax.persistence.JoinTable`
- `javax.persistence.Entity Manager`
- `javax.persistence.PersistenceContext`
- `javax.persistence.Query`
- `javax.persistence.Version`
- `javax.persistence.Embeddable`
- `javax.persistence.NamedNativeQuery`
- `javax.persistence.DiscriminatorValue`
- `javax.persistence.Basic`

**EJB (javax.ejb.*)**
- `javax.ejb.Stateless`
- `javax.ejb.EJB`
- `javax.ejb.SessionContext`
- `javax.ejb.TransactionAttribute`
- `javax.ejb.TransactionAttributeType`

**JAX-RS (javax.ws.rs.*)**
- `javax.ws.rs.GET`
- `javax.ws.rs.POST`
- `javax.ws.rs.PUT`
- `javax.ws.rs.DELETE`
- `javax.ws.rs.Path`
- `javax.ws.rs.PathParam`
- `javax.ws.rs.QueryParam`
- `javax.ws.rs.Produces`
- `javax.ws.rs.Consumes`
- `javax.ws.rs.WebApplicationException`
- `javax.ws.rs.core.Application`
- `javax.ws.rs.core.MediaType`
- `javax.ws.rs.core.Response`
- `javax.ws.rs.core.Response.Status`
- `javax.ws.rs.core.Context`
- `javax.ws.rs.core.HttpHeaders`
- `javax.ws.rs.core.MultivaluedMap`

**Annotations (javax.annotation.*)**
- `javax.annotation.Resource`
- `javax.annotation.security.RolesAllowed`

**JNDI (javax.naming.*)** 
- `javax.naming.Context`
- `javax.naming.InitialContext`
- `javax.naming.NamingException`

**Note:** javax.naming.* is part of Java SE, NOT Java EE, so it should remain as-is.

#### Affected Modules

1. **CustomerOrderServices (EJB Module)**
   - Domain classes: 9 entity classes
   - Service classes: 2 EJB stateless session beans
   - Test classes: 10 unit test classes
   - XML: persistence.xml, orm.xml

2. **CustomerOrderServicesWeb (WAR Module)**
   - REST resources: 3 JAX-RS resource classes
   - Application config: 1 JAX-RS application class
   - Test classes: 3 REST resource test classes
   - XML: web.xml

3. **CustomerOrderServicesTest (Test WAR Module)**
   - Test classes: 4 integration test classes
   - XML: web.xml

4. **CustomerOrderServicesApp (EAR Module)**
   - XML: application.xml

#### Files Requiring Changes

**Java Source Files (Production):** ~15 files
```
CustomerOrderServices/ejbModule/org/pwte/example/domain/
├── AbstractCustomer.java
├── Address.java
├── BusinessCustomer.java
├── Category.java
├── LineItem.java
├── LineItemId.java
├── Order.java
├── Product.java
└── ResidentialCustomer.java

CustomerOrderServices/ejbModule/org/pwte/example/service/
├── CustomerOrderServicesImpl.java
└── ProductSearchServiceImpl.java

CustomerOrderServicesWeb/src/org/pwte/example/app/
└── CustomerServicesApp.java

CustomerOrderServicesWeb/src/org/pwte/example/resources/
├── CategoryResource.java
├── CustomerOrderResource.java
└── ProductResource.java
```

**Java Test Files:** ~7 files
```
CustomerOrderServices/src/test/java/org/pwte/example/service/
└── CustomerOrderServicesImplTest.java

CustomerOrderServicesWeb/src/test/java/org/pwte/example/resources/
├── CategoryResourceTest.java
├── CustomerOrderResourceTest.java
└── ProductResourceTest.java

CustomerOrderServicesTest/src/org/pwte/example/jaxrs/test/
├── CustomerOrderRESTTest.java
└── ProductRESTSearchTest.java

CustomerOrderServicesTest/src/org/pwte/example/jpa/test/
├── CustomerOrderServicesTest.java
└── ProductSearchServiceTest.java
```

**XML Configuration Files:** ~5 files
```
CustomerOrderServices/ejbModule/META-INF/
├── persistence.xml (version 2.0 → 3.1)
└── orm.xml (version 1.0 → 3.1)

CustomerOrderServicesWeb/WebContent/WEB-INF/
└── web.xml (version 3.0 → 6.0)

CustomerOrderServicesTest/WebContent/WEB-INF/
└── web.xml (version 2.5 → 6.0)

CustomerOrderServicesApp/META-INF/
└── application.xml (version 6 → 10)
```

**Maven POM Files:** 3 files
```
CustomerOrderServicesProject/pom.xml (parent)
CustomerOrderServices/pom.xml
CustomerOrderServicesWeb/pom.xml
```

**Total Files to Modify:** ~30 files

---

## Migration Strategy

### Approach: Automated with OpenRewrite + Manual Validation

We will use a hybrid approach:

1. **OpenRewrite Plugin** - Automate the bulk of Java source changes
2. **Manual Updates** - Handle XML descriptors and POM dependencies
3. **Validation** - Ensure compilation and test success

### OpenRewrite Recipe

Recipe: `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta`

This recipe automatically handles:
- Package/import renaming from javax.* to jakarta.*
- Annotation updates
- Some API signature changes

---

## Phase-by-Phase Execution Plan

### Phase 1: Environment Setup ✅

**Tasks:**
- [x] Create migration branch: `migration/task-003-jakarta-migration`
- [x] Initialize progress tracking files
- [x] Create execution plan (this document)
- [x] Get user approval

**Commits:** 1
**Estimated Time:** 30 minutes

---

### Phase 2: Configure OpenRewrite

**Tasks:**
- [ ] Add OpenRewrite Maven plugin to parent POM
- [ ] Configure Jakarta migration recipe
- [ ] Test OpenRewrite dry-run

**Changes:**
- `CustomerOrderServicesProject/pom.xml` - Add plugin configuration

**Commits:** 1
**Estimated Time:** 1 hour

**POM Changes:**
```xml
<plugin>
  <groupId>org.openrewrite.maven</groupId>
  <artifactId>rewrite-maven-plugin</artifactId>
  <version>5.15.0</version>
  <configuration>
    <activeRecipes>
      <recipe>org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta</recipe>
    </activeRecipes>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>org.openrewrite.recipe</groupId>
      <artifactId>rewrite-migrate-java</artifactId>
      <version>2.8.0</version>
    </dependency>
  </dependencies>
</plugin>
```

---

### Phase 3: Update Maven Dependencies

**Tasks:**
- [ ] Update parent POM with Jakarta EE 10 dependencies
- [ ] Replace all javax.* dependencies with jakarta.* equivalents
- [ ] Update dependency versions to Jakarta EE 10 compatible versions

**Changes:**
- `CustomerOrderServicesProject/pom.xml` - Dependency management
- `CustomerOrderServices/pom.xml` - Module dependencies
- `CustomerOrderServicesWeb/pom.xml` - Module dependencies

**Commits:** 1
**Estimated Time:** 2 hours

**Key Dependency Changes:**

| Before (Java EE 7) | After (Jakarta EE 10) |
|--------------------|----------------------|
| `javax:javaee-api:7.0` | `jakarta.platform:jakarta.jakartaee-api:10.0.0` |
| `javax.persistence:javax.persistence-api:2.1` | `jakarta.persistence:jakarta.persistence-api:3.1.0` |
| `javax.servlet:javax.servlet-api:3.0` | `jakarta.servlet:jakarta.servlet-api:6.0.0` |
| `javax.ws.rs:javax.ws.rs-api:2.0` | `jakarta.ws.rs:jakarta.ws.rs-api:3.1.0` |
| `javax.ejb:javax.ejb-api:3.2` | `jakarta.ejb:jakarta.ejb-api:4.0.1` |
| `javax.validation:validation-api:1.1` | `jakarta.validation:jakarta.validation-api:3.0.2` |
| `javax.json:javax.json-api:1.0` | `jakarta.json:jakarta.json-api:2.1.2` |

---

### Phase 4: Run OpenRewrite Migration

**Tasks:**
- [ ] Execute `mvn rewrite:run` from project root
- [ ] Review OpenRewrite changes
- [ ] Verify all Java imports updated
- [ ] Commit automated changes

**Affected Files:** ~22 Java files

**Commits:** 1
**Estimated Time:** 2 hours

**Expected Transformations:**
```java
// BEFORE
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

// AFTER
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
```

---

### Phase 5: Update XML Descriptors

**Tasks:**
- [ ] Update persistence.xml to Jakarta EE 10 schema
- [ ] Update orm.xml to Jakarta EE 10 schema
- [ ] Update web.xml files to Jakarta EE 10 schema
- [ ] Update application.xml to Jakarta EE 10 schema
- [ ] Commit XML changes

**Commits:** 1
**Estimated Time:** 2 hours

#### persistence.xml Changes
```xml
<!-- BEFORE -->
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
                                 http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
             version="2.0">

<!-- AFTER -->
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">
```

#### orm.xml Changes
```xml
<!-- BEFORE -->
<entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/orm
                                     http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"
                 version="1.0">

<!-- AFTER -->
<entity-mappings xmlns="https://jakarta.ee/xml/ns/persistence/orm"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence/orm
                                     https://jakarta.ee/xml/ns/persistence/orm_3_1.xsd"
                 version="3.1">
```

#### web.xml Changes
```xml
<!-- BEFORE (version 3.0) -->
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                             http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">

<!-- AFTER (version 6.0) -->
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
```

#### application.xml Changes
```xml
<!-- BEFORE -->
<application xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                                 http://java.sun.com/xml/ns/javaee/application_6.xsd"
             version="6">

<!-- AFTER -->
<application xmlns="https://jakarta.ee/xml/ns/jakartaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                                 https://jakarta.ee/xml/ns/jakartaee/application_10.xsd"
             version="10">
```

---

### Phase 6: Manual Cleanup & Verification

**Tasks:**
- [ ] Search for any remaining javax.* references in Java files
- [ ] Manually fix any missed imports
- [ ] Verify javax.naming.* remains unchanged (Java SE, not Java EE)
- [ ] Commit manual fixes

**Commits:** 1 (if needed)
**Estimated Time:** 2 hours

**Verification Commands:**
```powershell
# Find remaining Java EE javax.* references (should be empty)
rg "import javax\.(persistence|ejb|ws\.rs|servlet|annotation\.security)" --type java

# Verify javax.naming.* still exists (should find results - this is correct)
rg "import javax\.naming\." --type java

# Find XML namespace references
rg "xmlns=\"http://java\.sun\.com" --type xml
```

---

### Phase 7: Build Validation

**Tasks:**
- [ ] Run `mvn clean compile` on all modules
- [ ] Fix any compilation errors
- [ ] Document API breaking changes (if any)
- [ ] Commit compilation fixes

**Modules to Build:**
1. CustomerOrderServicesProject (parent)
2. CustomerOrderServices (EJB)
3. CustomerOrderServicesWeb (WAR)
4. CustomerOrderServicesTest (Test WAR)
5. CustomerOrderServicesApp (EAR)

**Commits:** 1+ (depending on issues)
**Estimated Time:** 3 hours

**Potential Issues:**
- Missing Jakarta dependencies
- API signature changes in Jakarta EE 10
- Incompatible versions

---

### Phase 8: Test Validation

**Tasks:**
- [ ] Run `mvn clean test` on all modules
- [ ] Fix test failures due to namespace changes
- [ ] Update test mocks if needed (EntityManager, SessionContext)
- [ ] Verify all 163 tests still pass
- [ ] Commit test fixes

**Commits:** 1+ (depending on issues)
**Estimated Time:** 4 hours

**Expected Test Updates:**
- Mock imports updated automatically by OpenRewrite
- EntityManager/Query API remains compatible
- JAX-RS Response API remains compatible

---

### Phase 9: Integration Testing

**Tasks:**
- [ ] Build complete EAR package: `mvn clean package`
- [ ] Verify all modules package successfully
- [ ] Check WAR/EAR file structure
- [ ] Document any packaging issues

**Commits:** 1 (if fixes needed)
**Estimated Time:** 2 hours

---

### Phase 10: Documentation & Summary

**Tasks:**
- [ ] Create migration summary document
- [ ] Document all commits made
- [ ] List all files changed
- [ ] Create before/after comparison
- [ ] Document lessons learned
- [ ] Create handoff documentation

**Deliverables:**
- `summary.md` - Migration summary report
- `handoff.md` - Next steps and recommendations
- Updated `progress.md` - Final status
- Completed `todos.md` - All checkboxes marked

**Commits:** N/A (documentation only)
**Estimated Time:** 3 hours

---

## Risk Assessment

### High Risk Items

1. **API Breaking Changes**
   - **Risk:** Jakarta EE 10 may have API changes from Java EE 7
   - **Mitigation:** Thorough compilation and testing
   - **Impact:** Medium - May require code adjustments

2. **XML Schema Compatibility**
   - **Risk:** Application server may not support Jakarta EE 10 schemas
   - **Mitigation:** Use correct schema versions, test deployment
   - **Impact:** High - Deployment failure

3. **Dependency Conflicts**
   - **Risk:** Transitive dependencies may pull in javax.* versions
   - **Mitigation:** Use dependency:tree to check, exclude if needed
   - **Impact:** High - Runtime classpath issues

### Medium Risk Items

1. **OpenRewrite Coverage**
   - **Risk:** OpenRewrite may miss some imports
   - **Mitigation:** Manual verification after automation
   - **Impact:** Medium - Additional manual work

2. **Test Framework Compatibility**
   - **Risk:** Mockito/JUnit may not work with Jakarta namespace
   - **Mitigation:** Use latest versions already configured
   - **Impact:** Low - Already on compatible versions

### Low Risk Items

1. **Documentation Gaps**
   - **Risk:** Missing information about changes
   - **Mitigation:** Thorough documentation phase
   - **Impact:** Low - Can be fixed post-migration

---

## Success Criteria

- [ ] All javax.* Java EE imports replaced with jakarta.*
- [ ] All XML descriptors use Jakarta EE 10 schemas
- [ ] `mvn clean compile` succeeds for all modules
- [ ] `mvn clean test` passes all 163 tests
- [ ] `mvn clean package` creates valid EAR
- [ ] No javax.* references in Java/XML (except javax.naming which is Java SE)
- [ ] All commits follow conventional commit format
- [ ] Complete documentation generated

---

## Timeline Estimate

| Phase | Tasks | Estimated Hours |
|-------|-------|----------------|
| Phase 1: Environment Setup | 2 | 0.5 |
| Phase 2: Configure OpenRewrite | 3 | 1.0 |
| Phase 3: Update Maven Dependencies | 3 | 2.0 |
| Phase 4: Run OpenRewrite Migration | 4 | 2.0 |
| Phase 5: Update XML Descriptors | 5 | 2.0 |
| Phase 6: Manual Cleanup | 4 | 2.0 |
| Phase 7: Build Validation | 4 | 3.0 |
| Phase 8: Test Validation | 5 | 4.0 |
| Phase 9: Integration Testing | 4 | 2.0 |
| Phase 10: Documentation | 6 | 3.0 |
| **Total** | **40 tasks** | **21.5 hours** |

**Buffer:** 3.5 hours  
**Total Estimated:** 25 hours (AI-assisted)

---

## Commit Strategy

Planned commits (5-8 total):

1. `chore: create branch for Jakarta EE migration task`
2. `build: add OpenRewrite plugin for Jakarta migration`
3. `build: upgrade to Jakarta EE 10 dependencies`
4. `refactor: migrate javax.* to jakarta.* namespace (OpenRewrite)`
5. `refactor: update XML descriptors to Jakarta EE 10 schemas`
6. `fix: resolve Jakarta EE compilation errors` (if needed)
7. `fix: update tests for Jakarta EE 10 API changes` (if needed)
8. `docs: add Jakarta EE migration summary and handoff docs`

---

## Dependencies & Prerequisites

### Required Tools
- ✅ Maven 3.9.11 (already configured)
- ✅ Java 17 JDK (already installed)
- ✅ Git (for branch management)

### Completed Tasks
- ✅ TASK-001: Java 17 Upgrade
- ✅ TASK-002: Test Foundation (163 tests)

### External Dependencies
- OpenRewrite Maven Plugin 5.15.0
- OpenRewrite Recipe: rewrite-migrate-java 2.8.0
- Jakarta EE 10 API artifacts

---

## Post-Migration Validation Checklist

- [ ] No compilation errors in any module
- [ ] All 163 unit/service tests pass
- [ ] JaCoCo coverage remains ≥72%
- [ ] No javax.* imports (except javax.naming.*)
- [ ] All XML files use Jakarta schemas
- [ ] EAR builds successfully
- [ ] WAR files include correct dependencies
- [ ] No classpath conflicts
- [ ] Documentation complete

---

## Next Steps After Completion

1. **Merge to agent-test branch** - After user approval
2. **TASK-004: WebSphere → Open Liberty** - Migrate to Jakarta-compatible server
3. **TASK-005: Spring Boot Migration** - Modernize to Spring Boot 3.x
4. **TASK-006: Azure Deployment** - Deploy to Azure App Service

---

## References

- [Jakarta EE 10 Specification](https://jakarta.ee/specifications/platform/10/)
- [OpenRewrite Jakarta Migration](https://docs.openrewrite.org/recipes/java/migrate/jakarta/javaxmigrationtojakarta)
- [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)
- [Jakarta Servlet 6.0](https://jakarta.ee/specifications/servlet/6.0/)
- [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)

---

**Status:** ✅ PLAN COMPLETE - Ready for user approval  
**Next Action:** Present plan to user and await approval to proceed

**Created by:** AI Migration Assistant  
**Date:** November 5, 2025  
**Document Version:** 1.0
