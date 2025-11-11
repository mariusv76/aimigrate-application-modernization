# Deployment Agent Input Specification for TASK-006

## Flow Context
**Intake Agent → Design Agent → Deployment Agent (Bicep Generation Only) → Transformation Agent (Consumes Outputs)**

This document specifies the required inputs/outputs contract between the Deployment Agent (which generates Bicep files) and the Transformation Agent (which integrates them into the application).

## Required Inputs TO Deployment Agent

### 1. Repository Metadata
```json
{
  "repo": {
    "owner": "mariusv76",
    "name": "aimigrate-application-modernization",
    "branch": "migration/task-006-azure-integration",
    "infraPath": "infra/",
    "modulesPath": "infra/modules/"
  }
}
```

### 2. Azure Target Environment
```json
{
  "azure": {
    "subscriptionId": "<azure-subscription-id>",
    "resourceGroup": "rg-customerorder-dev",
    "location": "eastus",
    "createResourceGroup": true
  }
}
```

### 3. Application Context
```json
{
  "application": {
    "name": "customerorder-api",
    "baseName": "customerorder",
    "environment": "dev",
    "runtime": "openliberty",
    "runtimeVersion": "24.0.0.11",
    "javaVersion": "17",
    "containerPort": 9080,
    "healthEndpoint": "/health",
    "readinessEndpoint": "/health/ready",
    "livenessEndpoint": "/health/live"
  }
}
```

### 4. Required Azure Resources
```json
{
  "resources": [
    {
      "type": "Microsoft.KeyVault/vaults",
      "name": "kv-customerorder-dev",
      "sku": "standard",
      "properties": {
        "enablePurgeProtection": true,
        "enableSoftDelete": true,
        "enableRbacAuthorization": true
      },
      "secrets": [
        "db-password",
        "entra-client-secret"
      ]
    },
    {
      "type": "Microsoft.AppConfiguration/configurationStores",
      "name": "appconfig-customerorder",
      "sku": "Standard",
      "configKeys": [
        "db:host",
        "db:port",
        "db:name",
        "telemetry:sampling",
        "feature:dynamicConfig"
      ]
    },
    {
      "type": "Microsoft.Insights/components",
      "name": "customerorder-insights",
      "kind": "web",
      "applicationType": "web"
    },
    {
      "type": "Microsoft.ContainerRegistry/registries",
      "name": "acrcustomerorderdev",
      "sku": "Basic",
      "properties": {
        "adminUserEnabled": false
      }
    },
    {
      "type": "Microsoft.Web/serverfarms",
      "name": "asp-customerorder-dev",
      "sku": {
        "name": "B1",
        "tier": "Basic"
      },
      "properties": {
        "reserved": true
      }
    },
    {
      "type": "Microsoft.Web/sites",
      "name": "app-customerorder-dev",
      "properties": {
        "httpsOnly": true,
        "linuxFxVersion": "DOCKER|<acrName>.azurecr.io/customerorder-api:latest",
        "appSettings": [
          {
            "name": "WEBSITES_PORT",
            "value": "9080"
          },
          {
            "name": "JAVA_TOOL_OPTIONS",
            "value": "-javaagent:/opt/otel/opentelemetry-javaagent.jar"
          },
          {
            "name": "OTEL_SERVICE_NAME",
            "value": "customerorder-api"
          },
          {
            "name": "OTEL_RESOURCE_ATTRIBUTES",
            "value": "deployment.environment=dev,service.namespace=customerorder"
          }
        ]
      }
    }
  ]
}
```

### 5. Identity & RBAC Requirements
```json
{
  "identity": {
    "type": "SystemAssigned",
    "roleAssignments": [
      {
        "scope": "keyVault",
        "role": "Key Vault Secrets User",
        "roleDefinitionId": "4633458b-17de-408a-b874-1327992a3a45"
      },
      {
        "scope": "appConfiguration",
        "role": "App Configuration Data Reader",
        "roleDefinitionId": "516239f1-63e1-4d78-a4de-a74fb236a071"
      },
      {
        "scope": "containerRegistry",
        "role": "AcrPull",
        "roleDefinitionId": "7f951dda-4ed3-4680-a7ca-43fe172d538d"
      }
    ]
  }
}
```

### 6. OpenTelemetry Configuration
```json
{
  "observability": {
    "enabled": true,
    "otelAgent": {
      "version": "2.1.0",
      "imagePath": "/opt/otel/opentelemetry-javaagent.jar"
    },
    "sampling": {
      "type": "parentbased_traceidratio",
      "ratio": 0.1
    },
    "exporters": [
      "otlp",
      "azure-monitor"
    ]
  }
}
```

### 7. Security Requirements
```json
{
  "security": {
    "authentication": {
      "provider": "EntraID",
      "issuer": "https://login.microsoftonline.com/${TENANT_ID}/v2.0",
      "audience": "api://${CLIENT_ID}",
      "jwksUri": "https://login.microsoftonline.com/${TENANT_ID}/discovery/v2.0/keys"
    },
    "tls": {
      "minVersion": "1.2",
      "httpsOnly": true
    }
  }
}
```

