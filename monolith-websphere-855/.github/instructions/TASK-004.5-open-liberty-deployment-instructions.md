# Migration Task Prompt: Open Liberty Deployment

## Mission
You are an AI agent tasked with performing **TASK-004.5** for the Customer Order Services application: **Deploy to Open Liberty Server and Validate Migration**. This intermediate task ensures the application runs successfully on Open Liberty before proceeding to database migration and Azure deployment.

## Task Overview
**Task ID:** TASK-004.5  
**Task Name:** Open Liberty Deployment & Validation  
**Priority:** 🔴 CRITICAL  
**Dependencies:** 
- TASK-001 (Java 17 Upgrade) completed
- TASK-002 (Test Foundation) completed
- TASK-003 (Jakarta EE Migration) completed
- TASK-004 (Jackson Upgrade) completed
**Estimated Effort:** 40 hours manual / 20 hours AI-assisted  
**Success Criteria:** Application deploys to Open Liberty, all integration tests pass, JSON endpoints validated

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.vscode/transformation/TASK-003/summary.md` (Jakarta EE migration results)
   - Review `.vscode/transformation/TASK-004/summary.md` (Jackson upgrade results)
   - Review `Deployment/Dockerfile` for current configuration
   - Check existing Open Liberty configuration in project

2. **Create a detailed execution plan**
   - Analyze current server configuration (WebSphere vs Open Liberty)
   - Document Open Liberty features needed (JAX-RS, JPA, CDI, etc.)
   - Plan server.xml configuration
   - List datasource configuration requirements
   - Identify testing strategy for deployment validation
   - Save plan to `.vscode/transformation/TASK-004.5/plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-004.5-open-liberty-deployment`
   - Branch from: current branch (agent-test or your working branch)
   - Commit message: "chore: create branch for Open Liberty deployment task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-004.5/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-004.5/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation - Open Liberty Configuration

6. **Create Open Liberty server.xml**
   - Create `Deployment/server.xml`:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <server description="Customer Order Services Open Liberty Server">
     
       <!-- Enable Liberty features -->
       <featureManager>
         <feature>jakartaee-10.0</feature>
         <feature>microProfile-6.0</feature>
         <feature>jsonb-3.0</feature>
         <feature>jsonp-2.1</feature>
         <feature>restfulWS-3.1</feature>
         <feature>cdi-4.0</feature>
         <feature>persistence-3.1</feature>
         <feature>jdbc-4.3</feature>
         <feature>jndi-1.0</feature>
         <feature>servlet-6.0</feature>
       </featureManager>
     
       <!-- HTTP Endpoint -->
       <httpEndpoint id="defaultHttpEndpoint"
                     httpPort="9080"
                     httpsPort="9443"
                     host="*" />
     
       <!-- Application Configuration -->
       <application id="CustomerOrderServicesApp" 
                    location="CustomerOrderServicesApp.ear"
                    type="ear">
         <classloader delegation="parentLast" />
       </application>
     
       <!-- Datasource Configuration (DB2 for now) -->
       <dataSource id="orderDS" jndiName="jdbc/orderDS">
         <jdbcDriver libraryRef="db2DriverLib"/>
         <properties.db2.jcc 
           databaseName="${env.DB_NAME}"
           serverName="${env.DB_HOST}"
           portNumber="${env.DB_PORT}"
           user="${env.DB_USER}"
           password="${env.DB_PASSWORD}"/>
         <connectionManager maxPoolSize="20" minPoolSize="5"/>
       </dataSource>
     
       <library id="db2DriverLib">
         <fileset dir="${server.config.dir}/lib" includes="db2jcc*.jar"/>
       </library>
     
       <!-- Logging -->
       <logging 
         traceSpecification="*=info:org.pwte.example.*=all"
         maxFileSize="100"
         maxFiles="10"
         consoleLogLevel="INFO"/>
     
       <!-- Application Security -->
       <applicationManager autoExpand="true"/>
       
       <!-- HTTP Session -->
       <httpSession cookieHttpOnly="true" cookieSecure="true"/>
     
     </server>
     ```
   - Commit: "config: create Open Liberty server.xml configuration"

7. **Create bootstrap.properties**
   - Create `Deployment/bootstrap.properties`:
     ```properties
     # Default ports
     default.http.port=9080
     default.https.port=9443
     
     # Application name
     app.name=CustomerOrderServices
     
     # Environment-specific overrides
     # DB_HOST=localhost
     # DB_PORT=50000
     # DB_NAME=ORDERDB
     # DB_USER=db2inst1
     # DB_PASSWORD=db2inst1
     ```
   - Commit: "config: add bootstrap properties for Open Liberty"

