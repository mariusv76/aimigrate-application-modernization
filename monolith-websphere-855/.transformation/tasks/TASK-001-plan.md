# TASK-001: Java Version Upgrade (8 → 17) - Execution Plan

**Task ID:** TASK-001  
**Task Name:** Java Version Upgrade (8 → 17)  
**Priority:** 🔴 CRITICAL  
**Status:** PLANNING  
**Created:** 2025-11-04  
**Estimated Effort:** 40 hours manual / 20 hours AI-assisted  

---

## Executive Summary

This task upgrades the Customer Order Services application from Java 8 to Java 17 LTS. This is the foundational task that must be completed before any framework migration or Azure integration work can proceed.

### Current State Analysis

Based on analysis of the workspace, I've identified the following current configuration:

#### Maven Module Structure
1. **CustomerOrderServicesProject** (Parent/Aggregator POM)
   - Location: `CustomerOrderServicesProject/pom.xml`
   - Current Java Version: Not explicitly set (inherits defaults)
   - Packaging: `pom`

2. **CustomerOrderServices** (EJB Module)
   - Location: `CustomerOrderServices/pom.xml`
   - Current Java Version: `1.8` (source/target)
   - Maven Compiler Plugin: `3.6.1`
   - Packaging: `ejb`

3. **CustomerOrderServicesWeb** (Web Module)
   - Location: `CustomerOrderServicesWeb/pom.xml`
   - Current Java Version: `1.6` (source/target) ⚠️ **Very outdated!**
   - Maven Compiler Plugin: `3.6.1`
   - Packaging: `war`

4. **CustomerOrderServicesTest** (Test Module)
   - Location: `CustomerOrderServicesTest/pom.xml`
   - Current Java Version: `1.6` (source/target) ⚠️ **Very outdated!**
   - Maven Compiler Plugin: `3.6.1`
   - Packaging: `war`

5. **CustomerOrderServicesApp** (EAR Assembly)
   - Location: `CustomerOrderServicesApp/pom.xml`
   - Current Java Version: `1.8` (source/target)
   - Maven Compiler Plugin: `3.6.1`
   - Packaging: `ear`

### Key Findings

✅ **Good News:**
- Clean multi-module Maven structure
- Standard Maven build configuration
- No complex custom build scripts
- All modules already have maven-compiler-plugin defined

⚠️ **Concerns:**
- Inconsistent Java versions across modules (1.6 vs 1.8)
- Web and Test modules using obsolete Java 1.6
- Old compiler plugin configuration format (source/target vs release)

### Breaking Changes Expected

Based on Java 8 → 17 migration best practices:

1. **Minimal** - Project uses standard Java EE APIs
2. **No usage of internal JDK APIs detected** (no sun.* imports visible)
3. **Standard Maven plugins** - should be compatible
4. **Dependencies may need version updates** (addressed in future tasks)

---

## Detailed Execution Plan

### Step 1: Update Parent POM (CustomerOrderServicesProject)

**File:** `CustomerOrderServicesProject/pom.xml`

**Action:** Add properties section to centralize Java version management

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<build>
    <pluginManagement>
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
    </pluginManagement>
</build>
```

**Rationale:**
- Centralizes version management in parent POM
- Uses modern `<release>` property instead of `source`/`target`
- Enables child modules to inherit configuration
- Updates compiler plugin to latest stable version (3.11.0)

---

### Step 2: Update CustomerOrderServices POM

**File:** `CustomerOrderServices/pom.xml`

**Current Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
</plugin>
```

**New Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>
    </configuration>
</plugin>
```

**Changes:**
- ✅ Update plugin version: `3.6.1` → `3.11.0`
- ✅ Replace `<source>1.8</source>` + `<target>1.8</target>` with `<release>17</release>`
- ✅ Inherit properties from parent POM

---

### Step 3: Update CustomerOrderServicesWeb POM

**File:** `CustomerOrderServicesWeb/pom.xml`

**Current Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <source>1.6</source>
        <target>1.6</target>
    </configuration>
</plugin>
```

**New Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>
    </configuration>
