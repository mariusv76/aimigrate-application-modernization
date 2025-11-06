# TASK-003: Jakarta EE Namespace Migration - Todo List

**Created:** November 5, 2025  
**Status:** IN PROGRESS

---

## Phase 1: Environment Setup ✅

- [x] Create migration branch `migration/task-003-jakarta-migration`
- [x] Create plan.md with detailed execution plan
- [x] Create progress.md for tracking
- [x] Create todos.md (this file)
- [x] Get user approval to proceed
- [x] Initial commit

## Phase 2: Configure OpenRewrite

- [ ] Add OpenRewrite Maven plugin to parent POM
- [ ] Configure Jakarta migration recipe dependency
- [ ] Test OpenRewrite with dry-run
- [ ] Commit: "build: add OpenRewrite plugin for Jakarta migration"

## Phase 3: Update Maven Dependencies

- [ ] Add Jakarta EE 10 platform dependency to parent POM
- [ ] Add Jakarta Persistence API 3.1.0
- [ ] Add Jakarta Servlet API 6.0.0
- [ ] Add Jakarta RESTful WS API 3.1.0
- [ ] Add Jakarta EJB API 4.0.1
- [ ] Add Jakarta Validation API 3.0.2
- [ ] Add Jakarta JSON API 2.1.2
- [ ] Update module POMs if needed
- [ ] Commit: "build: upgrade to Jakarta EE 10 dependencies"

## Phase 4: Run OpenRewrite Migration

- [ ] Execute `mvn rewrite:run` from project root
- [ ] Review changes in domain classes (9 files)
- [ ] Review changes in service classes (2 files)
- [ ] Review changes in REST resources (3 files)
- [ ] Review changes in test files (7 files)
- [ ] Verify all javax.* imports updated to jakarta.*
- [ ] Commit: "refactor: migrate javax.* to jakarta.* namespace (OpenRewrite)"

## Phase 5: Update XML Descriptors

- [ ] Update CustomerOrderServices/ejbModule/META-INF/persistence.xml (2.0 → 3.1)
- [ ] Update CustomerOrderServices/ejbModule/META-INF/orm.xml (1.0 → 3.1)
- [ ] Update CustomerOrderServicesWeb/WebContent/WEB-INF/web.xml (3.0 → 6.0)
- [ ] Update CustomerOrderServicesTest/WebContent/WEB-INF/web.xml (2.5 → 6.0)
- [ ] Update CustomerOrderServicesApp/META-INF/application.xml (6 → 10)
- [ ] Commit: "refactor: update XML descriptors to Jakarta EE 10 schemas"

## Phase 6: Manual Cleanup & Verification

- [ ] Search for remaining `import javax.persistence.*` (should be empty)
- [ ] Search for remaining `import javax.ejb.*` (should be empty)
- [ ] Search for remaining `import javax.ws.rs.*` (should be empty)
- [ ] Search for remaining `import javax.servlet.*` (should be empty)
- [ ] Search for remaining `import javax.annotation.*` (should be empty)
- [ ] Verify `import javax.naming.*` still exists (Java SE - should remain)
- [ ] Search for XML namespace `http://java.sun.com` (should be empty)
- [ ] Manually fix any missed imports
- [ ] Commit: "fix: manually update remaining javax imports" (if needed)

## Phase 7: Build Validation

- [ ] Run `mvn clean compile -pl :CustomerOrderServices`
- [ ] Run `mvn clean compile -pl :CustomerOrderServicesWeb`
- [ ] Run `mvn clean compile -pl :CustomerOrderServicesTest`
- [ ] Run `mvn clean compile -pl :CustomerOrderServicesApp`
- [ ] Fix any compilation errors
- [ ] Document API changes (if any)
- [ ] Commit: "fix: resolve Jakarta EE compilation errors" (if needed)

## Phase 8: Test Validation

- [ ] Run `mvn clean test -pl :CustomerOrderServices`
- [ ] Verify domain tests pass (133 tests)
- [ ] Verify service tests pass (30 tests)
- [ ] Run `mvn clean test -pl :CustomerOrderServicesWeb`
- [ ] Fix any test failures
- [ ] Update test mocks if needed
- [ ] Verify total 163 tests pass
- [ ] Verify JaCoCo coverage ≥72%
- [ ] Commit: "fix: update tests for Jakarta EE 10 API changes" (if needed)

## Phase 9: Integration Testing

- [ ] Run `mvn clean package` from project root
- [ ] Verify CustomerOrderServices.jar builds
- [ ] Verify CustomerOrderServicesWeb.war builds
- [ ] Verify CustomerOrderServicesTest.war builds
- [ ] Verify CustomerOrderServicesApp.ear builds
- [ ] Check EAR file structure and dependencies
- [ ] Verify no javax.* JARs in packaged artifacts
- [ ] Commit: "fix: resolve packaging issues" (if needed)

## Phase 10: Documentation & Summary

- [ ] Create summary.md with migration results
- [ ] Document all files changed
- [ ] Document all commits made
- [ ] List before/after package usage
- [ ] Document lessons learned
- [ ] Create handoff.md for next steps
- [ ] Update progress.md to 100% complete
- [ ] Mark all todos complete

---

## Summary Statistics

- **Total Todos:** 65
- **Completed:** 6 (9%)
- **Remaining:** 59
- **In Progress:** Phase 2

**Last Updated:** November 5, 2025, 2:30 PM
