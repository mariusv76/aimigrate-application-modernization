---
applyTo: '**'
description: Build environment configuration and commands for WebSphere to Open Liberty migration project
---

## Environment Configuration

### Java & Build Tools
- **JDK Location**: `C:\Users\mvorster\.jdk\jdk-17.0.16`
- **Maven Location**: `C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd`
- **Java Version**: 17.0.16 (OpenJDK)
- **Maven Version**: 3.9.11

### PowerShell Environment Setup
Before running any Maven or Java commands, **ALWAYS** set the JAVA_HOME:
```powershell
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
```

## Project Structure

### Working Directory
- **Workspace Root**: `C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855`
- **Parent POM**: `CustomerOrderServicesProject\pom.xml`
- **Build from**: Always run Maven commands from workspace root using `-f` flag

### Module Structure
```
monolith-websphere-855/
├── CustomerOrderServicesProject/      # Parent POM
│   └── pom.xml                        # Reactor build configuration
├── CustomerOrderServices/             # EJB module
│   └── pom.xml
├── CustomerOrderServicesWeb/          # Web module (WAR)
│   └── pom.xml
├── CustomerOrderServicesTest/         # Test module (WAR)
│   └── pom.xml
└── CustomerOrderServicesApp/          # EAR packaging
    └── pom.xml
```

## Standard Build Commands

### Full Build (from workspace root)
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install
```

### Build Without Tests
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests
```

### Compile Only
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean compile -DskipTests
```

### Run Tests
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml test
```

### Quiet Build (minimal output)
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests -q
```

## Liberty Server Commands

### Start Liberty in Dev Mode
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd liberty:dev
```

### Start Liberty Server (Direct)
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\bin
.\server.bat start defaultServer
```

### Stop Liberty Server (Direct)
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\bin
.\server.bat stop defaultServer
```

### Stop Liberty Server (Maven)
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd liberty:stop
```

### Check Liberty Server Status
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\bin
.\server.bat status defaultServer
```

### View Liberty Logs
```powershell
# Messages log
Get-Content "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\usr\servers\defaultServer\logs\messages.log" -Tail 50

# Console log
Get-Content "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\usr\servers\defaultServer\logs\console.log" -Tail 50

# Follow messages log
Get-Content "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\CustomerOrderServicesApp\target\liberty\wlp\usr\servers\defaultServer\logs\messages.log" -Wait -Tail 20
```

## Database Commands (PostgreSQL)

### Docker PostgreSQL Container
- **Container Name**: `postgres-customerorder`
- **Port**: `5432`
- **Database**: `orderdb`
- **User**: `dbuser`
- **Password**: `dbpass123`

### Check Container Status
```powershell
docker ps -a --filter name=postgres-customerorder
```

### Execute SQL Query
```powershell
docker exec postgres-customerorder psql -U dbuser -d orderdb -c "SELECT COUNT(*) FROM customer;"
```

### Execute SQL Script
```powershell
Get-Content "C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855\Common\createOrderDB-postgres.sql" | docker exec -i postgres-customerorder psql -U dbuser -d orderdb
```

### Access PostgreSQL CLI
```powershell
docker exec -it postgres-customerorder psql -U dbuser -d orderdb
```

## Common Issues & Solutions

### Issue: Maven can't find POM
**Solution**: Always run from workspace root with `-f` flag:
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml [goals]
```

### Issue: Liberty modules not found
**Cause**: Loose application XML points to outdated `.m2/repository` artifacts
**Solution**: Run full `clean install` to rebuild and update Maven repository:
```powershell
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests
```

### Issue: JNDI context errors in tests
**Expected**: Unit tests requiring JNDI context will fail outside Liberty runtime. These are integration tests that need the full application server environment.

### Issue: Build hangs or slow
**Solution**: Use filtered output with `Select-String` or quiet mode `-q`:
```powershell
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests -q
```

### Issue: Port 9080 already in use
**Check**: 
```powershell
netstat -ano | Select-String ":9080.*LISTENING"
```
**Solution**: Stop existing Liberty server or kill the process

## Build Artifacts

### Maven Repository Artifacts
After successful build, modules are installed to:
```
C:\Users\mvorster\.m2\repository\org\pwte\example\
├── CustomerOrderServices\0.1.0-SNAPSHOT\
│   └── CustomerOrderServices-0.1.0-SNAPSHOT.jar
├── CustomerOrderServicesWeb\0.1.0-SNAPSHOT\
│   └── CustomerOrderServicesWeb-0.1.0-SNAPSHOT.war
├── CustomerOrderServicesTest\0.1.0-SNAPSHOT\
│   └── CustomerOrderServicesTest-0.1.0-SNAPSHOT.war
└── CustomerOrderServicesApp\0.1.0-SNAPSHOT\
    └── CustomerOrderServicesApp-0.1.0-SNAPSHOT.ear
```

### Liberty Loose Application
Liberty uses loose application configuration at:
```
CustomerOrderServicesApp\target\liberty\wlp\usr\servers\defaultServer\apps\CustomerOrderServicesApp.ear.xml
```
This XML file references artifacts from `.m2/repository`, so **always rebuild after dependency changes**.

## Important Notes

1. **ALWAYS set JAVA_HOME** before running Maven or Java commands
2. **ALWAYS run from workspace root** using `-f CustomerOrderServicesProject\pom.xml`
3. **Run `clean install`** (not just `package`) to update `.m2/repository` artifacts
4. **Rebuild after dependency changes** to ensure Liberty picks up new artifacts
5. **Use `-DskipTests`** for faster builds when tests aren't needed
6. **PostgreSQL must be running** before starting Liberty (check with `docker ps`)

## Quick Reference Card

| Task | Command |
|------|---------|
| Full build | `mvn -f CustomerOrderServicesProject\pom.xml clean install` |
| Fast build | `mvn -f CustomerOrderServicesProject\pom.xml clean install -DskipTests -q` |
| Compile only | `mvn -f CustomerOrderServicesProject\pom.xml compile -DskipTests` |
| Run tests | `mvn -f CustomerOrderServicesProject\pom.xml test` |
| Start Liberty | `cd CustomerOrderServicesApp; mvn liberty:dev` |
| Stop Liberty | `cd CustomerOrderServicesApp; mvn liberty:stop` |
| Check DB | `docker exec postgres-customerorder psql -U dbuser -d orderdb -c "\dt"` |
| View logs | `Get-Content ...messages.log -Tail 50` |

## Before Every Build Session

```powershell
# 1. Navigate to workspace
cd C:\AIMigrate\java\appmodernization-samples\monolith-websphere-855

# 2. Set Java environment
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"

# 3. Verify database is running
docker ps --filter name=postgres-customerorder

# 4. Build the project
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests
```
