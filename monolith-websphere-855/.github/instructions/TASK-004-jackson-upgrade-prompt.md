# Migration Task Prompt: Jackson Upgrade & IBM JSON Replacement

## Mission
You are an AI agent tasked with performing **TASK-004** for the Customer Order Services application: **Upgrade Jackson 1.7.1 → 2.17.0 and Replace IBM JSON Libraries**. This task eliminates critical CVEs and removes WebSphere proprietary dependencies.

## Task Overview
**Task ID:** TASK-004  
**Task Name:** Jackson Upgrade & IBM JSON Replacement  
**Priority:** 🔴 CRITICAL (Security - 5 CVEs)  
**Dependencies:** 
- TASK-001 (Java 17 Upgrade) completed
- TASK-002 (Test Foundation) completed
- TASK-003 (Jakarta EE Migration) completed
**Estimated Effort:** 90 hours manual / 40 hours AI-assisted  
**Success Criteria:** Zero critical CVEs, all IBM JSON code replaced, all modules compile, all tests pass

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review `.transformation/FRAMEWORK_MIGRATION_GUIDE.md` Section 5 (Jackson Upgrade)
   - Review `.transformation/ASSESSMENT_SUMMARY.md` Phase 1, Tasks 1.7-1.8
   - Review `.transformation/MIGRATION_ASSESSMENT.md` Section 2.2 (Critical Vulnerabilities)

2. **Create a detailed execution plan**
   - Scan codebase for Jackson 1.x usage: `rg "org\.codehaus\.jackson" --type java`
   - Scan codebase for IBM JSON usage: `rg "com\.ibm\.json\.java" --type java`
   - Identify all affected files and code patterns
   - List CVEs being resolved (CVE-2019-14540, CVE-2020-36518, etc.)
   - Document migration strategies (ObjectMapper, JsonNode, etc.)
   - Save plan to `.vscode/transformation/TASK-004/plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-004-jackson-upgrade`
   - Branch from: current branch (agent-test or your working branch)
   - Commit message: "chore: create branch for Jackson upgrade task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-004/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-004/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation - Jackson Upgrade

6. **Update Jackson Dependencies** (Task 1.7)
   - Remove OLD Jackson 1.x from `pom.xml`:
     ```xml
     <!-- REMOVE -->
     <dependency>
       <groupId>org.codehaus.jackson</groupId>
       <artifactId>jackson-mapper-asl</artifactId>
       <version>1.7.1</version>
     </dependency>
     <dependency>
       <groupId>org.codehaus.jackson</groupId>
       <artifactId>jackson-core-asl</artifactId>
       <version>1.7.1</version>
     </dependency>
     ```
   - Add NEW Jackson 2.17.0:
     ```xml
     <!-- ADD -->
     <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.17.0</version>
     </dependency>
     <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-core</artifactId>
       <version>2.17.0</version>
     </dependency>
     <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-annotations</artifactId>
       <version>2.17.0</version>
     </dependency>
     <!-- For JAX-RS JSON support -->
     <dependency>
       <groupId>com.fasterxml.jackson.jaxrs</groupId>
       <artifactId>jackson-jaxrs-json-provider</artifactId>
       <version>2.17.0</version>
     </dependency>
     <!-- For Java 8 date/time support -->
     <dependency>
       <groupId>com.fasterxml.jackson.datatype</groupId>
       <artifactId>jackson-datatype-jsr310</artifactId>
       <version>2.17.0</version>
     </dependency>
     ```
   - Commit: "build: upgrade Jackson from 1.7.1 to 2.17.0"

7. **Update Jackson Imports**
   - Search for old imports:
     ```powershell
     rg "org\.codehaus\.jackson" --type java
     ```
   - Replace with new Jackson 2.x imports:
     ```java
     // BEFORE (Jackson 1.x)
     import org.codehaus.jackson.map.ObjectMapper;
     import org.codehaus.jackson.JsonNode;
     import org.codehaus.jackson.annotate.JsonProperty;
     
     // AFTER (Jackson 2.x)
     import com.fasterxml.jackson.databind.ObjectMapper;
     import com.fasterxml.jackson.databind.JsonNode;
     import com.fasterxml.jackson.annotation.JsonProperty;
     ```
   - Use find/replace or GitHub Copilot to update all files
   - Commit: "refactor: update Jackson imports to 2.17.0 package"

