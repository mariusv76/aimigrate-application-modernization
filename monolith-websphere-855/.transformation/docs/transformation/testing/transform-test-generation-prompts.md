# Unit Test Baseline Generation for .NET Migration (GitHub Copilot Guide)

> **Purpose:** Step-by-step instructions for developers to use GitHub Copilot Chat prompts to establish a representative baseline of unit tests before migrating from .NET 8 to .NET 9. These tests serve as migration evidence—proving functional equivalence post-upgrade.

---

## 1. Overview

When migrating a .NET solution from one version to another (e.g., .NET 8 → .NET 9), you need **proof** that core business logic remains intact. This guide shows you how to:

1. **Discover** what unit tests already exist
2. **Identify gaps** in critical business logic coverage
3. **Generate baseline unit tests** using GitHub Copilot prompts
4. **Run and validate** tests to establish a pre-migration snapshot
5. **Use the same tests post-migration** to verify nothing broke

### What You'll Create


- A representative set of **pure unit tests** covering:
  - Core business logic and calculations
  - Edge cases and boundary conditions
  - Null/invalid input handling (guard clauses)
  - Domain invariants and validation rules
  - Serialization/deserialization fidelity (for DTOs)

### Out of Scope

This guide focuses on **unit tests only**—no integration tests, API endpoint tests, database tests, or performance benchmarks. Those can be added later.

---

## 2. Prerequisites

Before you begin:


- [ ] .NET 8 solution builds successfully (`dotnet build`)

- [ ] GitHub Copilot Chat enabled in your IDE (VS Code or Visual Studio)

- [ ] Test project(s) exist or can be created (using xUnit, FluentAssertions, Moq/NSubstitute)

- [ ] Access to production code you want to test

**Test Frameworks Used:**


- **xUnit** for test structure

- **FluentAssertions** for readable assertions

- **Moq** or **NSubstitute** for mocking dependencies

---

## 3. Step-by-Step Workflow

### Step 1: Discover Existing Test Coverage

**Goal:** Understand what you already have and where the gaps are.

#### 📋 Prompt 1.1 – Inventory Your Test Projects

Open **GitHub Copilot Chat** and paste:


```text

List all .csproj files in this solution. Identify which are production projects and which are test projects. For each production project, tell me if a corresponding test project exists.
```


**What to do with the output:**

- Note any production projects without test projects → you'll need to create them

- If test projects exist, proceed to the next prompt


#### 📋 Prompt 1.2 – Find Untested Classes

For each production project, run:


```text

In project [ProjectName], list all public classes and their public methods. For each class, tell me if there's a corresponding test file (e.g., MyServiceTests.cs). Highlight classes with NO test coverage.
```


**What to do with the output:**

- Create a list of **untested classes** (these are your high-priority targets)

- Focus on classes with business logic, calculations, validations, or state management

---

### Step 2: Prioritize What to Test

**Goal:** Focus effort on high-value, high-risk code.

#### 📋 Prompt 2.1 – Identify Critical Business Logic


```text

From the untested classes in [ProjectName], which ones contain:

- Financial calculations or pricing logic

- Security or authorization decisions

- Data validation or business rules

- State transitions or workflow logic

- Serialization/deserialization of DTOs

Rank them by criticality.
```


**What to do with the output:**

- Start with the top 3–5 critical classes

- These will form your baseline test suite


#### 📋 Prompt 2.2 – Enumerate Methods to Test

For each critical class:


```text

For class [ClassName] in file [FilePath], list all public methods that should have unit tests. Exclude simple property getters/setters. For each method, describe what it does in one sentence.
```


**What to do with the output:**

- You now have a specific list of methods to test

- Move to implementation

---

### Step 3: Generate Unit Tests

**Goal:** Use Copilot to write tests in small, reviewable batches.

#### 📋 Prompt 3.1 – Create Test File Structure

If the test file doesn't exist:


