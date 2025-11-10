# TASK-004.6: Fix Jackson Classloading in Open Liberty

## Context

TASK-004.5 successfully deployed the Customer Order Services application to Open Liberty 24.0.0.11, but REST API endpoints fail due to a Jackson classloading issue. Even though Jackson JARs are packaged in the WAR's WEB-INF/lib directory, Liberty's EAR classloader cannot find them because EAR applications use parent-first classloading by default.

## Objective

Configure Open Liberty to properly load Jackson libraries so that REST API endpoints function correctly with Jackson 2.17.0 for JSON serialization/deserialization.

## Current State

### What Works ✅
- Open Liberty 24.0.0.11 running successfully
- Application deploys without errors
- Web application accessible at http://localhost:9080/CustomerOrderServicesWeb/
- Static HTML content serves correctly
- Jakarta EE 10 features load properly

### What Doesn't Work ❌
- REST API endpoints return 500 errors
- Error: `java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/JsonNode`
- Error: `java.lang.ClassNotFoundException: com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider`

### Root Cause
EAR classloading in Liberty uses parent-first strategy, causing:
1. Application looks for Jackson at server level (parent)
2. Jackson not found in server libraries
3. Falls back to application level too late
4. JAX-RS provider initialization fails

## Success Criteria

1. ✅ REST API endpoint responds successfully (e.g., `/jaxrs/Product`)
2. ✅ JSON response properly serialized using Jackson 2.17.0
3. ✅ No `ClassNotFoundException` or `NoClassDefFoundError` for Jackson classes
4. ✅ Existing web application functionality still works
5. ✅ Solution is portable and maintainable

## Approach Options

### Option 1: Liberty Shared Library (RECOMMENDED)
Configure Jackson as a shared library in `server.xml`:

```xml
<library id="jacksonLib">
    <fileset dir="${shared.resource.dir}/jackson" includes="*.jar"/>
</library>

<application location="CustomerOrderServicesApp.ear">
    <classloader commonLibraryRef="jacksonLib"/>
</application>
```

**Pros:**
- Clean separation of concerns
- Reusable across multiple applications
- Standard Liberty configuration pattern

**Cons:**
- Requires copying JARs to shared location
- Server configuration change needed

### Option 2: Application Classloader Configuration
Configure the application to use application-first classloading:

```xml
<application location="CustomerOrderServicesApp.ear">
    <classloader delegation="parentLast"/>
</application>
```

**Pros:**
- Simple configuration change
- No need to copy JARs
- Application remains self-contained

**Cons:**
- May cause conflicts with server-provided libraries
- Less predictable behavior

### Option 3: Deploy as WAR Instead of EAR
Simplify deployment by using WAR-only deployment:

**Pros:**
- WAR classloading is simpler
- No EAR parent-first issues

**Cons:**
- Requires restructuring application packaging
- Loses EJB module benefits

### Option 4: Use Liberty's JSON-B Instead
Replace Jackson with Jakarta JSON-B (built into Liberty):

**Pros:**
- No classloading issues
- Part of Jakarta EE 10 spec

**Cons:**
- Doesn't validate Jackson 2.17.0 upgrade
- Code changes required
- Defeats purpose of TASK-004

## Recommended Solution

**Use Option 1 (Liberty Shared Library)** as it:
- Follows Liberty best practices
- Maintains clean architecture
- Validates Jackson 2.17.0 properly
- Is production-ready

## Implementation Steps

### Step 1: Create Shared Library Directory
```powershell
# Create shared resources directory
New-Item -ItemType Directory -Path "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\usr\shared\resources\jackson" -Force

# Copy Jackson JARs from WAR
Copy-Item "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesWeb\target\CustomerOrderServicesWeb-0.1.0-SNAPSHOT\WEB-INF\lib\jackson-*.jar" `
    -Destination "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\usr\shared\resources\jackson\"
```

### Step 2: Update server.xml
Add library and reference configuration:

```xml
<!-- Jackson Shared Library for JSON Processing -->
<library id="jacksonLib">
    <fileset dir="${shared.resource.dir}/jackson" includes="*.jar"/>
