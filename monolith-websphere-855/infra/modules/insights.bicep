@description('Application Insights component name')
param name string
@description('Location')
param location string

resource insights 'Microsoft.Insights/components@2020-02-02' = {
  name: name
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    Flow_Type: 'Bluefield'
    Request_Source: 'Azure'
  }
}

output connectionString string = insights.properties.ConnectionString
