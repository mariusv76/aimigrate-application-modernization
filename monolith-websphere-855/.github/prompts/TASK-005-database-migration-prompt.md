# Migration Task Prompt: Database Migration - DB2 → PostgreSQL

## Mission
You are an AI agent tasked with performing **TASK-005** for the Customer Order Services application: **Migrate Database from IBM DB2 to Azure Database for PostgreSQL**. This task eliminates proprietary database dependencies and prepares for Azure deployment.

## Task Overview
**Task ID:** TASK-005  
**Task Name:** Database Migration (DB2 → PostgreSQL)  
**Priority:** 🔴 CRITICAL  
**Dependencies:** 
- TASK-001 (Java 17 Upgrade) completed
- TASK-002 (Test Foundation) completed
- TASK-003 (Jakarta EE Migration) completed
- TASK-004 (Jackson Upgrade) completed
**Estimated Effort:** 140 hours manual / 70 hours AI-assisted  
**Success Criteria:** Application runs on PostgreSQL, schema migrated, data migrated, all tests pass

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 2, Tasks 2.1-2.2
   - Review `.transformation/PLATFORM_MIGRATION_GUIDE.md` Database sections
   - Review Common/createOrderDB.sql and other SQL scripts

2. **Create a detailed execution plan**
   - Analyze current DB2 schema (from SQL scripts)
   - Identify DB2-specific SQL syntax and features
   - Document schema changes needed for PostgreSQL
   - Plan data migration strategy
   - List OpenJPA → Hibernate migration steps
   - Save plan to `.transformation/tasks/TASK-005-plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-005-database-migration`
   - Branch from: `migration/task-004-jackson-upgrade` (completed branch)
   - Commit message: "chore: create branch for database migration task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-005/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-005/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation - Schema Migration

6. **Update PostgreSQL Driver Dependency**
   - Add to `pom.xml`:
     ```xml
     <!-- PostgreSQL Driver -->
     <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <version>42.7.1</version>
     </dependency>
     ```
   - Remove DB2 driver if present:
     ```xml
     <!-- REMOVE -->
     <dependency>
       <groupId>com.ibm.db2</groupId>
       <artifactId>jcc</artifactId>
       <version>...</version>
     </dependency>
     ```
   - Commit: "build: add PostgreSQL driver, remove DB2 driver"

7. **Replace OpenJPA with Hibernate** (Task 2.2)
   - Remove OpenJPA dependencies:
     ```xml
     <!-- REMOVE -->
     <dependency>
       <groupId>org.apache.openjpa</groupId>
       <artifactId>openjpa</artifactId>
       <version>...</version>
     </dependency>
     ```
   - Add Hibernate dependencies:
     ```xml
     <!-- ADD -->
     <dependency>
       <groupId>org.hibernate.orm</groupId>
       <artifactId>hibernate-core</artifactId>
       <version>6.4.0.Final</version>
     </dependency>
     <dependency>
       <groupId>org.hibernate.orm</groupId>
       <artifactId>hibernate-entitymanager</artifactId>
       <version>6.0.0.Alpha7</version>
     </dependency>
     ```
   - Commit: "build: replace OpenJPA with Hibernate"