### 8. Tags & Governance
```json
{
  "tags": {
    "environment": "dev",
    "application": "customerorder",
    "costCenter": "engineering",
    "owner": "platform-team",
    "managedBy": "bicep"
  }
}
```

## Required Outputs FROM Deployment Agent

### 1. Generated Bicep Files Structure
```
infra/
├── main.bicep                    # Orchestrator
├── parameters.dev.json           # Environment-specific params
├── modules/
│   ├── keyvault.bicep
│   ├── appconfig.bicep
│   ├── insights.bicep
│   ├── acr.bicep
│   ├── appservice.bicep
│   └── roleassignments.bicep
└── README.md                     # Deployment instructions
```

### 2. Bicep Outputs Contract
The generated `main.bicep` MUST expose these outputs:

```bicep
output keyVaultName string
output keyVaultUri string
output appConfigEndpoint string
output appInsightsConnectionString string
output appInsightsInstrumentationKey string
output acrLoginServer string
output acrName string
output appServiceName string
output appServiceUrl string
output appServicePrincipalId string  // System-assigned managed identity
```

### 3. Parameter File Template
```json
{
  "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentParameters.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "baseName": {
      "value": "customerorder"
    },
    "environment": {
      "value": "dev"
    },
    "location": {
      "value": "eastus"
    },
    "useContainerApps": {
      "value": false
    }
  }
}
```

### 4. Resource Naming Manifest
Deployment Agent should generate a mapping file showing resource name resolution:

```json
{
  "resourceNames": {
    "keyVault": "kv-customerorder-dev",
    "appConfiguration": "appconfig-customerorder",
    "applicationInsights": "customerorder-insights",
    "containerRegistry": "acrcustomerorderdev",
    "appServicePlan": "asp-customerorder-dev",
    "appService": "app-customerorder-dev"
  }
}
```

### 5. Configuration Mapping Document
```json
{
  "configurationSources": {
    "keyVaultSecrets": {
      "db-password": {
        "type": "secret",
        "consumedBy": "datasource",
        "microprofileConfigKey": "secret.db-password"
      },
      "entra-client-secret": {
        "type": "secret",
        "consumedBy": "authentication",
        "microprofileConfigKey": "secret.entra-client-secret"
      }
    },
    "appConfigurationKeys": {
      "db:host": {
        "type": "config",
        "defaultValue": "localhost",
        "microprofileConfigKey": "DB_HOST"
      },
      "db:port": {
        "type": "config",
        "defaultValue": "5432",
        "microprofileConfigKey": "DB_PORT"
      },
      "db:name": {
        "type": "config",
        "defaultValue": "orderdb",
        "microprofileConfigKey": "DB_NAME"
      }
    },
    "environmentVariables": {
      "APPLICATIONINSIGHTS_CONNECTION_STRING": {
        "source": "bicep-output",
        "outputName": "appInsightsConnectionString"
      },
      "TENANT_ID": {
        "source": "manual",
        "description": "Entra ID tenant ID for JWT validation"
      },
      "CLIENT_ID": {
        "source": "manual",
        "description": "Entra ID app registration client ID"
      }
    }
  }
}
```

## Transformation Agent Consumption

Once Deployment Agent provides the above outputs, Transformation Agent will:

1. **Integrate Bicep files** into repository at `infra/` path
2. **Update application configuration**:
   - Replace placeholders in `microprofile-config.properties` with output references
   - Add Azure SDK dependencies for Key Vault + App Configuration access
3. **Create ConfigSource implementations**:
   - `KeyVaultConfigSource` for secret resolution
   - `AppConfigurationConfigSource` for dynamic config
4. **Update Docker image**:
   - Add OpenTelemetry agent download stage
   - Configure agent path and permissions
5. **Update deployment configuration**:
   - Wire Bicep outputs → App Service app settings
   - Configure managed identity bindings
6. **Generate deployment pipeline**:
   - Build container → Push to ACR → Update App Service
7. **Update documentation**:
   - Deployment runbook
   - Configuration guide
   - Troubleshooting

## Validation Checklist

Before Transformation Agent proceeds, verify Deployment Agent provided:
- [ ] All Bicep files compile without errors (`az bicep build`)
- [ ] Parameter file matches expected schema
- [ ] All required outputs are declared in `main.bicep`
- [ ] Resource naming follows conventions (no conflicts)
- [ ] Role assignments reference correct principalId placeholder
- [ ] Configuration mapping includes all secrets and config keys
- [ ] Tags are applied to all resources

## Future Enhancements

Optional inputs for future phases:
- PostgreSQL Flexible Server configuration
- Private endpoint specifications
- Container Apps alternative configuration
- Network security group rules
- Diagnostic settings (Log Analytics workspace)
- Custom domain bindings
- Scale settings (autoscale rules)

---

**Status**: Draft specification for Deployment Agent input contract
**Last Updated**: 2025-11-11
**Related Documents**: `plan.md`, `secrets-strategy.md`, `auth-strategy.md`, `observability-strategy.md`