8. **Update Jackson API Usage**
   - Update ObjectMapper usage patterns:
     ```java
     // BEFORE (Jackson 1.x)
     ObjectMapper mapper = new ObjectMapper();
     String json = mapper.writeValueAsString(object);
     MyObject obj = mapper.readValue(json, MyObject.class);
     
     // AFTER (Jackson 2.x) - mostly compatible, but configure for Java 8
     ObjectMapper mapper = new ObjectMapper();
     mapper.registerModule(new JavaTimeModule());
     mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
     String json = mapper.writeValueAsString(object);
     MyObject obj = mapper.readValue(json, MyObject.class);
     ```
   - Update JsonNode API usage (mostly compatible):
     ```java
     // BEFORE (Jackson 1.x)
     JsonNode node = mapper.readTree(json);
     String value = node.get("field").getTextValue(); // deprecated
     
     // AFTER (Jackson 2.x)
     JsonNode node = mapper.readTree(json);
     String value = node.get("field").asText(); // new method
     ```
   - Commit after each file or logical group: "refactor: update Jackson API usage in {ClassName}"

### Phase 4: Implementation - IBM JSON Replacement

9. **Remove IBM JSON Dependencies** (Task 1.8)
   - Remove from `pom.xml`:
     ```xml
     <!-- REMOVE -->
     <dependency>
       <groupId>com.ibm.websphere</groupId>
       <artifactId>com.ibm.websphere.appserver.api.json</artifactId>
       <version>1.0</version>
     </dependency>
     ```
   - Commit: "build: remove IBM WebSphere JSON dependencies"

10. **Replace IBM JSONObject with Jackson**
    - Search for IBM JSON usage:
      ```powershell
      rg "com\.ibm\.json\.java" --type java
      ```
    - Replace IBM JSONObject patterns:
      ```java
      // BEFORE (IBM JSON)
      import com.ibm.json.java.JSONObject;
      import com.ibm.json.java.JSONArray;
      
      JSONObject jo = new JSONObject();
      jo.put("orderId", order.getOrderId());
      jo.put("customer", order.getCustomer().getName());
      JSONArray items = new JSONArray();
      items.add(item1);
      items.add(item2);
      jo.put("items", items);
      return jo.toString();
      
      // AFTER (Jackson 2.x - Option 1: ObjectNode)
      import com.fasterxml.jackson.databind.ObjectMapper;
      import com.fasterxml.jackson.databind.node.ObjectNode;
      import com.fasterxml.jackson.databind.node.ArrayNode;
      
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode jo = mapper.createObjectNode();
      jo.put("orderId", order.getOrderId());
      jo.put("customer", order.getCustomer().getName());
      ArrayNode items = mapper.createArrayNode();
      items.add(item1);
      items.add(item2);
      jo.set("items", items);
      return mapper.writeValueAsString(jo);
      
      // AFTER (Jackson 2.x - Option 2: POJO - PREFERRED)
      @Data
      public class OrderDTO {
          private Long orderId;
          private String customer;
          private List<String> items;
      }
      
      OrderDTO dto = new OrderDTO();
      dto.setOrderId(order.getOrderId());
      dto.setCustomer(order.getCustomer().getName());
      dto.setItems(Arrays.asList(item1, item2));
      return mapper.writeValueAsString(dto);
      ```
    - **PREFER POJO approach** for type safety and maintainability
    - Commit after each file: "refactor: replace IBM JSON with Jackson in {ClassName}"