8. **Update persistence.xml for PostgreSQL**
   - Modify `CustomerOrderServices/ejbModule/META-INF/persistence.xml`:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <persistence xmlns="https://jakarta.ee/xml/ns/persistence"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                      https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
                  version="3.1">
       
       <persistence-unit name="CustomerOrderServices" transaction-type="JTA">
         <!-- Remove DB2-specific provider -->
         <!-- <provider>org.apache.openjpa.persistence.PersistenceProviderImpl</provider> -->
         
         <!-- Add Hibernate provider -->
         <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
         
         <!-- Update datasource from JNDI -->
         <jta-data-source>java:comp/env/jdbc/postgresDS</jta-data-source>
         
         <!-- Entity classes -->
         <class>org.pwte.example.domain.Order</class>
         <class>org.pwte.example.domain.Customer</class>
         <class>org.pwte.example.domain.Address</class>
         <class>org.pwte.example.domain.LineItem</class>
         <class>org.pwte.example.domain.Product</class>
         <class>org.pwte.example.domain.Category</class>
         <!-- Add all entity classes -->
         
         <properties>
           <!-- PostgreSQL Dialect -->
           <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
           
           <!-- Schema generation (use 'validate' in production) -->
           <property name="hibernate.hbm2ddl.auto" value="validate"/>
           
           <!-- SQL Logging (disable in production) -->
           <property name="hibernate.show_sql" value="true"/>
           <property name="hibernate.format_sql" value="true"/>
           
           <!-- Connection pool settings -->
           <property name="hibernate.hikari.minimumIdle" value="5"/>
           <property name="hibernate.hikari.maximumPoolSize" value="20"/>
           <property name="hibernate.hikari.idleTimeout" value="30000"/>
         </properties>
       </persistence-unit>
     </persistence>
     ```
   - Commit: "config: update persistence.xml for PostgreSQL and Hibernate"

9. **Convert DB2 SQL Scripts to PostgreSQL**
   - Analyze `Common/createOrderDB.sql`:
     - Identify DB2-specific syntax (e.g., `GENERATED ALWAYS AS IDENTITY`)
     - Identify DB2 data types (e.g., `VARCHAR2`, `NUMBER`, `TIMESTAMP`)
     - Identify DB2 functions (e.g., `SYSDATE`, `NVL`)
   - Create PostgreSQL version `Common/createOrderDB-postgres.sql`:
     ```sql
     -- Example conversions:
     -- DB2: ORDID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY
     -- PostgreSQL: ordid SERIAL PRIMARY KEY
     
     -- DB2: VARCHAR2(100)
     -- PostgreSQL: VARCHAR(100)
     
     -- DB2: NUMBER(10,2)
     -- PostgreSQL: NUMERIC(10,2)
     
     -- DB2: SYSDATE
     -- PostgreSQL: CURRENT_TIMESTAMP
     
     -- DB2: NVL(column, default)
     -- PostgreSQL: COALESCE(column, default)
     ```
   - Convert all SQL scripts:
     - `createOrderDB.sql` → `createOrderDB-postgres.sql`
     - `cleanOrderDB.sql` → `cleanOrderDB-postgres.sql`
     - `addBusinessCustomer.sql` → `addBusinessCustomer-postgres.sql`
     - `addResidentialCustomer.sql` → `addResidentialCustomer-postgres.sql`
     - `InventoryDdl.sql` → `InventoryDdl-postgres.sql`
     - `InventoryData.sql` → `InventoryData-postgres.sql`
   - Use GitHub Copilot with prompts like: "Convert this DB2 SQL to PostgreSQL syntax"
   - Commit: "feat: add PostgreSQL versions of SQL scripts"

10. **Update Entity Annotations for Hibernate**
    - Review JPA entity classes for OpenJPA-specific annotations
    - Update to Hibernate-compatible annotations:
      ```java
      // Example: Sequence generation
      // DB2/OpenJPA style:
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      
      // PostgreSQL/Hibernate style:
      @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
      @SequenceGenerator(name = "order_seq", sequenceName = "order_sequence", allocationSize = 1)
      ```
    - Commit after each entity class: "refactor: update {EntityClass} for Hibernate compatibility"

11. **Update progress tracking after each commit**
    - Update `TASK-005-progress.md` with completed steps
    - Check off items in `TASK-005-todos.md`
    - Document any issues or deviations

### Phase 4: Implementation - Data Migration

12. **Setup PostgreSQL Database (Local/Azure)**
    - If using Azure Database for PostgreSQL:
      ```powershell
      # Create Azure PostgreSQL Flexible Server
      az postgres flexible-server create `
        --name customerorder-db `
        --resource-group rg-customerorder-dev `
        --location eastus `
        --admin-user dbadmin `
        --admin-password <SecurePassword> `
        --sku-name Standard_B2s `
        --tier Burstable `
        --storage-size 32 `
        --version 15
      
      # Configure firewall
      az postgres flexible-server firewall-rule create `
        --resource-group rg-customerorder-dev `
        --name customerorder-db `
        --rule-name AllowLocalDev `
        --start-ip-address <YourIP> `
        --end-ip-address <YourIP>
      ```
    - If using local PostgreSQL:
      ```powershell
      # Using Docker
      docker run --name postgres-dev `
        -e POSTGRES_DB=orderdb `
        -e POSTGRES_USER=dbuser `
        -e POSTGRES_PASSWORD=dbpass `
        -p 5432:5432 `
        -d postgres:15-alpine
      ```
    - Document connection details in progress file

