# Migration Task Prompt: Jakarta EE Namespace Migration

## Mission
You are an AI agent tasked with performing **TASK-003** for the Customer Order Services application: **Migrate from javax.* to jakarta.* namespace**. This is a critical step in modernizing from Java EE 7 to Jakarta EE 10.

## Task Overview
**Task ID:** TASK-003  
**Task Name:** Jakarta EE Namespace Migration (javax → jakarta)  
**Priority:** 🔴 CRITICAL  
**Dependencies:** 
- TASK-001 (Java 17 Upgrade) completed
- TASK-002 (Test Foundation) completed
**Estimated Effort:** 60 hours manual / 25 hours AI-assisted  
**Success Criteria:** All javax.* imports replaced with jakarta.*, all modules compile, all tests pass

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/FRAMEWORK_MIGRATION_GUIDE.md` Section 3 (Jakarta EE Migration)
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 1, Task 1.6
   - Understand javax → jakarta namespace changes

2. **Create a detailed execution plan**
   - Scan codebase for all javax.* package usage
   - Identify affected modules and files
   - List all javax.* packages used (persistence, servlet, ejb, ws.rs, etc.)
   - Identify XML configuration files that need updates
   - Document OpenRewrite recipe to use
   - Save plan to `.vscode/transformation/TASK-003/plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-003-jakarta-migration`
   - Branch from: `migration/task-002-test-foundation` (completed branch)
   - Commit message: "chore: create branch for Jakarta EE migration task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-003/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-003/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation

6. **Configure OpenRewrite Maven Plugin**
   - Add to parent `pom.xml`:
     ```xml
     <build>
       <plugins>
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
       </plugins>
     </build>
     ```
   - Commit: "build: add OpenRewrite plugin for Jakarta migration"

7. **Update Maven Dependencies (Jakarta EE 10)**
   - Replace in parent `pom.xml`:
     ```xml
     <!-- BEFORE -->
     <dependency>
       <groupId>javax</groupId>
       <artifactId>javaee-api</artifactId>
       <version>7.0</version>
       <scope>provided</scope>
     </dependency>
     
     <!-- AFTER -->
     <dependency>
       <groupId>jakarta.platform</groupId>
       <artifactId>jakarta.jakartaee-api</artifactId>
       <version>10.0.0</version>
       <scope>provided</scope>
     </dependency>
     ```
   - Update specific Jakarta EE dependencies:
     ```xml
     <!-- Persistence API -->
     <dependency>
       <groupId>jakarta.persistence</groupId>
       <artifactId>jakarta.persistence-api</artifactId>
       <version>3.1.0</version>
     </dependency>
     
     <!-- Servlet API -->
     <dependency>
       <groupId>jakarta.servlet</groupId>
       <artifactId>jakarta.servlet-api</artifactId>
       <version>6.0.0</version>
     </dependency>
     
     <!-- JAX-RS API -->
     <dependency>
       <groupId>jakarta.ws.rs</groupId>
       <artifactId>jakarta.ws.rs-api</artifactId>
       <version>3.1.0</version>
     </dependency>
     
     <!-- EJB API -->
     <dependency>
       <groupId>jakarta.ejb</groupId>
       <artifactId>jakarta.ejb-api</artifactId>
       <version>4.0.1</version>
     </dependency>
     
     <!-- JSON API -->
     <dependency>
       <groupId>jakarta.json</groupId>
       <artifactId>jakarta.json-api</artifactId>
       <version>2.1.2</version>
     </dependency>
     
     <!-- Validation API -->
     <dependency>
       <groupId>jakarta.validation</groupId>
       <artifactId>jakarta.validation-api</artifactId>
       <version>3.0.2</version>
     </dependency>
     ```
   - Commit: "build: upgrade to Jakarta EE 10 dependencies"

8. **Run OpenRewrite Migration**
   - Execute from project root:
     ```powershell
     mvn rewrite:run
     ```
   - This will automatically:
     - Replace all `javax.persistence.*` → `jakarta.persistence.*`
     - Replace all `javax.servlet.*` → `jakarta.servlet.*`
     - Replace all `javax.ws.rs.*` → `jakarta.ws.rs.*`
     - Replace all `javax.ejb.*` → `jakarta.ejb.*`
     - Replace all `javax.inject.*` → `jakarta.inject.*`
     - Replace all `javax.annotation.*` → `jakarta.annotation.*`
     - Update other javax.* packages
   - Commit: "refactor: migrate javax.* to jakarta.* namespace (OpenRewrite)"
   - **Note:** If OpenRewrite doesn't work, manually update imports using find/replace

9. **Update XML Configuration Files**
   - Update `persistence.xml` files:
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
   - Update `web.xml` files:
     ```xml
     <!-- BEFORE -->
     <web-app xmlns="http://java.sun.com/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                                  http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
              version="3.0">
     
     <!-- AFTER -->
     <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                                  https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
              version="6.0">
     ```
   - Update any other XML descriptors (ejb-jar.xml, application.xml, etc.)
   - Commit: "refactor: update XML descriptors to Jakarta EE 10 schemas"

10. **Manual Import Fixes (if needed)**
    - Search for any remaining `javax.*` imports:
      ```powershell
      rg "import javax\." --type java
      ```
    - Manually replace any missed imports
    - Use multi-file find/replace or GitHub Copilot
    - Common mappings:
      - `javax.persistence.*` → `jakarta.persistence.*`
      - `javax.ejb.*` → `jakarta.ejb.*`
      - `javax.ws.rs.*` → `jakarta.ws.rs.*`
      - `javax.servlet.*` → `jakarta.servlet.*`
      - `javax.inject.*` → `jakarta.inject.*`
      - `javax.annotation.*` → `jakarta.annotation.*`
    - Commit: "fix: manually update remaining javax imports"

11. **Update progress tracking after each commit**
    - Update `TASK-003-progress.md` with completed steps
    - Check off items in `TASK-003-todos.md`
    - Document any issues or deviations

### Phase 4: Validation & Testing

12. **Build verification**
    - Run `mvn clean compile` on each module
    - Document any compilation errors
    - Fix errors iteratively (missing imports, API changes)
    - Commit fixes: "fix: resolve Jakarta EE compilation errors in {module}"

13. **Run all tests**
    - Execute: `mvn clean test`
    - Verify all tests still pass (from TASK-002)
    - Fix any test failures due to namespace changes
    - Update test code if Jakarta EE 10 API changed
    - Commit fixes: "fix: update tests for Jakarta EE 10 API changes"

14. **Verify no remaining javax.* references**
    - Search entire codebase:
      ```powershell
      rg "javax\." --type java
      rg "javax\." --type xml
      ```
    - Document any intentional javax.* usage (e.g., javax.crypto, javax.net - these stay)
    - Ensure all Java EE packages are migrated

15. **Update todos and progress**
    - Mark validation steps complete
    - Document any remaining issues
    - Update progress percentage

### Phase 5: Documentation & Summary

16. **Generate migration summary**
    - Create `.vscode/transformation/TASK-003/summary.md` with:
      - Task completion date/time
      - Files modified (count and list)
      - javax.* packages migrated (list)
      - jakarta.* packages adopted (list)
      - Commits created (count and list with messages)
      - Issues encountered and resolutions
      - Validation results
      - Lessons learned
      - Recommendations for next tasks

17. **Generate diff documentation**
    - For EACH modified file, create `.vscode/transformation/TASK-003/diffs/{filename}.diff.md`
    - Include:
      - File path
      - Before/after import comparison
      - Explanation of namespace changes
      - Rationale for Jakarta EE 10 upgrade

18. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 6: Handoff

19. **Create final summary report**
    - Consolidate all findings
    - List all commits made
    - Show before/after package usage
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

Task: TASK-003
```