```text

Create a new xUnit test class file for [ClassName]. Use FluentAssertions for assertions. Include:

- Proper namespace matching the test project

- Class named [ClassName]Tests

- A constructor that sets up any needed test fixtures

- Use Moq for mocking dependencies
```


**What to do with the output:**

- Review the generated file

- Save it in your test project (e.g., `tests/MyProject.Tests/MyServiceTests.cs`)


#### 📋 Prompt 3.2 – Generate Happy Path Tests

Start with the most common, valid scenarios:


```text

Generate xUnit tests for [ClassName].[MethodName] covering:

- Happy path with valid inputs

- Expected return value or state change

Use FluentAssertions. Mock any dependencies with Moq. Name tests using MethodName_Scenario_ExpectedResult pattern.
```


**What to do with the output:**

- Add tests to your test file

- Run `dotnet test` to verify they pass

- Commit before moving to next batch


#### 📋 Prompt 3.3 – Generate Edge Case Tests


```text

For [ClassName].[MethodName], generate tests for edge cases:

- Boundary values (min, max, zero, empty collections)

- Null inputs (if applicable)

- Empty strings or whitespace

- Large datasets or extreme values

Use FluentAssertions and the MethodName_Scenario_ExpectedResult naming pattern.
```


**What to do with the output:**

- Add to test file

- Run tests

- Fix any failures by clarifying expected behavior


#### 📋 Prompt 3.4 – Generate Negative/Exception Tests


```text

For [ClassName].[MethodName], generate tests that verify proper exception handling:

- ArgumentNullException when required parameters are null

- ArgumentException for invalid inputs

- InvalidOperationException for illegal state transitions

Use FluentAssertions .Should().Throw<TException>() syntax.
```


**What to do with the output:**

- Add to test file

- Verify guard clauses exist in production code

- Add them if missing (this is a migration-safe improvement)


#### 📋 Prompt 3.5 – Generate Serialization Tests (DTOs only)

For classes that serialize to JSON:


```text

For DTO class [ClassName], generate tests verifying:

- Round-trip serialization with System.Text.Json

- Property names match expected casing (camelCase in JSON)

- Null handling is correct

- Deserialization of invalid JSON throws expected exceptions
```


**What to do with the output:**

- Add to test file

- Ensure serializer settings match production configuration

---

### Step 4: Review and Refine Tests

**Goal:** Ensure tests are deterministic, clear, and maintainable.

#### 📋 Prompt 4.1 – Check for Nondeterminism


```text

Review these test methods [paste test code]. Identify any sources of nondeterminism:

- Use of DateTime.Now or DateTime.UtcNow

- Random number generation without fixed seed

- Thread.Sleep or timing dependencies

- Accessing external resources

Suggest refactorings to make tests deterministic.
```


**What to do with the output:**

- Implement suggested refactorings

- Consider injecting `IClock` or `ITimeProvider` for time dependencies


#### 📋 Prompt 4.2 – Simplify Overly Complex Tests


```text

Review this test [paste code]. Does it test multiple unrelated behaviors? If so, suggest how to split it into focused, single-purpose tests.
```


**What to do with the output:**

- Split multi-assert tests into separate test methods

- Each test should verify ONE logical expectation

---

### Step 5: Establish Baseline & Document

**Goal:** Capture the pre-migration state as evidence.

#### ✅ Run Full Test Suite

```powershell
dotnet test --configuration Release --logger "trx;LogFileName=baseline-net8.trx"
`


**What to do:**

- Ensure all tests pass (or document any known failures)

- Save the `.trx` file as your baseline

- Commit all test code to source control


#### ✅ Measure Coverage (Optional)

If you want coverage metrics:

```powershell
dotnet test /p:CollectCoverage=true /p:CoverletOutputFormat=cobertura
`


**What to do:**

- Review `coverage.cobertura.xml`

- Aim for 60–80% line coverage of critical business logic

- Don't chase 100%—focus on high-value code


#### ✅ Document Test Strategy

Create a brief summary:

