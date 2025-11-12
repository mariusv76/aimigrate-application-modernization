@description('Base name for resources')
param baseName string
@description('Environment (dev|staging|prod)')
param environment string
@description('Location')
param location string
@description('ACR login server')
param acrLoginServer string
@description('Application Insights connection string')
@secure()
param appInsightsConnectionString string
@description('Key Vault name for secret references')
param keyVaultName string
@description('Azure Tenant ID')
param tenantId string
@description('App Configuration endpoint')
param appConfigEndpoint string
@description('PostgreSQL server hostname')
param dbHost string
@description('PostgreSQL database name')
param dbName string = 'orderdb'
@description('PostgreSQL admin username')
param dbUser string = 'dbadmin'

var caEnvName = 'cae-${baseName}-${environment}'
var caName = 'ca-${baseName}-${environment}'
var keyVaultUrl = 'https://${keyVaultName}${az.environment().suffixes.keyvaultDns}/'

// Container Apps Environment
resource containerAppEnv 'Microsoft.App/managedEnvironments@2023-05-01' = {
  name: caEnvName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'azure-monitor'
    }
    daprAIConnectionString: appInsightsConnectionString
  }
}

// Container App with managed identity
resource containerApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: caName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    managedEnvironmentId: containerAppEnv.id
    configuration: {
      ingress: {
        external: true
        targetPort: 9080
        transport: 'http'
        allowInsecure: false
      }
      registries: [
        {
          server: acrLoginServer
          identity: 'system'
        }
      ]
      secrets: [
        {
          name: 'app-insights-connection-string'
          value: appInsightsConnectionString
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'customerorder-api'
          image: '${acrLoginServer}/customerorder-api:latest'
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: [
            {
              name: 'WEBSITES_PORT'
              value: '9080'
            }
            {
              name: 'JAVA_TOOL_OPTIONS'
              value: '-javaagent:/opt/otel/opentelemetry-javaagent.jar'
            }
            {
              name: 'OTEL_SERVICE_NAME'
              value: 'customerorder-api'
            }
            {
              name: 'OTEL_RESOURCE_ATTRIBUTES'
              value: 'deployment.environment=${environment}'
            }
            {
              name: 'AZURE_KEYVAULT_ENDPOINT'
              value: keyVaultUrl
            }
            {
              name: 'AZURE_APPCONFIGURATION_ENDPOINT'
              value: appConfigEndpoint
            }
            {
              name: 'AZURE_TENANT_ID'
              value: tenantId
            }
            {
              name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
              secretRef: 'app-insights-connection-string'
            }
            {
              name: 'DB_HOST'
              value: dbHost
            }
            {
              name: 'DB_PORT'
              value: '5432'
            }
            {
              name: 'DB_NAME'
              value: dbName
            }
            {
              name: 'DB_USER'
              value: dbUser
            }
            {
              name: 'DB_PASSWORD'
              value: 'placeholder-will-be-updated'
            }
            {
              name: 'OTEL_TRACES_SAMPLER'
              value: 'parentbased_traceidratio'
            }
            {
              name: 'OTEL_TRACES_SAMPLER_ARG'
              value: '0.1'
            }
          ]
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 3
      }
    }
  }
}

output containerAppUrl string = 'https://${containerApp.properties.configuration.ingress.fqdn}'
output containerAppPrincipalId string = containerApp.identity.principalId
output containerAppName string = containerApp.name
