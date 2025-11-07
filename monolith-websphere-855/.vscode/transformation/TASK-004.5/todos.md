# TASK-004.5: Open Liberty Deployment - Todo List

**Branch:** migration/task-004.5-open-liberty-deployment  
**Created:** November 6, 2025

---

## Phase 1: Planning & Preparation ✅
- [x] Read TASK-003 summary (Jakarta EE migration results)
- [x] Read TASK-004 summary (Jackson upgrade results)
- [x] Analyze Dockerfile (WebSphere configuration)
- [x] Check persistence.xml for datasource configuration
- [x] Identify JNDI names and database requirements
- [x] Design Open Liberty server.xml structure
- [x] Create execution plan (plan.md)
- [x] Get user approval

## Phase 2: Branch & Progress Setup ✅
- [x] Create branch: migration/task-004.5-open-liberty-deployment
- [x] Commit plan.md to branch
- [x] Create progress.md tracker
- [x] Create todos.md (this file)

## Phase 3: Open Liberty Configuration
- [ ] Create Deployment/server.xml with:
  - [ ] Feature manager (jakartaee-10.0, microProfile-6.0)
  - [ ] HTTP endpoint configuration (9080/9443)
  - [ ] Application configuration (EAR deployment)
  - [ ] DB2 datasource with JNDI jdbc/orderds
  - [ ] Connection pool settings (min=5, max=20)
  - [ ] Logging configuration
  - [ ] Security configuration (if needed)
- [ ] Create Deployment/bootstrap.properties
  - [ ] Default HTTP/HTTPS ports
  - [ ] Application name
  - [ ] Environment variable placeholders
- [ ] Update CustomerOrderServicesApp/pom.xml
  - [ ] Add Liberty Maven Plugin dependency
  - [ ] Configure server.xml reference
  - [ ] Configure bootstrap.properties reference
  - [ ] Add install-server goal
  - [ ] Add deploy goal
- [ ] Commit configuration files

## Phase 4: Liberty Startup Scripts
- [ ] Create run-liberty-dev.ps1
  - [ ] Set DB2 environment variables
  - [ ] Navigate to App module
  - [ ] Execute mvn liberty:dev
- [ ] Create run-liberty.ps1
  - [ ] Set DB2 environment variables
  - [ ] Navigate to App module
  - [ ] Execute mvn liberty:run
- [ ] Commit startup scripts

## Phase 5: Build & Deployment
- [ ] Install Open Liberty runtime
  - [ ] Run: mvn liberty:install-server
  - [ ] Verify Liberty downloaded to target/liberty/
  - [ ] Document Liberty version in progress.md
- [ ] Build application
  - [ ] Run: mvn clean install -DskipTests
  - [ ] Verify CustomerOrderServices.jar created
  - [ ] Verify CustomerOrderServicesWeb.war created
  - [ ] Verify CustomerOrderServicesTest.war created
  - [ ] Verify CustomerOrderServicesApp.ear created
  - [ ] Document build output in progress.md
- [ ] Deploy to Liberty
  - [ ] Run: .\run-liberty-dev.ps1
  - [ ] Monitor startup logs
  - [ ] Verify server starts (<60 seconds)
  - [ ] Verify application deploys successfully
  - [ ] Document deployment status in progress.md

## Phase 6: Validation & Testing
- [ ] Manual REST endpoint validation
  - [ ] Test: GET /jaxrs/Product (all products)
  - [ ] Test: GET /jaxrs/Product/category (categories)
  - [ ] Test: GET /jaxrs/Product/1 (product by ID)
  - [ ] Test: GET /jaxrs/Customer (customers)
  - [ ] Verify JSON responses (Jackson serialization)
  - [ ] Check for IBM JSON4J errors (should be none)
  - [ ] Check for Jakarta namespace errors (should be none)
  - [ ] Document endpoint test results
- [ ] Integration test execution
  - [ ] Set TEST_BASE_URL environment variable
  - [ ] Run: mvn test in CustomerOrderServicesTest
  - [ ] Document pass/fail counts
  - [ ] Compare with TASK-004 results (48 failures baseline)
  - [ ] Investigate any new failures
  - [ ] Document test results in progress.md
- [ ] Performance smoke test
  - [ ] Measure server startup time
  - [ ] Measure memory usage (Get-Process)
  - [ ] Measure sample endpoint response time
  - [ ] Document performance metrics
- [ ] Logging validation
  - [ ] Check messages.log
  - [ ] Check console.log
  - [ ] Verify no ERROR messages (except DB connection - OK)
  - [ ] Document any WARNING messages
  - [ ] Verify application startup logged correctly

## Phase 7: Documentation
- [ ] Create deployment-guide.md
  - [ ] Prerequisites (Maven, Java 17)
  - [ ] Open Liberty installation steps
  - [ ] server.xml configuration explanation
  - [ ] Datasource setup instructions
  - [ ] Environment variables required
  - [ ] Deployment procedures (dev and production)
  - [ ] Troubleshooting common issues
- [ ] Create summary.md
  - [ ] Task completion date/time
  - [ ] Open Liberty version deployed
  - [ ] Configuration files created (list)
  - [ ] Files modified (list with descriptions)
  - [ ] Commits created (list with messages)
  - [ ] Deployment validation results
  - [ ] Integration test results (pass/fail counts)
  - [ ] Performance metrics (startup, memory, response time)
  - [ ] Issues encountered and resolutions
  - [ ] Lessons learned
  - [ ] Recommendations for TASK-005
- [ ] Create diff documentation
  - [ ] .vscode/transformation/TASK-004.5/diffs/server.xml.diff.md
  - [ ] .vscode/transformation/TASK-004.5/diffs/bootstrap.properties.diff.md
  - [ ] .vscode/transformation/TASK-004.5/diffs/pom.xml.diff.md
  - [ ] .vscode/transformation/TASK-004.5/diffs/run-liberty-dev.ps1.diff.md
  - [ ] .vscode/transformation/TASK-004.5/diffs/run-liberty.ps1.diff.md
- [ ] Update progress.md final status
  - [ ] Mark task as COMPLETED
  - [ ] Update completion timestamp
  - [ ] Calculate actual vs estimated effort
  - [ ] Document final metrics

## Phase 8: Handoff
- [ ] Review all documentation
- [ ] Verify all commits have proper messages
- [ ] Create final handoff report
- [ ] Present to user for review
- [ ] Address any feedback
- [ ] Prepare recommendations for TASK-005 (Database Migration)

---

**Total Tasks:** 75  
**Completed:** 14 (19%)  
**In Progress:** 1  
**Remaining:** 60

---

**Last Updated:** November 6, 2025  
**Next Task:** Create server.xml configuration