13. **Execute PostgreSQL Schema Scripts**
    - Connect to PostgreSQL database
    - Run schema creation:
      ```powershell
      psql -h <hostname> -U <username> -d <database> -f Common/createOrderDB-postgres.sql
      psql -h <hostname> -U <username> -d <database> -f Common/InventoryDdl-postgres.sql
      ```
    - Verify schema created successfully
    - Document in progress file

14. **Migrate Data from DB2 to PostgreSQL** (Task 2.1)
    - **Option 1: Azure Database Migration Service**
      - Create migration project in Azure Portal
      - Configure source (DB2) and target (PostgreSQL)
      - Run assessment and migration
    - **Option 2: Manual Export/Import**
      - Export data from DB2:
        ```bash
        db2 "EXPORT TO customers.csv OF DEL SELECT * FROM CUSTOMER"
        db2 "EXPORT TO orders.csv OF DEL SELECT * FROM ORDERS"
        # ... export all tables
        ```
      - Import to PostgreSQL:
        ```sql
        COPY customer FROM '/path/to/customers.csv' DELIMITER ',' CSV HEADER;
        COPY orders FROM '/path/to/orders.csv' DELIMITER ',' CSV HEADER;
        -- ... import all tables
        ```
    - **Option 3: Sample Data Only (for dev/test)**
      - Run sample data scripts:
        ```powershell
        psql -h <hostname> -U <username> -d <database> -f Common/addBusinessCustomer-postgres.sql
        psql -h <hostname> -U <username> -d <database> -f Common/addResidentialCustomer-postgres.sql
        psql -h <hostname> -U <username> -d <database> -f Common/InventoryData-postgres.sql
        ```
    - Verify data migrated successfully
    - Document row counts and validation in progress file

15. **Update Datasource Configuration**
    - For development/testing, create `src/main/resources/META-INF/microprofile-config.properties`:
      ```properties
      # PostgreSQL Connection
      quarkus.datasource.db-kind=postgresql
      quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:orderdb}
      quarkus.datasource.username=${DB_USER:dbuser}
      quarkus.datasource.password=${DB_PASSWORD:dbpass}
      quarkus.datasource.jdbc.max-size=20
      quarkus.datasource.jdbc.min-size=5
      ```
    - OR configure JNDI datasource in application server
    - Commit: "config: add PostgreSQL datasource configuration"

### Phase 5: Validation & Testing

16. **Build verification**
    - Run `mvn clean compile` on all modules
    - Document any compilation errors
    - Fix Hibernate-specific errors
    - Commit fixes: "fix: resolve Hibernate compilation errors in {module}"

17. **Run Database Integration Tests**
    - Execute: `mvn clean test`
    - Focus on tests that interact with database (from TASK-002)
    - Document test results in progress file
    - Fix any test failures due to:
      - PostgreSQL SQL dialect differences
      - Hibernate behavior differences from OpenJPA
      - Data type conversions
    - Commit fixes: "fix: update tests for PostgreSQL compatibility"

18. **Manual Database Validation**
    - Connect to PostgreSQL and verify:
      - All tables created
      - All data migrated (row counts match)
      - Foreign key constraints working
      - Sequences working correctly
    - Test CRUD operations through application:
      - Create order (INSERT)
      - Read order (SELECT)
      - Update order (UPDATE)
      - Delete order (DELETE)
    - Document validation results in progress file

19. **Performance Testing (Optional)**
    - Compare query performance DB2 vs PostgreSQL
    - Identify any slow queries
    - Add indexes if needed
    - Document in progress file

20. **Update todos and progress**
    - Mark validation steps complete
    - Document final database status
    - Update progress percentage

### Phase 6: Documentation & Summary

21. **Generate migration summary**
    - Create `.vscode/transformation/TASK-005/summary.md` with:
      - Task completion date/time
      - Database migration approach used
      - Schema conversion summary
      - Data migration statistics (table count, row count)
      - Files modified (count and list)
      - SQL scripts created (PostgreSQL versions)
      - Commits created (count and list with messages)
      - Issues encountered and resolutions
      - Validation results
      - Performance comparison (if tested)
      - Lessons learned
      - Recommendations for next tasks