8. **Update Maven Build for Open Liberty**
   - Update `CustomerOrderServicesApp/pom.xml` to add Liberty Maven Plugin:
     ```xml
     <build>
       <plugins>
         <plugin>
           <groupId>io.openliberty.tools</groupId>
           <artifactId>liberty-maven-plugin</artifactId>
           <version>3.10</version>
           <configuration>
             <serverName>defaultServer</serverName>
             <serverXmlFile>${project.basedir}/../Deployment/server.xml</serverXmlFile>
             <bootstrapPropertiesFile>${project.basedir}/../Deployment/bootstrap.properties</bootstrapPropertiesFile>
             <installDirectory>${project.build.directory}/liberty</installDirectory>
             <looseApplication>true</looseApplication>
             <stripVersion>true</stripVersion>
           </configuration>
           <executions>
             <execution>
               <id>install-liberty</id>
               <phase>prepare-package</phase>
               <goals>
                 <goal>install-server</goal>
               </goals>
             </execution>
             <execution>
               <id>deploy-app</id>
               <phase>package</phase>
               <goals>
                 <goal>deploy</goal>
               </goals>
             </execution>
           </executions>
         </plugin>
       </plugins>
     </build>
     ```
   - Commit: "build: add Liberty Maven Plugin for deployment automation"

9. **Create Liberty Development Mode Scripts**
   - Create `run-liberty-dev.ps1`:
     ```powershell
     # Run Open Liberty in dev mode
     Write-Host "Starting Open Liberty in Development Mode..." -ForegroundColor Green
     
     $env:DB_HOST = "localhost"
     $env:DB_PORT = "50000"
     $env:DB_NAME = "ORDERDB"
     $env:DB_USER = "db2inst1"
     $env:DB_PASSWORD = "db2inst1"
     
     Set-Location CustomerOrderServicesApp
     mvn liberty:dev
     ```
   - Create `run-liberty.ps1`:
     ```powershell
     # Run Open Liberty in production mode
     Write-Host "Starting Open Liberty Server..." -ForegroundColor Green
     
     $env:DB_HOST = "localhost"
     $env:DB_PORT = "50000"
     $env:DB_NAME = "ORDERDB"
     $env:DB_USER = "db2inst1"
     $env:DB_PASSWORD = "db2inst1"
     
     Set-Location CustomerOrderServicesApp
     mvn liberty:run
     ```
   - Commit: "chore: add Liberty server startup scripts"

10. **Update progress tracking after each commit**
    - Update `TASK-004.5-progress.md` with completed steps
    - Check off items in `TASK-004.5-todos.md`
    - Document any issues or deviations

### Phase 4: Implementation - Deployment Validation

11. **Install and Configure Open Liberty Locally**
    - Download Open Liberty runtime:
      ```powershell
      # Using Maven to install Liberty
      cd CustomerOrderServicesApp
      mvn liberty:install-server
      ```
    - Verify installation:
      ```powershell
      mvn liberty:version
      ```
    - Document Liberty version in progress file

12. **Build and Package Application**
    - Clean build all modules:
      ```powershell
      mvn clean install
      ```
    - Verify EAR package created:
      ```powershell
      Get-ChildItem -Path CustomerOrderServicesApp/target -Filter "*.ear"
      ```
    - Document build output in progress file

13. **Deploy Application to Open Liberty**
    - Start Liberty in dev mode:
      ```powershell
      .\run-liberty-dev.ps1
      ```
    - Monitor startup logs for errors
    - Verify application deployed successfully
    - Check endpoints are accessible:
      ```powershell
      # Test root endpoint
      Invoke-WebRequest -Uri "http://localhost:9080/CustomerOrderServicesWeb/" -UseBasicParsing
      
      # Test REST API
      Invoke-WebRequest -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product" -UseBasicParsing
      ```
    - Document deployment status in progress file

14. **Validate Application Functionality**
    - **Test REST API Endpoints:**
      ```powershell
      # Get all products
      Invoke-RestMethod -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product" -Method GET
      
      # Get product by ID
      Invoke-RestMethod -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product/1" -Method GET
      
      # Get all categories
      Invoke-RestMethod -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product/category" -Method GET
      
      # Search products
      Invoke-RestMethod -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product?productName=laptop" -Method GET
      ```
    - **Test JSON Serialization (Jackson):**
      - Verify JSON responses are properly formatted
      - Check that Jackson annotations work correctly
      - Ensure no IBM JSON4J errors in logs
    - **Test Database Operations:**
      - Create a test customer (POST)
      - Retrieve customer (GET)
      - Update customer (PUT)
      - Verify JPA/database operations work
    - Document all test results in progress file

