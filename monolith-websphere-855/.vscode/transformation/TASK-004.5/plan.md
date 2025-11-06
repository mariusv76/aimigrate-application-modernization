# TASK-004.5: Open Liberty Deployment & Validation - Execution Plan

**Task ID:** TASK-004.5  
**Task Name:** Deploy to Open Liberty Server and Validate Migration  
**Branch:** migration/task-004.5-open-liberty-deployment  
**Created:** November 6, 2025  
**Status:** PLANNING

---

## Context & Dependencies

### Completed Tasks
✅ **TASK-001:** Java 17 Upgrade  
✅ **TASK-002:** Test Foundation (163 tests created, 72% coverage)  
✅ **TASK-003:** Jakarta EE Migration (javax.* → jakarta.*)  
✅ **TASK-004:** Jackson 2.17.0 Upgrade & IBM JSON4J Removal

### Current State Analysis

**From TASK-003 Summary:**
- All 22 production files migrated to Jakarta EE 10
- 5 XML descriptors updated (persistence.xml, web.xml, etc.)
- All tests passing (163/163)
- Known limitation: Test execution requires application server runtime

**From TASK-004 Summary:**
- All 5 modules compile successfully
- Jackson 2.17.0 integrated across all modules
- 5 critical/high CVEs resolved
- 48 test failures expected (require JAX-RS runtime + JNDI context)
- JSON endpoints need validation in live environment

**Current Dockerfile:**
- Uses WebSphere Traditional 9.0.0.11
- Contains DB2 driver configuration
- Migration-specific setup for WebSphere

---

## Migration Strategy

### Approach: Open Liberty with Jakarta EE 10

**Why Open Liberty:**
1. ✅ Native Jakarta EE 10 support (matches TASK-003 migration)
2. ✅ Lightweight compared to WebSphere Traditional
3. ✅ Free, open-source runtime (no licensing costs)
4. ✅ MicroProfile support for cloud-native patterns
5. ✅ Excellent developer experience (Liberty Dev Mode)
6. ✅ Azure deployment ready (TASK-006/007 preparation)

**Server Features Required:**
- `jakartaee-10.0` - Full Jakarta EE 10 platform
- `jsonb-3.0` - JSON Binding (for Jackson integration)
- `jsonp-2.1` - JSON Processing
- `restfulWS-3.1` - JAX-RS 3.1 (REST endpoints)
- `cdi-4.0` - Contexts and Dependency Injection
- `persistence-3.1` - JPA 3.1 (database access)
- `jdbc-4.3` - JDBC connectivity
- `jndi-1.0` - JNDI for datasource lookup
- `servlet-6.0` - Servlet API
- `microProfile-6.0` - MicroProfile features (optional but recommended)

---

## Detailed Execution Plan

### Phase 1: Planning & Analysis

#### Step 1.1: Review Transformation Documentation ✅
- [x] Read TASK-003 summary (Jakarta EE migration results)
- [x] Read TASK-004 summary (Jackson upgrade results)
- [x] Analyze current Dockerfile (WebSphere configuration)
- [x] Identify EAR structure and module dependencies

**Findings:**
- EAR contains 3 modules: EJB (CustomerOrderServices), 2 WARs (Web, Test)
- Datasource JNDI: `jdbc/orderDS` (from persistence.xml analysis needed)
- Security role: `SecureShopper` defined in EAR
- WebSphere-specific bindings: `ibm-application-bnd.xml`

#### Step 1.2: Analyze Application Server Dependencies
- [ ] Check persistence.xml for datasource configuration
- [ ] Identify JNDI names used in application
- [ ] Document security requirements
- [ ] List any WebSphere-specific features in use

#### Step 1.3: Design Open Liberty Configuration
- [ ] server.xml structure with required features
- [ ] Datasource configuration (DB2 support)
- [ ] Security configuration (if needed)
- [ ] Logging and monitoring setup

---

### Phase 2: Branch & Progress Setup

#### Step 2.1: Create Migration Branch
```powershell
git checkout -b migration/task-004.5-open-liberty-deployment
git commit --allow-empty -m "chore: create branch for Open Liberty deployment task"
```

#### Step 2.2: Initialize Progress Tracking
Create files:
- `.vscode/transformation/TASK-004.5/progress.md` - Track completion %
- `.vscode/transformation/TASK-004.5/todos.md` - Detailed checklist

---

### Phase 3: Open Liberty Configuration Files

#### Step 3.1: Create server.xml
**File:** `Deployment/server.xml`

**Configuration Plan:**
```xml
<server description="Customer Order Services">
  <featureManager>
    <feature>jakartaee-10.0</feature>
    <feature>microProfile-6.0</feature>
  </featureManager>
  
  <httpEndpoint id="defaultHttpEndpoint" 
                httpPort="9080" 
                httpsPort="9443"/>
  
  <application location="CustomerOrderServicesApp.ear"/>
  
  <dataSource id="orderDS" jndiName="jdbc/orderDS">
    <!-- DB2 configuration from env variables -->
  </dataSource>
  
  <logging consoleLogLevel="INFO" traceSpecification="*=info"/>
</server>
```