22. **Generate diff documentation**
    - For EACH modified file, create `.vscode/transformation/TASK-005/diffs/{filename}.diff.md`
    - For SQL scripts, create comparison docs
    - Include:
      - File path
      - Before/after comparison (DB2 vs PostgreSQL)
      - Explanation of database changes
      - Rationale for Hibernate over OpenJPA

23. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 7: Handoff

24. **Create final summary report**
    - Consolidate all findings
    - List all commits made
    - Show database migration statistics
    - Document PostgreSQL connection details
    - Provide branch merge instructions
    - Highlight any blockers for next tasks
    - Present to user for review

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

Task: TASK-005
```

Types: feat, refactor, build, config, fix
Examples:
- `build: add PostgreSQL driver, remove DB2 driver`
- `config: update persistence.xml for PostgreSQL and Hibernate`
- `feat: add PostgreSQL versions of SQL scripts`
- `refactor: update Order entity for Hibernate compatibility`

### Error Handling
- Document ALL database errors
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip database validation

### File Organization
```
.vscode/transformation/TASK-005/
├── plan.md
├── progress.md
├── todos.md
├── summary.md
└── diffs/
    ├── persistence.xml.diff.md
    ├── createOrderDB-postgres.sql.diff.md
    └── ... (one file per changed file)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-005-database-migration`
2. ✅ PostgreSQL driver added, DB2 driver removed
3. ✅ Hibernate replaces OpenJPA
4. ✅ persistence.xml updated for PostgreSQL
5. ✅ All SQL scripts converted to PostgreSQL syntax
6. ✅ PostgreSQL database created and schema deployed
7. ✅ Data migrated from DB2 to PostgreSQL
8. ✅ All entity classes compatible with Hibernate
9. ✅ All modules compile successfully
10. ✅ All tests passing with PostgreSQL
11. ✅ Database validated (schema, data, CRUD operations)
12. ✅ 10-20 commits with proper messages
13. ✅ Task plan, progress, todos, summary, and diff documentation
14. ✅ Final handoff report for user review

## Success Criteria

- [ ] PostgreSQL driver configured in all modules
- [ ] Hibernate replaces OpenJPA
- [ ] persistence.xml uses PostgreSQLDialect
- [ ] All SQL scripts converted to PostgreSQL syntax
- [ ] PostgreSQL database running (local or Azure)
- [ ] Schema created successfully in PostgreSQL
- [ ] Data migrated (all tables, correct row counts)
- [ ] CRUD operations working through application
- [ ] `mvn clean compile` succeeds for all modules
- [ ] `mvn clean test` passes all database tests
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document includes migration statistics
- [ ] Diff files exist for every changed file
- [ ] No unresolved database errors
- [ ] User has clear instructions for next steps

## Constraints & Guidelines

- **DO NOT** change business logic or functionality
- **DO NOT** lose data during migration (always backup first)
- **DO** test thoroughly before declaring migration complete
- **DO** document all PostgreSQL connection details securely
- **DO** commit frequently (every logical change)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation

## GitHub Copilot Usage Tips

For **SQL conversion**, use prompts like:
- "Convert this DB2 SQL script to PostgreSQL syntax"
- "Replace DB2 IDENTITY column with PostgreSQL SERIAL"
- "Convert DB2 functions (SYSDATE, NVL) to PostgreSQL equivalents"

For **Hibernate migration**, use prompts like:
- "Update this JPA entity for Hibernate with PostgreSQL sequences"
- "Configure Hibernate connection pool in persistence.xml"
- "Fix this OpenJPA-specific annotation for Hibernate"

## Start Command

Begin by:
1. Reading all transformation documentation files
2. Analyzing the current database schema and SQL scripts
3. Creating the detailed plan with schema conversion strategy
4. Presenting the plan to me for approval
5. Waiting for my "continue" command before proceeding

**Remember:** You must complete this task fully before moving to the next task. Take your time, be thorough, document everything, and ask questions if anything is unclear. Database migrations are critical and require careful validation.

---

**Ready to begin? Start with step 1: Read transformation documentation and create the execution plan.**