### Phase 5: Testing - Integration Tests

15. **Run Integration Tests Against Open Liberty**
    - Start Liberty server if not running
    - Configure test environment variables:
      ```powershell
      $env:TEST_BASE_URL = "http://localhost:9080/CustomerOrderServicesWeb"
      ```
    - Run integration tests:
      ```powershell
      cd CustomerOrderServicesTest
      mvn test -Dtest.base.url=$env:TEST_BASE_URL
      ```
    - Analyze test results:
      - Document pass/fail counts
      - Identify any failures related to Liberty runtime
      - Compare with previous test runs (from TASK-004)
    - Fix any Liberty-specific test failures
    - Commit fixes: "fix: resolve Open Liberty integration test failures"

16. **Run Performance Smoke Tests**
    - Test response times for key endpoints
    - Verify startup time is acceptable (<60 seconds)
    - Check memory usage:
      ```powershell
      # Check Liberty process memory
      Get-Process -Name java | Select-Object ProcessName, WS, PM
      ```
    - Document performance metrics in progress file

17. **Validate Logging and Monitoring**
    - Check Liberty logs:
      ```powershell
      Get-Content CustomerOrderServicesApp/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log -Tail 50
      ```
    - Verify application logs are captured
    - Ensure no ERROR or WARNING messages (except expected)
    - Test log levels can be adjusted
    - Document logging configuration in progress file

18. **Update todos and progress**
    - Mark deployment and testing steps complete
    - Document final Liberty deployment status
    - Update progress percentage

### Phase 6: Documentation & Summary

19. **Create deployment configuration guide**
    - Create `.vscode/transformation/TASK-004.5/deployment-guide.md`:
      - Open Liberty installation steps
      - server.xml configuration explanation
      - Datasource setup instructions
      - Environment variable requirements
      - Startup procedures
      - Troubleshooting common issues

20. **Generate migration summary**
    - Create `.vscode/transformation/TASK-004.5/summary.md` with:
      - Task completion date/time
      - Open Liberty version deployed
      - Configuration files created
      - Files modified (count and list)
      - Commits created (count and list with messages)
      - Deployment validation results
      - Integration test results (pass/fail counts)
      - Performance metrics
      - Issues encountered and resolutions
      - Lessons learned
      - Recommendations for next tasks (TASK-005)

21. **Generate diff documentation**
    - For EACH modified file, create `.vscode/transformation/TASK-004.5/diffs/{filename}.diff.md`
    - Include:
      - File path
      - Before/after comparison
      - Explanation of Liberty-specific changes
      - Rationale for configuration choices

22. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 7: Handoff

23. **Create final summary report**
    - Consolidate all findings
    - List all commits made
    - Show deployment validation results
    - Document Liberty server configuration
    - Provide next steps for TASK-005 (Database Migration)
    - Highlight any blockers
    - Present to user for review

24. **Prepare for TASK-005**
    - Document current database configuration (DB2)
    - Note any database-related issues discovered during deployment
    - Prepare recommendations for PostgreSQL migration

## Work Execution Guidelines

### Branch Management
- Create feature branch at start
- Regular commits (every logical change)
- Descriptive commit messages following conventional commits
- NO merge to main until user approval

### Progress Tracking
- Update progress file after EVERY significant step
- Use percentage completion (0-100%)
- Include timestamps for all updates
- Document blockers immediately

### Todo Management
- Create comprehensive todo list at start
- Check items off as completed: `- [x] Completed item`
- Add new items if discovered: `- [ ] New item (discovered during work)`
- Never delete items (preserve history)

### Commit Message Format
```
<type>(<scope>): <subject>

<body (optional)>

Task: TASK-004.5
```

Types: feat, config, build, fix, test, docs, chore
Examples:
- `config: create Open Liberty server.xml configuration`
- `build: add Liberty Maven Plugin for deployment automation`
- `fix: resolve Open Liberty integration test failures`
- `docs: add deployment configuration guide`

### Error Handling
- Document ALL deployment errors
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip validation steps