</plugin>
```

**Critical Change:** This module jumps from Java 1.6 to Java 17 (11 major versions!)

---

### Step 4: Update CustomerOrderServicesTest POM

**File:** `CustomerOrderServicesTest/pom.xml`

**Same changes as CustomerOrderServicesWeb** (currently at Java 1.6)

---

### Step 5: Update CustomerOrderServicesApp POM

**File:** `CustomerOrderServicesApp/pom.xml`

**Current Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
</plugin>
```

**New Configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>
    </configuration>
</plugin>
```

---

## Commit Strategy

Each module update will be committed separately for traceability:

1. **Commit 1:** `build(parent): configure Java 17 in parent POM`
2. **Commit 2:** `build(ejb): update CustomerOrderServices to Java 17`
3. **Commit 3:** `build(web): update CustomerOrderServicesWeb to Java 17`
4. **Commit 4:** `build(test): update CustomerOrderServicesTest to Java 17`
5. **Commit 5:** `build(ear): update CustomerOrderServicesApp to Java 17`
6. **Commit 6 (if needed):** `fix: resolve Java 17 compilation errors`

---

## Validation Strategy

### Build Validation
```powershell
# Clean build of all modules
mvn clean compile

# Expected output: BUILD SUCCESS for all 4 modules
# (Parent POM doesn't compile as it's just an aggregator)
```

### Module-by-Module Validation
```powershell
# Validate each module individually
cd CustomerOrderServices
mvn clean compile
cd ..\CustomerOrderServicesWeb
mvn clean compile
cd ..\CustomerOrderServicesTest
mvn clean compile
cd ..\CustomerOrderServicesApp
mvn clean package
```

### Test Execution (if tests exist)
```powershell
mvn test
```

**Note:** Based on assessment, test coverage is 0%, so we expect:
- No test execution
- Documentation of this finding for TASK-002 (test generation)

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **Compilation errors due to removed APIs** | Low | Medium | Review compilation errors, update deprecated API usage |
| **Dependency compatibility issues** | Medium | High | Will be addressed in Phase 1 (dependency upgrades) |
| **Build plugin incompatibilities** | Low | Low | Using latest stable versions (3.11.0) |
| **Inconsistent Java versions causing issues** | High | Medium | Standardizing all modules to Java 17 eliminates this |

---

## Dependencies & Blockers

### Prerequisites
- ✅ Git repository initialized (current branch: `agent-test`)
- ✅ Maven installed
- ⚠️ JDK 17 required (will verify/install if needed)

### Blocked Tasks
The following tasks **cannot proceed** until TASK-001 is complete:
- TASK-002: Test generation (requires Java 17)
- TASK-003: Framework migration (requires Java 17)
- TASK-004: Dependency upgrades (requires Java 17 baseline)

---

## Success Criteria

- [x] All 5 POM files updated with Java 17 configuration
- [ ] Parent POM has centralized Java version properties
- [ ] All modules use `<release>17</release>` configuration
- [ ] Maven compiler plugin upgraded to version 3.11.0
- [ ] `mvn clean compile` succeeds for all modules
- [ ] All commits follow conventional commit format
- [ ] Each module update has dedicated commit
- [ ] Progress tracking updated after each step
- [ ] No compilation errors or warnings
- [ ] Documentation generated for all changes

---

## Effort Estimate

| Activity | Estimated Time |
|----------|---------------|
| Planning & analysis | 1 hour (COMPLETE) |
| Update parent POM | 15 minutes |
| Update 4 child POMs | 30 minutes (7.5 min each) |
| Build verification | 30 minutes |
| Fix compilation errors (if any) | 1-2 hours (buffer) |
| Documentation & commits | 45 minutes |
| **TOTAL** | **3-4 hours** |

---

## Approval Request

Please review this execution plan and confirm:

1. ✅ Approach is sound (centralized Java version in parent POM)
2. ✅ Commit strategy is acceptable (6 separate commits)
3. ✅ Success criteria are clear
4. ✅ Risk mitigation is adequate

**Once approved, I will proceed with implementation.**

---

## Next Steps After Approval

1. Create feature branch: `migration/task-001-java-17-upgrade`
2. Initialize progress tracking document
3. Execute POM updates with commits
4. Validate builds
5. Generate documentation
6. Present final summary for review
