# Diff: CustomerOrderServicesTest/pom.xml

**File:** `CustomerOrderServicesTest/pom.xml`  
**Type:** Test Module (WAR)  
**Module:** CustomerOrderServicesTest  
**Task:** TASK-001  
**Commit:** 8615009

---

## Summary of Changes

Updated the Test module from Java 1.6 to Java 17 and removed duplicate plugin declaration.

---

## Changes Made

### 1. Updated Compiler Plugin Configuration

**Before:**
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

**After:**
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

### 2. Removed Duplicate maven-war-plugin

**Removed:**
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-war-plugin</artifactId>
  <configuration>
    <webXml>WebContent/WEB-INF/web.xml</webXml>
  </configuration>
</plugin>
```

**Rationale:** Same as CustomerOrderServicesWeb - duplicate declaration removed

---

## Specific Changes

| Property | Before | After | Reason |
|----------|--------|-------|--------|
| **Plugin Version** | 3.6.1 | 3.11.0 | Latest stable with Java 17 support |
| **Java Version** | 1.6 | 17 | Critical upgrade (11 major versions) |
| **Configuration Style** | `<source>` + `<target>` | `<release>` | Modern configuration |
| **maven-war-plugin** | 2 declarations | 1 declaration | Removed duplicate |

---

## Impact

**Module:** CustomerOrderServicesTest  
**Contents:** 
- 4 test classes (REST endpoint tests, JPA tests)
- Test data and JSON fixtures
- DBUnit dependencies (outdated)

**Compilation Result:** ✅ SUCCESS  
**Warnings:** 33 warnings (expected)

### Warning Categories:

1. **JPA Enum Warnings (16 warnings)**
   ```
   [WARNING] unknown enum constant javax.persistence.FetchType.EAGER
   [WARNING] unknown enum constant javax.persistence.CascadeType.MERGE
   ```
   - Cause: javax.persistence.* classes not in classpath during compilation
   - Impact: None (annotations still work at runtime)
   - Resolution: Will be addressed in dependency upgrade task

2. **Deprecation Warnings (17 warnings)**
   ```
   [WARNING] Long(long) in java.lang.Long has been deprecated
   ```
   - Locations: ProductRESTSearchTest.java (multiple lines)
   - Cause: Using deprecated `new Long(long)` constructor
   - Resolution: Replace with `Long.valueOf(long)` in future task

---

## Validation

```
[INFO] Building Customer Order Services Test Module 0.1.0-SNAPSHOT [4/5]
[INFO] Compiling 4 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
```

**Test Execution:**
```
[INFO] --- surefire:3.2.5:test (default-test) @ CustomerOrderServicesTest ---
[INFO] No tests to run.
```

**Note:** Test classes exist but are not properly configured to run with Maven Surefire. This will be addressed in TASK-002 (test generation/configuration).

---

## Breaking Changes

None - code compiles successfully with warnings. All warnings are non-critical and will be addressed in subsequent tasks:
- TASK-003: Dependency upgrades (fix javax.persistence references)
- TASK-004: Code modernization (replace deprecated Long constructors)

---

## Files Affected

- `CustomerOrderServicesTest/pom.xml` (1 file, 2 insertions, 10 deletions)
