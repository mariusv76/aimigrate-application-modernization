# Azure Infrastructure (Bicep) - Draft

This folder contains initial Bicep templates for provisioning core Azure resources required by TASK-006.

## Modules
- `main.bicep`: Orchestrates module deployments.
- `modules/keyvault.bicep`: Secure secret storage (RBAC only, purge protection ON).
- `modules/appconfig.bicep`: Centralized configuration store.
- `modules/insights.bicep`: Application Insights component for telemetry.
- `modules/acr.bicep`: Azure Container Registry (admin disabled).
- `modules/appservice.bicep`: Linux App Service + Plan for container deployment.

## Next Steps
1. Add role assignment module for Managed Identity.
2. Add PostgreSQL Flexible Server module (future phase).
3. Parameterize image reference and integrate ACR outputs.
4. Introduce environment-specific parameter files: `parameters.dev.json`.

## Deployment (Preview)
```bash
# What-if style validation (example; ensure RG exists)
az deployment group what-if \
  --resource-group rg-customerorder-dev \
  --template-file infra/main.bicep \
  --parameters baseName=customerorder environment=dev
```

## Notes
- Do not disable Key Vault purge protection.
- Use RBAC for Key Vault/App Configuration access via managed identity.
- Adjust `linuxFxVersion` in `appservice.bicep` after ACR image is pushed.