11. **Create DTO Classes (if using POJO approach)**
    - For each IBM JSONObject usage, consider creating a DTO:
      ```java
      package org.pwte.example.dto;
      
      import com.fasterxml.jackson.annotation.JsonProperty;
      import lombok.Data;
      
      @Data
      public class OrderDTO {
          @JsonProperty("order_id")
          private Long orderId;
          
          @JsonProperty("customer_name")
          private String customerName;
          
          private String status;
          private List<LineItemDTO> items;
      }
      ```
    - Place DTOs in new package: `org.pwte.example.dto`
    - Use GitHub Copilot to generate DTOs from JSON patterns
    - Commit: "feat: add DTO classes for Jackson serialization"

12. **Update JAX-RS Configuration for Jackson**
    - Register Jackson provider in JAX-RS application:
      ```java
      import javax.ws.rs.ApplicationPath;
      import javax.ws.rs.core.Application;
      import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
      
      @ApplicationPath("/api")
      public class RestApplication extends Application {
          @Override
          public Set<Class<?>> getClasses() {
              Set<Class<?>> classes = new HashSet<>();
              classes.add(JacksonJsonProvider.class); // Register Jackson
              // ... add resource classes
              return classes;
          }
      }
      ```
    - Or configure in `web.xml` if using servlet-based JAX-RS
    - Commit: "config: register Jackson JSON provider for JAX-RS"

13. **Update progress tracking after each commit**
    - Update `TASK-004-progress.md` with completed steps
    - Check off items in `TASK-004-todos.md`
    - Document any issues or deviations

### Phase 5: Validation & Testing

14. **Build verification**
    - Run `mvn clean compile` on each module
    - Document any compilation errors
    - Fix errors iteratively (API changes, missing imports)
    - Commit fixes: "fix: resolve Jackson compilation errors in {module}"

15. **Run all tests**
    - Execute: `mvn clean test`
    - Verify all tests still pass (from TASK-002)
    - Fix any test failures due to JSON serialization changes
    - Update test assertions if JSON format changed
    - Commit fixes: "fix: update tests for Jackson 2.x JSON format"

16. **Validate CVE Resolution**
    - Run dependency vulnerability scan:
      ```powershell
      mvn dependency-check:check
      # OR
      mvn org.owasp:dependency-check-maven:check
      ```
    - Verify ZERO critical/high CVEs for Jackson
    - Document resolved CVEs in progress file:
      - CVE-2019-14540 (CVSS 9.8) - RESOLVED
      - CVE-2020-36518 (CVSS 7.5) - RESOLVED
      - Others...

17. **Manual Testing (JSON Endpoints)**
    - Test REST endpoints that return JSON
    - Verify JSON format is correct
    - Use Postman/curl to test:
      ```powershell
      # Example
      Invoke-RestMethod -Uri "http://localhost:9080/CustomerOrderServicesWeb/api/orders/1" -Method GET
      ```
    - Document any JSON format changes in progress file

18. **Update todos and progress**
    - Mark validation steps complete
    - Document final CVE status
    - Update progress percentage

### Phase 6: Documentation & Summary

19. **Generate migration summary**
    - Create `.vscode/transformation/TASK-004/summary.md` with:
      - Task completion date/time
      - Files modified (count and list)
      - CVEs resolved (list with CVSS scores)
      - IBM JSON files replaced (count)
      - Jackson usage patterns adopted
      - Commits created (count and list with messages)
      - Issues encountered and resolutions
      - Validation results
      - Lessons learned
      - Recommendations for next tasks

20. **Generate diff documentation**
    - For EACH modified file, create `.vscode/transformation/TASK-004/diffs/{filename}.diff.md`
    - Include:
      - File path
      - Before/after code comparison
      - Explanation of changes (IBM JSON → Jackson, Jackson 1.x → 2.x)
      - Rationale for approach chosen (ObjectNode vs POJO)

21. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 7: Handoff

22. **Create final summary report**
    - Consolidate all findings
    - List all commits made
    - Show CVE resolution report
    - Provide branch merge instructions
    - Highlight any blockers for next tasks
    - Present to user for review

## Work Execution Guidelines

### Branch Management
- Create feature branch at start
- Regular commits (after each file or logical group)
- Descriptive commit messages following conventional commits
- NO merge to main until user approval