</library>

<!-- Application with Jackson Library -->
<enterpriseApplication id="CustomerOrderServicesApp" 
                       location="CustomerOrderServicesApp.ear" 
                       name="CustomerOrderServicesApp">
    <classloader commonLibraryRef="jacksonLib"/>
</enterpriseApplication>
```

### Step 3: Remove Jackson from WAR Dependencies (Optional)
If using shared library, can optionally revert Jackson scope back to `provided` in `CustomerOrderServicesWeb/pom.xml`:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <scope>provided</scope>
</dependency>
```

### Step 4: Restart Liberty and Test
```powershell
# Restart server
.\server.bat stop defaultServer
.\server.bat start defaultServer

# Test REST endpoint
Invoke-WebRequest -Uri "http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product" -UseBasicParsing
```

### Step 5: Validate JSON Serialization
Test various endpoints:
- GET `/jaxrs/Product` - List all products
- GET `/jaxrs/Product/category` - Get categories
- POST `/jaxrs/Customer` - Create customer (if DB available)

### Step 6: Update Liberty Maven Plugin
Make shared library setup part of automated build:

```xml
<plugin>
    <groupId>io.openliberty.tools</groupId>
    <artifactId>liberty-maven-plugin</artifactId>
    <version>3.10.3</version>
    <configuration>
        <!-- Copy Jackson to shared library during build -->
        <copyDependencies>
            <location>${project.build.directory}/liberty/wlp/usr/shared/resources/jackson</location>
            <dependency>
                <groupId>com.fasterxml.jackson.core</groupId>
                <artifactId>jackson-databind</artifactId>
            </dependency>
            <!-- Include all Jackson dependencies -->
        </copyDependencies>
    </configuration>
</plugin>
```

## Validation Tests

### Test 1: Basic REST Endpoint
```bash
curl http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product
```
**Expected:** JSON array of products (or empty array if no DB)

### Test 2: JSON Serialization
```bash
curl http://localhost:9080/CustomerOrderServicesWeb/jaxrs/Product/category
```
**Expected:** JSON array of categories

### Test 3: Check Logs for Jackson Errors
```bash
tail -f messages.log | grep -i jackson
```
**Expected:** No ClassNotFoundException or NoClassDefFoundError

### Test 4: Verify Jackson Version
Check application startup logs should show Jackson 2.17.0 being loaded.

## Files to Modify

1. **Deployment/server.xml**
   - Add `<library>` element
   - Update `<application>` or `<enterpriseApplication>` element

2. **CustomerOrderServicesApp/pom.xml** (optional)
   - Add copyDependencies configuration for automation

3. **CustomerOrderServicesWeb/pom.xml** (optional)
   - Revert Jackson scope to `provided` if using shared library

## Estimated Effort

- **Manual Implementation:** 1-2 hours
- **With Automation:** 2-3 hours
- **Testing:** 1 hour
- **Total:** 2-4 hours

## Dependencies

- TASK-004.5 completed (Open Liberty deployed)
- Jackson 2.17.0 JARs available (from TASK-004)

## Success Validation

Task is complete when:
1. ✅ At least one REST endpoint returns valid JSON
2. ✅ No Jackson-related ClassNotFoundException in logs
3. ✅ Jackson 2.17.0 confirmed in use (check logs or response headers)
4. ✅ Configuration changes documented
5. ✅ Build automation updated (if applicable)

## Notes

- This task validates that Jackson 2.17.0 upgrade (TASK-004) works correctly in Liberty runtime
- Database functionality will still be limited until TASK-005 (Database Migration) is complete
- This configuration is production-ready and follows Liberty best practices

## References

- [Open Liberty Shared Libraries](https://openliberty.io/docs/latest/class-loader-library-config.html)
- [Liberty Classloading Documentation](https://www.ibm.com/docs/en/was-liberty/base?topic=liberty-class-loaders)
- [Jackson in Liberty](https://stackoverflow.com/questions/tagged/openliberty+jackson)