Types: refactor, build, fix
Examples:
- `refactor: migrate javax.* to jakarta.* namespace (OpenRewrite)`
- `build: upgrade to Jakarta EE 10 dependencies`
- `fix: resolve Jakarta EE compilation errors in CustomerOrderServices`

### Error Handling
- Document ALL compilation errors
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip errors silently

### File Organization
```
.vscode/transformation/TASK-003/
├── plan.md
├── progress.md
├── todos.md
├── summary.md
└── diffs/
    ├── CustomerOrderServicesImpl.java.diff.md
    ├── persistence.xml.diff.md
    └── ... (one file per changed file)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-003-jakarta-migration`
2. ✅ OpenRewrite configured and executed
3. ✅ All javax.* imports replaced with jakarta.*
4. ✅ Jakarta EE 10 dependencies updated in POMs
5. ✅ XML descriptors updated to Jakarta EE 10 schemas
6. ✅ All modules compile successfully
7. ✅ All tests passing
8. ✅ 5-8 commits with proper messages
9. ✅ Task plan, progress, todos, summary, and diff documentation
10. ✅ Final handoff report for user review

## Success Criteria

- [ ] All Java EE packages migrated from javax.* to jakarta.*
- [ ] Jakarta EE 10 dependencies in all modules
- [ ] All XML descriptors use Jakarta EE 10 schemas
- [ ] No remaining javax.* references (except JDK packages)
- [ ] `mvn clean compile` succeeds for all modules
- [ ] `mvn clean test` passes all tests
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document is complete and accurate
- [ ] Diff files exist for every changed file
- [ ] No unresolved errors or warnings
- [ ] User has clear instructions for next steps

## Constraints & Guidelines

- **DO NOT** change business logic or functionality
- **DO NOT** upgrade to Spring Boot yet (that's a separate task)
- **DO** use OpenRewrite for automated migration
- **DO** commit frequently (every logical change)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation

## OpenRewrite Usage

The OpenRewrite recipe `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` automatically handles:
- ✅ Package/import renaming
- ✅ Annotation updates
- ✅ Some API changes

If OpenRewrite fails or is incomplete:
- Use IDE refactoring tools (Find/Replace)
- Use GitHub Copilot with prompts like: "Update this file from javax.* to jakarta.* namespace"

## Start Command

Begin by:
1. Reading all transformation documentation files
2. Analyzing the current codebase for javax.* usage
3. Creating the detailed plan with affected files list
4. Presenting the plan to me for approval
5. Waiting for my "continue" command before proceeding

**Remember:** You must complete this task fully before moving to the next task. Take your time, be thorough, document everything, and ask questions if anything is unclear.

---

**Ready to begin? Start with step 1: Read transformation documentation and create the execution plan.**