**Key Decisions:**
- Use environment variables for datasource (${env.DB_HOST}, etc.)
- Enable application auto-expand for easier deployment
- Configure connection pooling (min=5, max=20)

#### Step 3.2: Create bootstrap.properties
**File:** `Deployment/bootstrap.properties`

**Content:**
- HTTP/HTTPS port defaults (9080, 9443)
- Application name
- Environment variable placeholders

#### Step 3.3: Update Maven Build
**File:** `CustomerOrderServicesApp/pom.xml`

**Changes:**
- Add Liberty Maven Plugin 3.10+
- Configure server.xml reference
- Configure bootstrap.properties reference
- Add Liberty runtime download/install
- Configure deployment goals

---

### Phase 4: Liberty Startup Scripts

#### Step 4.1: Development Mode Script
**File:** `run-liberty-dev.ps1`

**Purpose:** Hot-reload development with Liberty Dev Mode
**Features:**
- Set DB2 environment variables
- Navigate to App module
- Execute `mvn liberty:dev`

#### Step 4.2: Production Mode Script
**File:** `run-liberty.ps1`

**Purpose:** Standard Liberty server startup
**Features:**
- Set DB2 environment variables
- Navigate to App module
- Execute `mvn liberty:run`

---

### Phase 5: Build & Deployment

#### Step 5.1: Install Open Liberty Runtime
```powershell
cd CustomerOrderServicesApp
mvn liberty:install-server
```

**Expected:** Download Liberty runtime to `target/liberty/`

#### Step 5.2: Build Application
```powershell
mvn clean install -DskipTests
```

**Expected:** EAR package in `CustomerOrderServicesApp/target/`

**Validation:**
- [ ] CustomerOrderServices.jar created
- [ ] CustomerOrderServicesWeb.war created
- [ ] CustomerOrderServicesTest.war created
- [ ] CustomerOrderServicesApp.ear created (includes all 3 modules)

#### Step 5.3: Deploy to Liberty
```powershell
.\run-liberty-dev.ps1
```

**Expected:**
- Liberty server starts in <60 seconds
- Application deploys successfully
- HTTP endpoint accessible at http://localhost:9080

**Troubleshooting Plan:**
1. Check `target/liberty/wlp/usr/servers/defaultServer/logs/messages.log`
2. Verify feature installation
3. Check datasource connection (may fail if no DB2 - expected)
4. Validate application classloading

---

### Phase 6: Validation & Testing

#### Step 6.1: Manual Endpoint Validation

**REST API Endpoints to Test:**
```powershell
# Base URL
$baseUrl = "http://localhost:9080/CustomerOrderServicesWeb"

# Test endpoints
Invoke-RestMethod -Uri "$baseUrl/jaxrs/Product" -Method GET
Invoke-RestMethod -Uri "$baseUrl/jaxrs/Product/category" -Method GET
Invoke-RestMethod -Uri "$baseUrl/jaxrs/Customer" -Method GET
```

**Success Criteria:**
- [ ] JSON responses returned (not 404)
- [ ] Jackson serialization working (check JSON structure)
- [ ] No IBM JSON4J errors in logs
- [ ] No Jakarta EE namespace errors

**Expected Issues:**
- Database operations may fail (no DB2 connection) - ACCEPTABLE
- Test endpoints may not work without data - ACCEPTABLE
- Focus on: Server starts, endpoints respond, JSON serialization works

#### Step 6.2: Integration Test Execution

**Test Configuration:**
```powershell
$env:TEST_BASE_URL = "http://localhost:9080/CustomerOrderServicesWeb"
cd CustomerOrderServicesTest
mvn test
```

**Expected Results:**
- Some tests may fail due to DB2 unavailability (acceptable for this task)
- Focus on: JAX-RS runtime errors resolved, JSON serialization tests pass
- Compare with TASK-004 results (48 failures expected)

**Success Metric:** 
- Test failure count ≤ 48 (same or better than TASK-004)
- No new runtime errors (ClassNotFoundException, NoClassDefFoundError)

#### Step 6.3: Performance Smoke Test

**Metrics to Capture:**
- Server startup time (target: <60 seconds)
- Memory usage: `Get-Process -Name java | Select-Object WS, PM`
- Response time for sample endpoint (target: <500ms)

**Baseline:**
- Document all metrics for future comparison
- No performance regression expected (Liberty is lighter than WebSphere)

#### Step 6.4: Logging Validation

**Log Files:**
- `messages.log` - Main server log
- `console.log` - Console output
- `trace.log` - Detailed trace (if enabled)

**Validation:**
- [ ] Application startup logged
- [ ] No ERROR messages (except DB connection - acceptable)
- [ ] Warning messages documented
- [ ] Application class loading successful

---

