# Migration Task Prompt: Test Foundation - Unit & Integration Tests

## Mission
You are an AI agent tasked with performing **TASK-002** for the Customer Order Services application: **Build Comprehensive Test Foundation**. This task creates a safety net for all future migration work by establishing automated testing with ≥70% code coverage.

## Task Overview
**Task ID:** TASK-002  
**Task Name:** Testing Foundation (Unit + Integration Tests)  
**Priority:** 🔴 CRITICAL  
**Dependencies:** TASK-001 (Java 17 Upgrade) must be completed  
**Estimated Effort:** 120 hours manual / 52 hours AI-assisted  
**Success Criteria:** ≥70% test coverage, all tests passing, JUnit 5 + Mockito configured

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 1, Tasks 1.1-1.4
   - Review `.transformation/FRAMEWORK_MIGRATION_GUIDE.md` Section 6
   - Analyze existing codebase for testable units

2. **Create a detailed execution plan**
   - Inventory all domain model classes (8 classes in `org.pwte.example.domain`)
   - Inventory all service classes (2 classes: `CustomerOrderServices`, others)
   - Inventory all REST endpoints (JAX-RS resources)
   - Identify testing frameworks needed (JUnit 5, Mockito, REST Assured, AssertJ)
   - Document test coverage targets per module
   - Save plan to `.vscode/transformation/TASK-002/plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-002-test-foundation`
   - Branch from: `migration/task-001-java-17-upgrade` (completed branch)
   - Commit message: "chore: create branch for test foundation task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-002/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-002/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation

6. **Configure JUnit 5 + Testing Dependencies** (Task 1.4)
   - Update parent `pom.xml` with test dependency management:
     ```xml
     <dependencyManagement>
       <dependencies>
         <dependency>
           <groupId>org.junit</groupId>
           <artifactId>junit-bom</artifactId>
           <version>5.10.1</version>
           <type>pom</type>
           <scope>import</scope>
         </dependency>
       </dependencies>
     </dependencyManagement>
     ```
   - Add to each module's `pom.xml`:
     ```xml
     <dependencies>
       <!-- JUnit 5 -->
       <dependency>
         <groupId>org.junit.jupiter</groupId>
         <artifactId>junit-jupiter</artifactId>
         <scope>test</scope>
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
       
       <!-- For integration tests -->
       <dependency>
         <groupId>io.rest-assured</groupId>
         <artifactId>rest-assured</artifactId>
         <version>5.4.0</version>
         <scope>test</scope>
       </dependency>
     </dependencies>
     ```
   - Configure Surefire plugin for JUnit 5:
     ```xml
     <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-surefire-plugin</artifactId>
       <version>3.2.3</version>
     </plugin>
     ```
   - Commit: "build: add JUnit 5, Mockito, AssertJ, REST Assured dependencies"

7. **Generate Unit Tests for Domain Model** (Task 1.1)
   - Target classes in `CustomerOrderServices/ejbModule/org/pwte/example/domain/`:
     - `Customer.java`
     - `Address.java`
     - `Order.java`
     - `LineItem.java`
     - `Product.java`
     - `Category.java`
     - `AbstractCustomer.java`
     - Other entity classes
   - For EACH domain class, use GitHub Copilot to generate:
     - Constructor tests
     - Getter/setter tests
     - Equals/hashCode tests
     - Validation tests (if applicable)
     - JPA relationship tests
   - Create test files in `CustomerOrderServices/src/test/java/org/pwte/example/domain/`
   - Commit after each test class: "test: add unit tests for {ClassName}"
   - **Estimated:** 40 hours manual / 16 hours AI-assisted

8. **Generate Unit Tests for Service Layer** (Task 1.2)
   - Target service classes:
     - `CustomerOrderServicesImpl.java`
     - Other business logic classes
   - For EACH service class, generate:
     - Happy path tests
     - Error handling tests
     - Mock EntityManager interactions
     - Transaction boundary tests
     - Security annotation tests (@RolesAllowed)
   - Use Mockito to mock dependencies
   - Create test files in `CustomerOrderServices/src/test/java/org/pwte/example/`
   - Commit after each test class: "test: add service tests for {ServiceName}"
   - **Estimated:** 40 hours manual / 16 hours AI-assisted

9. **Generate Integration Tests for REST Endpoints** (Task 1.3)
   - Target JAX-RS resource classes in `CustomerOrderServicesWeb/src/`
   - For EACH REST endpoint, generate:
     - HTTP GET tests (200, 404 responses)
     - HTTP POST tests (201, 400 responses)
     - HTTP PUT tests (200, 404, 400 responses)
     - HTTP DELETE tests (204, 404 responses)
     - JSON serialization/deserialization tests
     - Authentication/authorization tests
   - Use REST Assured for HTTP testing
   - Create test files in `CustomerOrderServicesWeb/src/test/java/org/pwte/example/`
   - Commit after each test class: "test: add integration tests for {ResourceName}"
   - **Estimated:** 40 hours manual / 20 hours AI-assisted

