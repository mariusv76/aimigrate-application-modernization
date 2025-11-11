@description('App Configuration Store name')
param name string
@description('Location')
param location string

resource appConfig 'Microsoft.AppConfiguration/configurationStores@2023-03-01' = {
  name: name
  location: location
  sku: {
    name: 'Standard'
  }
}

output endpoint string = 'https://${name}.azconfig.io'