### File Organization
```
.vscode/transformation/TASK-004.5/
├── plan.md
├── progress.md
├── todos.md
├── summary.md
├── deployment-guide.md
└── diffs/
    ├── server.xml.diff.md
    ├── pom.xml.diff.md
    └── ... (one file per changed file)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-004.5-open-liberty-deployment`
2. ✅ Open Liberty server.xml with Jakarta EE 10 features
3. ✅ bootstrap.properties for environment configuration
4. ✅ Liberty Maven Plugin configured in POM
5. ✅ Liberty startup scripts (dev and production modes)
6. ✅ Application successfully deployed to Open Liberty
7. ✅ All REST endpoints validated (manual testing)
8. ✅ JSON serialization verified (Jackson working correctly)
9. ✅ Integration tests passing on Open Liberty
10. ✅ Performance smoke tests completed
11. ✅ Logging and monitoring validated
12. ✅ 5-10 commits with proper messages
13. ✅ Deployment configuration guide
14. ✅ Task plan, progress, todos, summary, and diff documentation
15. ✅ Final handoff report for user review

## Success Criteria

- [ ] Open Liberty server.xml created with all required features
- [ ] Liberty Maven Plugin configured and working
- [ ] Application builds successfully (`mvn clean install`)
- [ ] Application deploys to Open Liberty without errors
- [ ] Server starts in <60 seconds
- [ ] All REST API endpoints accessible and returning correct JSON
- [ ] Jackson serialization/deserialization working (no IBM JSON errors)
- [ ] Database operations functional (CRUD via JPA)
- [ ] Integration tests passing (>90% pass rate)
- [ ] No ERROR messages in Liberty logs (except known/expected)
- [ ] Performance metrics documented (response times, memory usage)
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document includes deployment validation results
- [ ] Deployment guide created with troubleshooting steps
- [ ] Diff files exist for every changed file
- [ ] User has clear instructions for next steps (TASK-005)

## Constraints & Guidelines

- **DO NOT** change business logic or functionality
- **DO NOT** modify database schema (that's TASK-005)
- **DO** use Open Liberty latest stable version (24.0.0.x or newer)
- **DO** test thoroughly before declaring deployment successful
- **DO** commit frequently (every logical change)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation
- **DO** validate JSON responses match expected Jackson output

## Tools & Resources

### Open Liberty Documentation
- [Open Liberty Features](https://openliberty.io/docs/latest/reference/feature/feature-overview.html)
- [Jakarta EE 10 Guide](https://openliberty.io/docs/latest/jakarta-ee.html)
- [Liberty Maven Plugin](https://github.com/OpenLiberty/ci.maven)
- [Server Configuration](https://openliberty.io/docs/latest/reference/config/server-configuration-overview.html)

### Testing Tools
- PowerShell `Invoke-RestMethod` for API testing
- Liberty Dev Mode for hot reload during testing
- Maven Surefire for integration test execution

### Troubleshooting Resources
- [Open Liberty Troubleshooting](https://openliberty.io/docs/latest/troubleshooting.html)
- Liberty logs: `target/liberty/wlp/usr/servers/defaultServer/logs/`
- Check `messages.log` and `console.log` for errors

## GitHub Copilot Usage Tips

For **server.xml configuration**, use prompts like:
- "Generate Open Liberty server.xml for Jakarta EE 10 with JAX-RS and JPA"
- "Configure Open Liberty datasource for DB2 with connection pooling"
- "Add logging configuration to Open Liberty server.xml"

For **deployment validation**, use prompts like:
- "Create PowerShell script to test REST API endpoints"
- "Write integration test for Open Liberty JAX-RS resource"
- "Generate curl commands to validate JSON endpoints"

For **troubleshooting**, use prompts like:
- "Explain this Open Liberty error: [paste error message]"
- "How to fix classloader issues in Open Liberty"
- "Configure Open Liberty for Jackson JSON provider"

## Start Command

Begin by:
1. Reading all transformation documentation files (TASK-003 and TASK-004 summaries)
2. Analyzing current project structure and dependencies
3. Creating the detailed plan with Open Liberty configuration strategy
4. Presenting the plan to me for approval
5. Waiting for my "continue" command before proceeding

**Remember:** This is a critical validation step. The application MUST work on Open Liberty before proceeding to database migration (TASK-005) and Azure deployment (TASK-006/007). Take your time, test thoroughly, document everything, and ask questions if anything is unclear.

---

**Ready to begin? Start with step 1: Read transformation documentation and create the execution plan.**
