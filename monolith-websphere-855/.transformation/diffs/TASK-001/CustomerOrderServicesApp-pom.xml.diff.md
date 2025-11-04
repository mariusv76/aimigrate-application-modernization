# Diff: CustomerOrderServicesApp/pom.xml

**File:** `CustomerOrderServicesApp/pom.xml`  
**Type:** EAR Assembly  
**Module:** CustomerOrderServicesApp  
**Task:** TASK-001  
**Commit:** cfd10af

---

## Summary of Changes

Updated the EAR assembly module from Java 1.8 to Java 17.

---

## Changes Made

### Updated Compiler Plugin Configuration

**Before:**
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

---

## Specific Changes

| Property | Before | After | Reason |
|----------|--------|-------|--------|
| **Plugin Version** | 3.6.1 | 3.11.0 | Latest stable version with Java 17 support |
| **Java Version** | 1.8 (source/target) | 17 (release) | Upgrade to Java 17 LTS |
| **Configuration Style** | `<source>` + `<target>` | `<release>` | Modern approach |

---

## Impact

**Module:** CustomerOrderServicesApp (EAR packaging)  
**Purpose:** Enterprise Archive assembly  
**Contents:** 
- Packages CustomerOrderServices.jar (EJB)
- Packages CustomerOrderServicesWeb.war
- Packages CustomerOrderServicesTest.war
- Generates application.xml

**Compilation Result:** ✅ SUCCESS  
**Warnings:** None  
**Errors:** None

---

## Validation

```
[INFO] Building CustomerOrderServicesApp 0.1.0-SNAPSHOT [5/5]
[INFO] --- ear:2.6:generate-application-xml @ CustomerOrderServicesApp ---
[INFO] Generating application.xml
[INFO] BUILD SUCCESS
```

**Assembly Result:**
- application.xml generated successfully
- All dependent modules (EJB, WARs) packaged correctly
- EAR structure validated

---

## Breaking Changes

None - EAR assembly process is unchanged. The compiler plugin configuration only affects any source code in this module (currently none).

---

## Notes

This module primarily serves as a packaging module and contains minimal source code. The compiler plugin configuration is included for consistency and future-proofing, but the module's main function is handled by the maven-ear-plugin (version 2.6).

**EAR Structure:**
```
CustomerOrderServicesApp.ear
├── CustomerOrderServices.jar (EJB module)
├── CustomerOrderServicesWeb.war
├── CustomerOrderServicesTest.war
└── META-INF/
    ├── application.xml (generated)
    └── ibm-application-bnd.xml
```

---

## Files Affected

- `CustomerOrderServicesApp/pom.xml` (1 file, 2 insertions, 3 deletions)

---

## Migration Complete

✅ All 5 modules now compile with Java 17  
✅ Build successful across entire project  
✅ EAR assembly generates correctly  
✅ Ready for next migration phase
