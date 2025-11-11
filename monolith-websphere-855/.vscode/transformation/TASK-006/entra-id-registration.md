# Entra ID App Registration Details

## Application Information
- **Display Name**: customerorder-api-dev
- **Application (Client) ID**: 3d3c655f-43b5-4130-816c-74493287e79b
- **Object ID**: 38606167-db0f-46eb-8808-e3e5592c8c3f
- **Tenant ID**: 89cea9ee-73b0-4f74-9541-b8153dff5960
- **Application ID URI**: api://3d3c655f-43b5-4130-816c-74493287e79b

## App Roles
1. **Orders.Read**
   - Description: Read customer orders
   - Value: Orders.Read
   - Allowed Member Types: User, Application

2. **Orders.Write**
   - Description: Create and modify customer orders
   - Value: Orders.Write
   - Allowed Member Types: User, Application

## Token Configuration
- **Issuer**: https://login.microsoftonline.com/89cea9ee-73b0-4f74-9541-b8153dff5960/v2.0
- **Audience**: api://3d3c655f-43b5-4130-816c-74493287e79b
- **JWKS Endpoint**: https://login.microsoftonline.com/89cea9ee-73b0-4f74-9541-b8153dff5960/discovery/v2.0/keys

## Usage

### MicroProfile JWT Configuration
The application uses MicroProfile JWT (mpJwt-2.1) for token validation. Configuration is in:
- `CustomerOrderServicesWeb/WebContent/WEB-INF/classes/META-INF/microprofile-config.properties`

### Role-Based Access Control
Use `@RolesAllowed` annotations with app role values:
```java
@RolesAllowed("Orders.Read")
public Order getOrder(long orderId) { ... }

@RolesAllowed("Orders.Write")
public Order createOrder(Order order) { ... }
```

### Token Acquisition (for testing)
```bash
# Get access token via OAuth2 client credentials flow
az account get-access-token --resource api://3d3c655f-43b5-4130-816c-74493287e79b
```

## Next Steps
1. Assign app roles to users/service principals via Azure Portal or CLI
2. Update `@RolesAllowed("SecureShopper")` annotations to use `Orders.Read`/`Orders.Write`
3. Test JWT validation with Entra ID tokens
