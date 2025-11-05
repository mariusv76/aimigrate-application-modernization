# Diff: CustomerOrderServicesProject/pom.xml

**File:** `CustomerOrderServicesProject/pom.xml`  
**Type:** Parent POM  
**Module:** Aggregator/Parent  
**Task:** TASK-001  
**Commit:** 0bb2905

---

## Summary of Changes

Added Java 17 configuration to the parent POM to centralize version management and compiler plugin configuration for all child modules.

---

## Changes Made

### ✅ Added Properties Section

**Purpose:** Centralize Java version and compiler settings

```xml
<properties>
  <java.version>17</java.version>
  <maven.compiler.release>17</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

**Rationale:**
- `java.version`: Defines target Java version (17 LTS)
- `maven.compiler.release`: Modern replacement for source/target properties
- `project.build.sourceEncoding`: Ensures consistent UTF-8 encoding across platforms

### ✅ Added Build Plugin Management

**Purpose:** Configure compiler plugin for all child modules

```xml
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
- Uses `pluginManagement` to allow child modules to inherit configuration
- Updates compiler plugin from implicit version to 3.11.0 (latest stable)
- Uses `<release>17</release>` instead of separate `source` and `target` properties
- `<release>` ensures compile, runtime, and API compatibility with Java 17

---

## Impact

**Before:**
- No explicit Java version defined
- Child modules had inconsistent configurations (Java 1.6 vs 1.8)
- No centralized plugin management

**After:**
- Java 17 LTS specified in parent
- All child modules can inherit consistent compiler settings
- Modern compiler configuration using `<release>` property
- UTF-8 encoding enforced

---

## Validation

✅ Parent POM structure is valid  
✅ Child modules successfully inherit configuration  
✅ Build successful with `mvn clean compile`

---

## Breaking Changes

None - this is purely additive to the parent POM. Child modules still need individual updates to leverage these settings.

---

## Next Steps

Child modules (CustomerOrderServices, CustomerOrderServicesWeb, CustomerOrderServicesTest, CustomerOrderServicesApp) will be updated in subsequent commits to use Java 17.