```markdown
# Unit Test Baseline for .NET 8 → 9 Migration

## Test Coverage Summary

- **Test Projects:** [list]

- **Classes Covered:** [count]

- **Total Tests:** [count]

- **Test Run Date:** [date]

- **Result:** All passing / [X] known failures documented below

## Known Gaps

- [Any intentionally untested areas]

## Post-Migration Validation Plan
1. Upgrade solution to .NET 9
2. Run same test suite
3. Compare results to baseline
4. Investigate any new failures
```

Save as `docs/migration-test-baseline.md`

---

## 4. Post-Migration: Using Your Tests as Evidence

After upgrading to .NET 9:

1. **Run the same test suite:**

   ```powershell
   dotnet test --configuration Release --logger "trx;LogFileName=post-migration-net9.trx"
   ```

2. **Compare results:**

   - Same tests passing? ✅ Evidence of functional equivalence
   - New failures? 🔍 Investigate breaking changes or behavior differences

3. **Document outcome:**
   - Add comparison to your migration report
   - Include both `.trx` files as evidence

---

## 5. Best Practices

**When Writing Tests:**


- ✅ **One logical assertion per test** – easier to diagnose failures

- ✅ **Clear naming** – `MethodName_Scenario_ExpectedResult`

- ✅ **Arrange-Act-Assert pattern** – organize test code clearly

- ✅ **Mock only true dependencies** – prefer in-memory fakes for simple data

- ✅ **Make tests fast** – no Thread.Sleep, no external I/O

- ✅ **Deterministic inputs** – no random data without fixed seeds

**What to Avoid:**


- ❌ Testing trivial property getters/setters

- ❌ Copying production logic into tests (mirrors bugs)

- ❌ Overmocking – mocking everything makes tests brittle

- ❌ Multi-assert spaghetti – testing unrelated things in one test

- ❌ Ignoring test failures – every failure is a signal

---

## 6. Troubleshooting Common Issues

### "Copilot suggests integration tests instead of unit tests"

**Fix:** Be explicit in your prompt:

```text

Generate PURE UNIT TESTS (no database, no HTTP, no containers). Mock all external dependencies using Moq.
```

### "Tests are flaky or fail intermittently"

**Fix:** Run this prompt:

```text

Analyze this test [paste code]. Identify any nondeterministic behavior (time, threading, randomness). Suggest refactorings.
```

### "Too many tests generated – overwhelming"

**Fix:** Work in small batches:

- Start with 1–2 critical classes

- Generate 3–5 tests at a time

- Run and validate before continuing

### "Don't know what to test first"

**Fix:** Use Prompt 2.1 to identify critical business logic. If still unclear, ask:

```text

If this application had a critical bug, which classes would cause the most damage? Rank them.
```

---

## 7. Quick Reference: Essential Prompts

| When you need... | Use this prompt |
|------------------|----------------|
| Find untested classes | "List all public classes in [project] and identify which have NO test coverage" |
| Identify critical logic | "Which classes in [project] contain business logic, calculations, or validation rules?" |
| Create test file | "Create xUnit test class for [ClassName] using FluentAssertions and Moq" |
| Happy path tests | "Generate xUnit tests for [Class].[Method] covering valid inputs and expected output" |
| Edge cases | "Generate edge case tests for [Class].[Method]: null, empty, boundary values" |
| Exception tests | "Generate tests verifying [Class].[Method] throws ArgumentNullException for null parameters" |
| Check determinism | "Review these tests and identify nondeterministic behavior (time, random, I/O)" |
| Simplify complex test | "Does this test check multiple behaviors? Suggest how to split it" |

---

## 8. Related Resources


- **Prerequisites Document:** See `transform-prereqs.md` Section 14 (Testing Prerequisites)

- **xUnit Documentation:** <https://xunit.net/>

- **FluentAssertions Documentation:** <https://fluentassertions.com/>

- **.NET Breaking Changes:** <https://learn.microsoft.com/en-us/dotnet/core/compatibility/>

---

## 9. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2025-09-30 | Redrafted for developer-focused instructions with Copilot prompts | Transformation Agent Docs |

---

End of document.
