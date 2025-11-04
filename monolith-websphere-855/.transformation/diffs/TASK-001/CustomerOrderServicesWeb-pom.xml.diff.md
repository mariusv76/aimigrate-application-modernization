# Diff: CustomerOrderServicesWeb/pom.xml

**File:** `CustomerOrderServicesWeb/pom.xml`  
**Type:** Web Module (WAR)  
**Module:** CustomerOrderServicesWeb  
**Task:** TASK-001  
**Commit:** 66978b7

---

## Summary of Changes

Updated the Web module from Java 1.6 to Java 17 (11 major version jump) and removed duplicate plugin declaration.

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

**Rationale:** 
- This was a duplicate declaration without version
- The properly configured maven-war-plugin (version 3.1.0) remains
- Maven POM schema requires unique plugin declarations

---

## Specific Changes

| Property | Before | After | Reason |
|----------|--------|-------|--------|
| **Plugin Version** | 3.6.1 | 3.11.0 | Latest stable with Java 17 support |
| **Java Version** | 1.6 | 17 | **Critical upgrade** (11 major versions) |
| **Configuration Style** | `<source>` + `<target>` | `<release>` | Modern configuration |
| **maven-war-plugin** | 2 declarations | 1 declaration | Removed duplicate |

---

## Critical Upgrade Note

⚠️ **This module was running on Java 1.6 (released 2006, EOL 2013)**

This represents a **17-year technology gap**. Major changes between Java 1.6 and Java 17:
- Lambdas and functional interfaces (Java 8)
- Modules system (Java 9)
- `var` keyword (Java 10)
- Text blocks (Java 15)
- Records (Java 16)
- Sealed classes (Java 17)

---

## Impact

**Module:** CustomerOrderServicesWeb (JAX-RS 1.1 REST API)  
**Contents:** 
- 4 REST resource classes
- JSON serialization (Jackson + IBM JSON)
- Static HTML/JavaScript frontend

**Compilation Result:** ✅ SUCCESS  
**Warnings:** 3 deprecation warnings  
```
[WARNING] Long(java.lang.String) in java.lang.Long has been deprecated
  - CustomerOrderResource.java:108,53
  - CustomerOrderResource.java:137,98
  - CustomerOrderResource.java:167,62
```

**Errors:** None

---

## Validation

```
[INFO] Building Customer Order Services Web Module 0.1.0-SNAPSHOT [3/5]
[INFO] Compiling 4 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
```

**Test Results:**
- No tests to run (0% test coverage)

---

## Breaking Changes

None detected. The deprecation warnings are expected:
- `new Long(String)` constructor deprecated in Java 9
- Replacement: `Long.valueOf(String)` or `Long.parseLong(String)`
- Will be addressed in future code modernization task

---

## Files Affected

- `CustomerOrderServicesWeb/pom.xml` (1 file, 2 insertions, 10 deletions)
