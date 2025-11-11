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
  }
}

output loginServer string = acr.properties.loginServer
