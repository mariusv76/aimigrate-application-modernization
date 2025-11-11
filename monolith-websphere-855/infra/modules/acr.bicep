@description('Container Registry name')
param name string
@description('Location')
param location string
@description('SKU (Basic, Standard, Premium)')
param sku string = 'Basic'

resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: name
  location: location
  sku: {
    name: sku
  }
  properties: {
    adminUserEnabled: false
    dataEndpointEnabled: false
    encryption: {
      status: 'enabled'
    }
    networkRuleSet: {
      defaultAction: 'Allow'
    }
    policies: {
      quarantinePolicy: {
        status: 'disabled'
      }
      trustPolicy: {
        status: 'disabled'
        type: 'Notary'
      }
      retentionPolicy: {
        status: 'disabled'
        days: 0
      }
    }
  }
}

output loginServer string = acr.properties.loginServer