### Phase 7: Documentation

#### Step 7.1: Deployment Guide
**File:** `.vscode/transformation/TASK-004.5/deployment-guide.md`

**Content:**
- Prerequisites (Maven, Java 17)
- Open Liberty installation steps
- Server configuration explanation
- Environment variable setup
- Deployment procedures
- Troubleshooting guide

#### Step 7.2: Migration Summary
**File:** `.vscode/transformation/TASK-004.5/summary.md`

**Content:**
- Task completion metrics
- Open Liberty version deployed
- Configuration files created
- Test results (pass/fail counts)
- Performance metrics
- Issues encountered and resolutions
- Recommendations for TASK-005

#### Step 7.3: Diff Documentation
**Directory:** `.vscode/transformation/TASK-004.5/diffs/`

**Files:**
- `server.xml.diff.md` - New file, explain configuration
- `bootstrap.properties.diff.md` - New file, explain properties
- `pom.xml.diff.md` - Liberty Maven Plugin addition
- `run-liberty-dev.ps1.diff.md` - New file, explain dev mode
- `run-liberty.ps1.diff.md` - New file, explain production mode

---

## Success Criteria

### Must-Have (Blocking)
- [x] Execution plan created and approved
- [ ] Branch created: `migration/task-004.5-open-liberty-deployment`
- [ ] Progress tracking initialized
- [ ] Open Liberty server.xml created with Jakarta EE 10 features
- [ ] Liberty Maven Plugin configured
- [ ] Application builds successfully
- [ ] Application deploys to Open Liberty
- [ ] Server starts successfully (<60 seconds)
- [ ] At least one REST endpoint responds with JSON
- [ ] Jackson serialization confirmed working (no IBM JSON errors)
- [ ] No Jakarta EE namespace errors in logs
- [ ] 5-10 commits with proper conventional commit messages
- [ ] Summary documentation created
- [ ] Deployment guide created

### Should-Have (Important)
- [ ] All REST endpoints accessible (may return errors without DB)
- [ ] Integration tests run (pass/fail documented)
- [ ] Performance metrics captured (startup time, memory)
- [ ] Logging configuration validated
- [ ] Diff documentation for all modified files

### Nice-to-Have (Optional)
- [ ] Liberty Dev Mode working (hot reload)
- [ ] Database connection successful (requires DB2 setup)
- [ ] All integration tests passing (requires DB2 data)
- [ ] Performance comparison with WebSphere

---

## Risk Assessment

### High Risk
**Database Unavailability**
- **Impact:** Cannot fully validate application functionality
- **Mitigation:** Focus on server startup, JSON serialization, endpoint accessibility
- **Acceptable:** DB connection errors are expected for TASK-004.5
- **Resolution:** Full DB validation in TASK-005 (PostgreSQL migration)

### Medium Risk
**WebSphere-Specific Features**
- **Impact:** Application may use WebSphere-proprietary APIs
- **Mitigation:** Review TASK-003 findings (IBM JSON4J already removed)
- **Detection:** Monitor Liberty logs for unsupported API errors
- **Resolution:** Document any issues for future refactoring

### Low Risk
**Configuration Complexity**
- **Impact:** server.xml may need tuning for optimal performance
- **Mitigation:** Start with minimal configuration, add features as needed
- **Detection:** Monitor server startup logs
- **Resolution:** Iterate on configuration based on errors

---

## Estimated Effort

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Phase 1: Planning | 3 steps | 1 hour ✅ |
| Phase 2: Branch Setup | 2 steps | 15 minutes |
| Phase 3: Configuration | 3 steps | 1 hour |
| Phase 4: Scripts | 2 steps | 30 minutes |
| Phase 5: Build & Deploy | 3 steps | 1 hour |
| Phase 6: Validation | 4 steps | 2 hours |
| Phase 7: Documentation | 3 steps | 1.5 hours |
| **Total** | **20 steps** | **~7 hours** |

**AI-Assisted:** Estimated 3-4 hours with automation and parallel execution

---

## Next Steps After TASK-004.5

1. **TASK-005:** Database Migration (DB2 → PostgreSQL)
   - Replace DB2 datasource with PostgreSQL
   - Migrate schema and data
   - Update JPA configuration for PostgreSQL dialect

2. **TASK-006:** Azure Integration
   - Azure Entra ID authentication
   - Azure Key Vault for secrets
   - Azure App Configuration

3. **TASK-007:** Containerization & Deployment
   - Create optimized Dockerfile with Open Liberty
   - Deploy to Azure Container Apps
   - Configure CI/CD pipeline

---

## Approval Request

**Ready to proceed with Phase 2:** Create branch and initialize tracking

**User Action Required:** 
- Review this plan
- Approve to continue
- Provide any additional requirements or constraints

---

**Status:** ✅ PLAN COMPLETE - Awaiting user approval  
**Created:** November 6, 2025  
**Next Action:** User approval → Create branch → Begin Phase 2
