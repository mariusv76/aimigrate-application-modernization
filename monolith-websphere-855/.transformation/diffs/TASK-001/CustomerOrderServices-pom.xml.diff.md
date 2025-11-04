# Diff: CustomerOrderServices/pom.xml

**File:** `CustomerOrderServices/pom.xml`  
**Type:** EJB Module  
**Module:** CustomerOrderServices  
**Task:** TASK-001  
**Commit:** f8b7457

---

## Summary of Changes

Updated the EJB module from Java 1.8 to Java 17 using modern compiler plugin configuration.

---

## Changes Made

### Before
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

### After
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

## Specific Changes

| Property | Before | After | Reason |
|----------|--------|-------|--------|
| **Plugin Version** | 3.6.1 | 3.11.0 | Latest stable version with Java 17 support |
| **Java Version** | 1.8 (source/target) | 17 (release) | Upgrade to Java 17 LTS |
| **Configuration Style** | `<source>` + `<target>` | `<release>` | Modern approach, ensures full Java 17 compatibility |

---

## Rationale

**Why `<release>17</release>`?**
- Ensures compilation against Java 17 APIs only
- Prevents accidental use of newer APIs not available in Java 17
- Guarantees bytecode compatibility with Java 17 JVM
- Simpler than managing source/target/bootclasspath separately

---

## Impact

**Module:** CustomerOrderServices (EJB 3.0)  
**Contents:** 
- 8 JPA entities (Customer, Product, Order, LineItem, etc.)
- 2 stateless session beans
- JPA 2.0 persistence layer

**Compilation Result:** ✅ SUCCESS  
**Warnings:** 1 deprecation warning (LineItem.java uses deprecated API)  
**Errors:** None

---

## Validation

```
[INFO] Building CustomerOrderServices 0.1.0-SNAPSHOT [2/5]
[INFO] Compiling 22 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
```

**Test Results:**
- No tests to run (0% test coverage - will be addressed in TASK-002)

---

## Breaking Changes

None - code is compatible with Java 17. The deprecation warning will be addressed in future dependency upgrade tasks.

---

## Files Affected

- `CustomerOrderServices/pom.xml` (1 file, 2 insertions, 3 deletions)
