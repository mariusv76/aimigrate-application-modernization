# TASK-006 Authentication Strategy (Microsoft Entra ID)

## Goals
Replace legacy EJB `@RolesAllowed("SecureShopper")` approach with modern role-based authorization backed by Microsoft Entra ID (App Registration + app roles) and JWT validation. Maintain principle of least privilege.

## Identity Model
- Entra ID App Registration: `customerorder-api-dev`
- App Roles:
  - `Orders.Read` (display: Orders Reader)
  - `Orders.Write` (display: Orders Writer)
- Users or service principals assigned to app roles.
- Access tokens (OAuth2 JWT) include `roles` claim with assigned roles.

## Authorization Mapping
| Legacy Role | New App Role | Usage |
|-------------|--------------|-------|
| SecureShopper | Orders.Read | View orders, customer data |
| (Write operations previously unrestricted) | Orders.Write | Create/update orders |

EJB methods currently annotated with `@RolesAllowed("SecureShopper")` will be migrated to `@PreAuthorize` or MicroProfile JWT `@RolesAllowed` (using token roles) depending on chosen stack.

## Stack Choice
Because application runs on Open Liberty (Jakarta EE + MicroProfile), prefer MicroProfile JWT (mpJwt feature) rather than introducing Spring Security to avoid stack overlap.

### MicroProfile JWT Approach
1. Enable `mpJwt-2.1` feature (or confirm inclusion via `microProfile-6.0`).
2. Provide `microprofile-config.properties` with:
   ```properties
   mp.jwt.verify.publickey.location=${JWT_PUBLIC_KEY_URL}
   mp.jwt.verify.issuer=https://login.microsoftonline.com/${TENANT_ID}/v2.0
   mp.jwt.verify.audiences=api://${CLIENT_ID}
   ```
3. Tokens issued by Entra ID (expose an API scope or audience) must include roles claim. Liberty maps roles by default via `groups` claim—add mapper logic if roles appear under `roles`.
4. If transformation needed, implement a custom JWT group-to-role mapper extension or pre-processing filter to translate `roles` → `groups` claim before Liberty security context initialization.

### Role Annotation Migration
- Replace each EJB method annotation:
  ```java
  // Before
  @RolesAllowed("SecureShopper")
  public Order loadOrder(long id) { ... }
  // After (read-only)
  @RolesAllowed("Orders.Read")
  public Order loadOrder(long id) { ... }
  ```
- For write operations:
  ```java
  @RolesAllowed("Orders.Write")
  public void updateOrder(Order order) { ... }
  ```

### Token Acquisition (Dev)
Use Azure CLI to obtain token for testing:
```powershell
az account get-access-token --resource api://{CLIENT_ID} --query accessToken -o tsv
```
If roles missing, assign user/app to roles in Entra App Registration.

## Managed Identity vs App Registration
- App Registration handles user/service principal auth for API access.
- Managed Identity used for resource access (Key Vault, App Configuration) and not for external client API calls.

## Security Hardening
- Enforce HTTPS only (already in `appservice.bicep`).
- Validate `exp`, `nbf`, `iss`, `aud` claims.
- Reject tokens without roles claim for protected endpoints.
- Add audit logging of principal + roles for critical operations.

## Implementation Steps
1. Add MP JWT config properties file under web module: `CustomerOrderServicesWeb/WebContent/WEB-INF/classes/META-INF/microprofile-config.properties` (or create `src/main/resources/META-INF/microprofile-config.properties` structure if refactor). 
2. Introduce issuer, audience placeholders referencing environment variables (mapped via App Configuration later).
3. Migrate role annotations incrementally (commit per group of classes).
4. Add a startup diagnostics endpoint `/auth/info` (secured) returning caller roles for verification.
5. Provide documentation snippet in Task 006 progress.

## Open Questions
- Are write operations currently secured or only reads? Need business logic review.
- Should we split roles for customer vs order vs inventory? Future refinement.

## Next Actions
- Confirm MP JWT feature presence (microProfile-6.0 should include it; verify).
- Add microprofile-config skeleton with issuer/audience placeholders.
- Inventory EJB classes with `@RolesAllowed` to plan migration.
