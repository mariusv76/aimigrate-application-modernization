# TASK-002 Todo List

## Phase 1: Setup
- [x] Create feature branch `migration/task-002-test-foundation`
- [x] Initialize progress tracking
- [x] Create plan document
- [ ] Configure JUnit 5 BOM in parent POM
- [ ] Add Mockito dependencies to CustomerOrderServices
- [ ] Add AssertJ dependencies to CustomerOrderServices
- [ ] Add REST Assured dependencies to CustomerOrderServicesWeb
- [ ] Configure maven-surefire-plugin
- [ ] Configure jacoco-maven-plugin with 70% threshold
- [ ] Create test directory structure
- [ ] Commit: "build: add JUnit 5, Mockito, AssertJ, REST Assured dependencies"

## Phase 2: Domain Model Tests
- [ ] Generate AbstractCustomerTest.java
- [ ] Generate AddressTest.java
- [ ] Generate BusinessCustomerTest.java
- [ ] Generate CategoryTest.java
- [ ] Generate LineItemTest.java
- [ ] Generate LineItemIdTest.java
- [ ] Generate OrderTest.java
- [ ] Generate ProductTest.java
- [ ] Generate ResidentialCustomerTest.java
- [ ] Commit after each test class
- [ ] Verify all domain tests pass

## Phase 3: Service Layer Tests
- [ ] Generate CustomerOrderServicesImplTest.java
- [ ] Test loadCustomer() method
- [ ] Test loadProduct() method
- [ ] Test loadCategory() method
- [ ] Test updateProduct() method
- [ ] Test addProductToCategory() method
- [ ] Test removeProductFromCategory() method
- [ ] Test loadOrder() method
- [ ] Test saveOrder() method
- [ ] Test error handling
- [ ] Test security annotations
- [ ] Commit: "test: add service tests for CustomerOrderServicesImpl"
- [ ] Verify service tests pass

## Phase 4: REST Integration Tests
- [ ] Generate CategoryResourceTest.java
- [ ] Test GET /categories endpoints
- [ ] Test POST /categories endpoint
- [ ] Test PUT /categories endpoint
- [ ] Test DELETE /categories endpoint
- [ ] Commit: "test: add integration tests for CategoryResource"
- [ ] Generate CustomerOrderResourceTest.java
- [ ] Test order management endpoints
- [ ] Commit: "test: add integration tests for CustomerOrderResource"
- [ ] Generate ProductResourceTest.java
- [ ] Test product endpoints
- [ ] Commit: "test: add integration tests for ProductResource"
- [ ] Verify all integration tests pass

## Phase 5: Coverage Validation
- [ ] Run: `mvn clean test`
- [ ] Generate coverage report: `mvn jacoco:report`
- [ ] Review coverage in target/site/jacoco/index.html
- [ ] Identify coverage gaps
- [ ] Add targeted tests for gaps
- [ ] Re-run until ≥70% achieved
- [ ] Document final coverage percentage

## Phase 6: Documentation
- [ ] Create summary.md
- [ ] Document test metrics
- [ ] Create diff documentation for key tests
- [ ] Final progress update
- [ ] Create handoff.md

## Final Validation
- [ ] All tests passing
- [ ] Coverage ≥70%
- [ ] No skipped tests
- [ ] All commits have proper messages
- [ ] Ready for user review
