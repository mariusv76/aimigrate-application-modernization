# Migration Task Prompt: Java 8 → Java 17 Upgrade

## Mission
You are an AI agent tasked with performing the **first migration task** for the Customer Order Services application: **Upgrade Java 8 to Java 17**. This is a critical foundation task that must be completed successfully before any framework or Azure integration work can begin.

## Task Overview
**Task ID:** TASK-001  
**Task Name:** Java Version Upgrade (8 → 17)  
**Priority:** 🔴 CRITICAL  
**Dependencies:** None (this is the first task)  
**Estimated Effort:** 40 hours manual / 20 hours AI-assisted  
**Success Criteria:** All modules compile with Java 17, all tests pass (or test generation completes if none exist)

## Detailed Instructions

### Phase 1: Planning & Preparation

1. **Read all transformation documentation**
   - Review FRAMEWORK_MIGRATION_GUIDE.md Section 2
   - Review MIGRATION_ASSESSMENT.md Section 2.1
   - Review ASSESSMENT_SUMMARY.md Phase 1

2. **Create a detailed execution plan**
   - Analyze all `pom.xml` files in the workspace
   - Identify current Java version settings
   - List all modules that need updates
   - Identify potential breaking changes
   - Document expected outcomes
   - Save plan to `.vscode/transformation/TASK-001/plan.md`

3. **Get user confirmation**
   - Present the plan to the user
   - Wait for explicit approval before proceeding
   - Document any plan adjustments based on feedback

### Phase 2: Branch & Progress Setup

4. **Create a dedicated migration branch**
   - Branch name: `migration/task-001-java-17-upgrade`
   - Branch from: current branch (`agent-test`)
   - Commit message: "chore: create branch for Java 17 upgrade task"

5. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-001/progress.md`
   - Initialize with: task start time, current status, completed steps
   - Create `.vscode/transformation/TASK-001/todos.md`
   - List all sub-tasks with checkboxes

### Phase 3: Implementation

6. **Update Maven compiler configuration** (all modules)
   - Update pom.xml (parent)
   - Update pom.xml
   - Update pom.xml
   - Update pom.xml
   - Update pom.xml
   - Changes required:
     ```xml
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

7. **Commit after each logical change**
   - Commit after parent POM update: "build: update parent POM to Java 17"
   - Commit after each module: "build: update {module-name} to Java 17"
   - Use conventional commit format

8. **Update progress tracking after each commit**
   - Update `.vscode/transformation/TASK-001/progress.md` with completed steps
   - Check off items in `.vscode/transformation/TASK-001/todos.md`
   - Document any issues or deviations

### Phase 4: Validation & Testing

9. **Build verification**
   - Run `mvn clean compile` on each module
   - Document any compilation errors
   - Fix errors iteratively
   - Commit fixes: "fix: resolve Java 17 compilation errors in {module}"

10. **Test execution**
    - Run `mvn test` (if tests exist)
    - Document test results
    - If no tests exist, document this finding
    - Note: Test generation is a separate task (TASK-002)

11. **Update todos and progress**
    - Mark validation steps complete
    - Document any remaining issues
    - Update progress percentage

### Phase 5: Documentation & Summary

12. **Generate migration summary**
    - Create `.vscode/transformation/TASK-001/summary.md` with:
      - Task completion date/time
      - Files modified (count and list)
      - Commits created (count and list with messages)
      - Issues encountered and resolutions
      - Validation results
      - Lessons learned
      - Recommendations for next tasks

13. **Generate diff documentation**
    - For EACH modified file, create `.vscode/transformation/TASK-001/diffs/{filename}.diff.md`
    - Include:
      - File path
      - Before/after code comparison
      - Explanation of changes
      - Rationale for changes

14. **Final progress update**
    - Mark task as COMPLETED in progress file
    - Update completion timestamp
    - Calculate actual vs. estimated effort
    - Document final status

### Phase 6: Handoff

15. **Create final summary report**
    - Consolidate all findings
    - List all commits made
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

Task: TASK-001
```

Types: feat, fix, build, chore, docs, refactor, test
Example: `build(pom): update compiler plugin to Java 17`

### Error Handling
- Document ALL errors encountered
- Attempt resolution 3 times
- If blocked, document in progress file and ask user
- NEVER skip errors silently

### File Organization
```
.vscode/transformation/
└── TASK-001/
    ├── plan.md
    ├── progress.md
    ├── todos.md
    ├── summary.md
    ├── handoff.md
    └── diffs/
        ├── CustomerOrderServicesProject-pom.xml.diff.md
        ├── CustomerOrderServices-pom.xml.diff.md
        └── ... (one file per changed file)
```

## Expected Deliverables

1. ✅ New branch: `migration/task-001-java-17-upgrade`
2. ✅ Updated POM files (all 5 modules) compiled with Java 17
3. ✅ 5-7 commits with proper messages
4. ✅ Task plan document
5. ✅ Progress tracking document (final state: 100%)
6. ✅ Todo list (all items checked)
7. ✅ Task summary document
8. ✅ Diff documentation for each changed file
9. ✅ Build verification (all modules compile)
10. ✅ Final handoff report for user review

## Success Criteria

- [ ] All POM files use Java 17 configuration
- [ ] `mvn clean compile` succeeds for all modules
- [ ] All commits have proper messages and task references
- [ ] Progress file shows 100% completion
- [ ] All todos are checked off
- [ ] Summary document is complete and accurate
- [ ] Diff files exist for every changed file
- [ ] No unresolved errors or warnings
- [ ] User has clear instructions for next steps

## Constraints & Guidelines

- **DO NOT** make changes outside the scope (no framework migration yet)
- **DO NOT** merge to main without user approval
- **DO** commit frequently (every logical change)
- **DO** update progress after every significant step
- **DO** ask for help if blocked for >30 minutes
- **DO** preserve all history (no force pushes)
- **DO** follow the exact file structure specified
- **DO** be thorough in documentation

## Start Command

Begin by:
1. Reading all transformation documentation files
2. Analyzing the current codebase structure
3. Creating the detailed plan
4. Presenting the plan to me for approval
5. Waiting for my "continue" command before proceeding

**Remember:** You must complete this task fully before moving to the next task. Take your time, be thorough, document everything, and ask questions if anything is unclear.