10. **Configure JaCoCo Code Coverage**
    - Add to parent `pom.xml`:
      ```xml
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
    - Commit: "build: add JaCoCo code coverage with 70% threshold"

11. **Update progress tracking after each commit**
    - Update `.vscode/transformation/TASK-002/progress.md` with completed steps
    - Check off items in `.vscode/transformation/TASK-002/todos.md`
    - Document any issues or deviations

### Phase 4: Validation & Testing

12. **Run all tests and verify coverage**
    - Execute: `mvn clean test`
    - Generate coverage report: `mvn jacoco:report`
    - Verify coverage ≥70% in `target/site/jacoco/index.html`
    - Document results in progress file
    - If coverage <70%, identify gaps and add more tests
    - Commit fixes: "test: add additional tests to reach 70% coverage"

13. **Fix any test failures**
    - Run tests module by module
    - Document any failures in progress file
    - Use GitHub Copilot to assist with fixing failing tests
    - Ensure all tests pass before proceeding
    - Commit fixes: "fix: resolve test failures in {module}"

14. **Update todos and progress**
    - Mark validation steps complete
    - Document final coverage percentage
    - Update progress to 100%

### Phase 5: Documentation & Summary

15. **Generate migration summary**
    - Create `.vscode/transformation/TASK-002/summary.md` with:
      - Task completion date/time
      - Total test count (unit + integration)
      - Final code coverage percentage
      - Files created (count and list)
      - Commits created (count and list with messages)
      - Issues encountered and resolutions
      - Validation results
      - Lessons learned
      - Recommendations for next tasks

16. **Generate diff documentation**
    - For EACH new test file created, create `.vscode/transformation/TASK-002/diffs/{filename}.diff.md`
    - For modified POM files, document changes
    - Include:
      - File path
      - New code added
      - Explanation of test coverage
      - Rationale for test approach

17. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 6: Handoff

18. **Create final summary report**
    - Consolidate all findings
    - List all commits made
    - Show code coverage metrics
    - Provide branch merge instructions
    - Highlight any blockers for next tasks
    - Present to user for review

## Work Execution Guidelines

### Branch Management
- Create feature branch at start
- Regular commits (after each test class or logical group)
- Descriptive commit messages following conventional commits
- NO merge to main until user approval

### Progress Tracking
- Update progress file after EVERY test class or module completion
- Use percentage completion (0-100%)
- Include timestamps for all updates
- Document blockers immediately

### Todo Management
- Create comprehensive todo list at start (one item per test class)
- Check items off as completed: `- [x] Completed item`
- Add new items if discovered: `- [ ] New item (discovered during work)`
- Never delete items (preserve history)

### Commit Message Format
```
<type>(<scope>): <subject>

<body (optional)>

Task: TASK-002
```

Types: test, build, fix, refactor
Examples: 
- `test: add unit tests for Customer domain model`
- `build: add JUnit 5 and Mockito dependencies`

### Error Handling
- Document ALL test failures
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip failing tests

### File Organization
```
.vscode/transformation/
└── TASK-002/
    ├── plan.md
    ├── progress.md
    ├── todos.md
    ├── summary.md
    ├── handoff.md
    └── diffs/
        ├── CustomerTest.java.diff.md
        ├── OrderTest.java.diff.md
        └── ... (one file per test created)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-002-test-foundation`
2. ✅ JUnit 5 + Mockito + AssertJ configured in all modules
3. ✅ Unit tests for all 8+ domain model classes
4. ✅ Unit tests for 2+ service classes
5. ✅ Integration tests for all REST endpoints
6. ✅ JaCoCo configured with 70% coverage threshold
7. ✅ Code coverage ≥70% achieved
8. ✅ All tests passing (mvn clean test succeeds)
9. ✅ 15-25 commits with proper messages
10. ✅ Task plan, progress, todos, summary, and diff documentation
11. ✅ Final handoff report for user review

## Success Criteria

- [ ] JUnit 5 + Mockito + AssertJ dependencies added to all modules
- [ ] Unit tests created for all domain model classes
- [ ] Unit tests created for all service classes
- [ ] Integration tests created for all REST endpoints
- [ ] JaCoCo code coverage ≥70%
- [ ] `mvn clean test` succeeds for all modules
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document is complete and accurate
- [ ] Diff files exist for every created test file
- [ ] No skipped or ignored tests without documented reason
- [ ] User has clear instructions for next steps

## Constraints & Guidelines

- **DO NOT** modify production code unless fixing bugs discovered by tests
- **DO NOT** skip tests that are difficult to write
- **DO** use GitHub Copilot extensively for test generation
- **DO** commit frequently (after each test class or small group)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation
- **DO** aim for meaningful tests, not just coverage numbers

## GitHub Copilot Usage Tips

For **generating unit tests**, use prompts like:
- "Generate JUnit 5 tests for this Customer class with AssertJ assertions"
- "Create Mockito-based tests for this service class, mocking EntityManager"
- "Write comprehensive tests for equals() and hashCode() methods"

For **generating integration tests**, use prompts like:
- "Generate REST Assured tests for this JAX-RS resource class"
- "Create integration tests for POST /orders endpoint with 201 and 400 cases"
- "Write tests for authentication on this protected endpoint"

## Start Command

Begin by:
1. Reading all transformation documentation files
2. Analyzing the current codebase structure
3. Identifying all classes that need tests
4. Creating the detailed plan with test coverage strategy
5. Presenting the plan to me for approval
6. Waiting for my "continue" command before proceeding

**Remember:** You must complete this task fully before moving to the next task. Take your time, be thorough, document everything, and ask questions if anything is unclear. Test quality is more important than test quantity.

---

**Ready to begin? Start with step 1: Read transformation documentation and create the execution plan.**
