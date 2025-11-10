# Migration Task 005 Report: DB2 → PostgreSQL & JPA Provider Reversion

## Overview

Task-005 migrated the application persistence layer from DB2 to PostgreSQL and ensured the `/api/customers/business` endpoint returned JSON business customer data. A temporary exploration of switching the JPA provider from EclipseLink (Liberty default) to Hibernate was performed, then reverted to maintain stability.

## Final State

- Database: PostgreSQL 16 (Docker container `postgres-customerorder`)
- Schema: 12 tables created via `Common/createOrderDB-postgres.sql`
- Data: Sample business & residential customers loaded; business customer id=1 visible via endpoint
- JPA Provider: EclipseLink (default, no explicit `<provider>` in `persistence.xml`)
- Datasource: `jdbc/orderds` configured in `Deployment/server.xml` using PostgreSQL driver `postgresql-42.7.1.jar` placed in Liberty `resources/`
- Security: `basicRegistry` enabled to satisfy EJB/security requirements
- Endpoint: `GET /CustomerOrderServicesWeb/api/customers/business` returns list of `BusinessCustomer` entities (verified post-revert)

## Key Files

| Purpose | File |
|---------|------|
| Datasource & features | `Deployment/server.xml` |
| Bootstrap DB properties | `Deployment/bootstrap.properties` |
| Persistence unit config | `CustomerOrderServices/ejbModule/META-INF/persistence.xml` |
| REST resource | `CustomerOrderServicesWeb/src/org/pwte/example/api/CustomerResource.java` |

## Migration Steps Summary

1. Added PostgreSQL driver dependency (Maven) and copied jar to Liberty resources.
2. Converted DB2 DDL/DML scripts to PostgreSQL equivalents (`*-postgres.sql`).
3. Started Docker PostgreSQL container and executed schema + sample data scripts.
4. Configured Liberty `server.xml` with PostgreSQL `dataSource` (removed DB2 definition).
5. Verified endpoint initially returned count placeholder; updated `CustomerResource` to return entity list.
6. Attempted switch to Hibernate (added dependency, provider, dialect) – encountered datasource binding errors, provider resolution issues, and OpenAPI scanner `StackOverflowError`.
7. Reverted to EclipseLink: removed Hibernate dependency and `<provider>` entry; simplified persistence properties; removed EAR lib bundling.
8. Restored stable startup and verified endpoint returns JSON business customer.
9. Removed temporary diagnostic artifacts: `BusinessCustomerDTO`, `HeaderDebugFilter`, `Ping2Resource`.

## Rationale for Reverting Hibernate

- Liberty already supplies a tested, integrated EclipseLink implementation (Jakarta EE 10 alignment).
- Hibernate introduction caused classloading/provider resolution issues and OpenAPI scanner recursion without clear functional benefit.
- Performance or feature gains not required for current domain model; simplicity and stability prioritized.

## Lessons Learned

- Introduce provider changes early or not at all for migration tasks—late changes risk destabilizing validated layers.
- Always confirm driver jar presence matches `library` configuration to avoid JNDI datasource failures.
- Keep persistence.xml minimal; rely on container defaults unless explicit tuning is required.

## Verification Checklist

| Item | Status |
|------|--------|
| PostgreSQL container running | ✔ |
| Driver jar present in Liberty resources | ✔ |
| Datasource bound (`jdbc/orderds`) | ✔ (no startup errors) |
| Endpoint returns business customer JSON | ✔ |
| No Hibernate artifacts remaining | ✔ |
| Diagnostic classes removed | ✔ |

## Suggested Next Steps

1. Add automated test (unit-level) for `CustomerResource` mocking `CustomerOrderServices` to assert JSON serialization of business customers.
2. Consider integration test profile that runs against a live Liberty dev mode for future CI flows.
3. Monitor RUs / performance (if moved to managed Postgres) and add health check for datasource in readiness endpoint.
4. Evaluate need for pagination if business customers list grows.

## Command Reference (PostgreSQL & Build)

```powershell
# Build (skip tests)
$env:JAVA_HOME="C:\Users\mvorster\.jdk\jdk-17.0.16"
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd -f CustomerOrderServicesProject\pom.xml clean install -DskipTests -q

# Start Liberty dev mode
cd CustomerOrderServicesApp
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd liberty:dev

# PostgreSQL container check
docker ps --filter name=postgres-customerorder

# Endpoint test
Invoke-WebRequest -Uri http://host.docker.internal:9080/CustomerOrderServicesWeb/api/customers/business -UseBasicParsing | Select-Object -ExpandProperty Content
```

## Decision Log

| Decision | Date | Notes |
|----------|------|-------|
| Use PostgreSQL over DB2 | Initial | Align with modernization & open-source ecosystem |
| Attempt Hibernate provider | Later | Evaluated feasibility; encountered runtime issues |
| Revert to EclipseLink | Final | Stability > potential Hibernate features |
| Remove diagnostics | Final | Clean codebase for handoff |

---
Report generated on 2025-11-10.
