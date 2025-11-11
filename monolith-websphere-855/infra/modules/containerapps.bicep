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

var caEnvName = 'cae-${baseName}-${environment}'
var caName = 'ca-${baseName}-${environment}'

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
