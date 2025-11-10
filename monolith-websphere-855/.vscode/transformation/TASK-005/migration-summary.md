# TASK-005 Migration Summary (DB2 → PostgreSQL + Jakarta Runtime Validation)

**Date:** 2025-11-07  
**Branch:** `migration/task-005-database-migration`  
**Scope:** Convert persistence layer from DB2/OpenJPA legacy to PostgreSQL + Jakarta EE 10 (Liberty), validate runtime via minimal REST endpoint.

---
## 1. Accomplishments
- Planned full migration (extensive 500+ line execution plan).
- Added PostgreSQL driver (42.7.1) and integrated Docker PostgreSQL (`postgres-customerorder`).
- Converted schema: identity → SERIAL, CLOB → TEXT, safe IF EXISTS for drops.
- Loaded baseline sample data (business + residential customers).
- Updated `persistence.xml` to Jakarta 3.0 namespace with entities and JPA properties.
- Migrated REST layer to Jakarta (`jakarta.ws.rs`), added `RestApplication` with `@ApplicationPath("/api")`.
- Added `CustomerResource` for quick JPA validation.
- Removed legacy `CustomerServicesApp` (javax + provider bootstrap) to eliminate classloading conflicts.
- Switched JSON provider to Jakarta variant (`jackson-jakarta-rs-json-provider`).
- Packaged EAR (disabled loose deployment) and achieved stable Liberty startup.

---
## 2. Key Changes
| Area | Before | After |
|------|--------|-------|
| JPA Provider | OpenJPA (implicit) | Attempted Hibernate → fallback EclipseLink (default Liberty) |
| Database | DB2 scripts | PostgreSQL scripts (new naming convention) |
| REST Bootstrapping | Custom javax Application | Simple Jakarta `Application` subclass + annotation |
| JSON Provider | Legacy Jackson javax | Jakarta RS Jackson provider |
| Deployment | Loose config w/ stale artifacts | Packaged EAR (fresh rebuild) |

---
## 3. Troubleshooting Journey
### 3.1 Initial REST Failures
- 404 due to incorrect path when `@ApplicationPath` changed and old URLs used.
- `ClassNotFoundException javax.ws.rs.ext.MessageBodyReader` traced to legacy bootstrap class referencing javax provider.

### 3.2 Provider & Persistence Issues
- Hibernate provider not found (`ClassNotFound` for `org.hibernate.jpa.HibernatePersistenceProvider`) because `hibernate-core` alone insufficient and marked `provided`; missing transitive libs.
- Removed `<provider>` to use Liberty default (EclipseLink) as interim fix.

### 3.3 Datasource Resolution Failure
- JPA predeployment error: `CWWJP0013E` — JNDI `jdbc/orderds` not found. Indicates missing or mismatched datasource definition in `server.xml` (Liberty configuration not yet aligned with persistence unit).
- EM injection failed → REST endpoint returned 500, followed by header rendering error from Resteasy when writing exception (`Invalid LF not followed by whitespace`).

### 3.4 Rebuild/Deployment Friction
- `maven-clean-plugin` deletion failures due to active Liberty server locking `workarea` — resolved by stopping server and manually clearing directory.

---
## 4. Current Blockers
| Blocker | Impact | Resolution Path |
|---------|--------|-----------------|
| Missing Liberty datasource (`jdbc/orderds`) | Persistence unit cannot initialize, REST returns 500 | Patch `Deployment/server.xml` with proper `<dataSource>` + driver library |
| Hibernate packaging incomplete | Cannot use Hibernate-specific features | Either fully include required Hibernate dependencies or remain on EclipseLink |
| Legacy test harness (`wink-client`, custom Application) | Noise & outdated patterns | Defer modernization or mark tests skipped |

---
## 5. Recommended Next Steps
1. Inspect `Deployment/server.xml` and add:
   ```xml
   <library id="postgresLib">
     <fileset dir="${server.config.dir}/resources" includes="postgresql-42.7.1.jar"/>
   </library>
   <dataSource id="orderds" jndiName="jdbc/orderds" type="javax.sql.DataSource">
     <jdbcDriver libraryRef="postgresLib"/>
     <properties databaseName="orderdb" serverName="localhost" portNumber="5432" user="dbuser" password="dbpass123"/>
   </dataSource>
   ```
   Then copy the driver into `usr/servers/defaultServer/resources/`.
2. Rebuild EAR; restart Liberty; verify JPA initialization success (`messages.log`).
3. Retest `GET /CustomerOrderServicesWeb/api/customers/business` expecting 200 + JSON list.
4. Optional: Restore Hibernate by adding its full dependency set (jboss-logging, bytecode libs) without `provided` scope.
5. Add a MicroProfile Health check to confirm datasource connectivity. 
6. Update documentation removing OpenJPA references; mark provider strategy decision.

---
## 6. Validation Criteria (Pending)
- REST endpoint returns HTTP 200 and JSON array of ≤10 business customers.
- Log shows successful PU deployment (no `CWWJP0013E`).
- No classloader/provider exceptions during server startup.

---
## 7. Lessons Learned
- Removing legacy bootstrap classes early reduces mixed namespace conflicts.
- Hibernate on Liberty requires full dependency packaging—`hibernate-core` alone is insufficient.
- Always stop Liberty before `mvn clean` to avoid locked `workarea` artifacts.
- Datasource JNDI mismatch is a primary silent blocker for JPA injection.

---
## 8. Open Decisions
| Decision | Status | Notes |
|----------|--------|-------|
| JPA provider (EclipseLink vs Hibernate) | Pending | EclipseLink works out-of-box; Hibernate needs full libs |
| Test modernization (wink → REST Assured integration) | Deferred | Focus first on datasource fix |
| Additional endpoints for health/exploration | Planned | After datasource success |

---
## 9. Files Touched (Representative)
- `CustomerOrderServices/ejbModule/META-INF/persistence.xml`
- `CustomerOrderServices/pom.xml`
- `CustomerOrderServicesWeb/pom.xml`
- `CustomerOrderServicesWeb/src/org/pwte/example/api/CustomerResource.java`
- Legacy removal: `CustomerOrderServicesWeb/src/org/pwte/example/app/CustomerServicesApp.java` (neutralized)

---
## 10. Action Snapshot (Next Session Entry Point)
Perform datasource configuration patch → rebuild → start Liberty → retest business customers endpoint.

---
**End of Summary**
