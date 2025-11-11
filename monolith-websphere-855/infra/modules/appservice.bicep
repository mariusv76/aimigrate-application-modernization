@description('Base name')
param baseName string
@description('Environment')
param environment string
@description('Location')
param location string
@description('App Service Plan SKU (B1/S1/P1v3)')
param planSku string = 'B1'

var planName = 'asp-${baseName}-${environment}'
var appName = 'app-${baseName}-${environment}'

resource plan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: planName
  location: location
  sku: {
    name: planSku
    tier: planSku == 'B1' ? 'Basic' : 'Standard'
    size: planSku
  }
  properties: {
    reserved: true // Linux
  }
}

resource site 'Microsoft.Web/sites@2022-09-01' = {
  name: appName
  location: location
  properties: {
    serverFarmId: plan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'DOCKER|placeholder/acr/image:tag'
      alwaysOn: true
      appSettings: [
        {
          name: 'WEBSITES_PORT'
          value: '9080'
        }
        {
          name: 'JAVA_TOOL_OPTIONS'
          value: '-javaagent:/home/site/wwwroot/otel-javaagent.jar'
        }
      ]
    }
  }
}

output appServiceName string = site.name