### Progress Tracking
- Update progress file after EVERY file or module completion
- Use percentage completion (0-100%)
- Include timestamps for all updates
- Document blockers immediately

### Todo Management
- Create comprehensive todo list at start (one item per file or pattern)
- Check items off as completed: `- [x] Completed item`
- Add new items if discovered: `- [ ] New item (discovered during work)`
- Never delete items (preserve history)

### Commit Message Format
```
<type>(<scope>): <subject>

<body (optional)>

Task: TASK-004
CVEs-Resolved: CVE-2019-14540, CVE-2020-36518 (if applicable)
```

Types: refactor, build, fix, feat
Examples:
- `build: upgrade Jackson from 1.7.1 to 2.17.0`
- `refactor: replace IBM JSON with Jackson in OrderResource`
- `feat: add OrderDTO class for Jackson serialization`
- `fix: resolve Jackson compilation errors in CustomerOrderServicesWeb`

### Error Handling
- Document ALL compilation/test failures
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip errors silently

### File Organization
```
.vscode/transformation/TASK-004/
├── plan.md
├── progress.md
├── todos.md
├── summary.md
└── diffs/
    └── TASK-004/
        ├── OrderResource.java.diff.md
        ├── pom.xml.diff.md
        └── ... (one file per changed file)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-004-jackson-upgrade`
2. ✅ Jackson upgraded from 1.7.1 to 2.17.0
3. ✅ All Jackson 1.x code migrated to 2.x APIs
4. ✅ All IBM JSON code replaced with Jackson
5. ✅ DTO classes created (if POJO approach used)
6. ✅ Jackson JAX-RS provider configured
7. ✅ All modules compile successfully
8. ✅ All tests passing
9. ✅ Zero critical/high CVEs for Jackson
10. ✅ 10-15 commits with proper messages
11. ✅ Task plan, progress, todos, summary, and diff documentation
12. ✅ CVE resolution report
13. ✅ Final handoff report for user review

## Success Criteria

- [ ] Jackson 2.17.0 dependencies in all modules
- [ ] Zero usage of Jackson 1.x (org.codehaus.jackson)
- [ ] Zero usage of IBM JSON (com.ibm.json.java)
- [ ] CVE-2019-14540 resolved (verified)
- [ ] CVE-2020-36518 resolved (verified)
- [ ] All other Jackson CVEs resolved
- [ ] `mvn clean compile` succeeds for all modules
- [ ] `mvn clean test` passes all tests
- [ ] JSON endpoints return valid JSON
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document is complete with CVE report
- [ ] Diff files exist for every changed file
- [ ] No unresolved errors or warnings
- [ ] User has clear instructions for next steps

## Constraints & Guidelines

- **DO NOT** change business logic or functionality
- **DO NOT** change JSON structure (unless absolutely necessary)
- **DO** prefer POJO DTOs over ObjectNode for new code
- **DO** maintain backward compatibility in JSON format
- **DO** commit frequently (every file or logical change)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation
- **DO** verify CVE resolution with dependency scan

## GitHub Copilot Usage Tips

For **replacing IBM JSON**, use prompts like:
- "Convert this IBM JSONObject code to Jackson 2.17 ObjectMapper"
- "Create a DTO class for this IBM JSONObject structure"
- "Replace this IBM JSONArray usage with Jackson ArrayNode"

For **upgrading Jackson 1.x**, use prompts like:
- "Update this Jackson 1.x code to Jackson 2.17 API"
- "Configure ObjectMapper for Java 8 date/time serialization"
- "Update JsonNode API from getTextValue() to asText()"

## Start Command

Begin by:
1. Reading all transformation documentation files
2. Analyzing the current codebase for Jackson 1.x and IBM JSON usage
3. Creating the detailed plan with affected files and CVE list
4. Presenting the plan to me for approval
5. Waiting for my "continue" command before proceeding

**Remember:** You must complete this task fully before moving to the next task. Take your time, be thorough, document everything, and ask questions if anything is unclear. Security vulnerability resolution is critical.

---

**Ready to begin? Start with step 1: Read transformation documentation and create the execution plan.**
