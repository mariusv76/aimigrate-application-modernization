@description('PostgreSQL Flexible Server name')
param serverName string

@description('Location for the PostgreSQL server')
param location string = resourceGroup().location

@description('PostgreSQL version')
param postgresVersion string = '16'

@description('Administrator username')
param administratorLogin string = 'dbadmin'

@description('Administrator password')
@secure()
param administratorPassword string

@description('Database name')
param databaseName string = 'orderdb'

@description('SKU name for the server')
param skuName string = 'Standard_B2s'

@description('SKU tier')
param skuTier string = 'Burstable'

@description('Storage size in GB')
param storageSizeGB int = 32

@description('Backup retention days')
param backupRetentionDays int = 7

@description('Enable geo-redundant backup')
param geoRedundantBackup string = 'Disabled'

@description('High availability mode')
param highAvailabilityMode string = 'Disabled'

@description('Tags for the resources')
param tags object = {}

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-03-01-preview' = {
  name: serverName
  location: location
  tags: tags
  sku: {
    name: skuName
    tier: skuTier
  }
  properties: {
    version: postgresVersion
    administratorLogin: administratorLogin
    administratorLoginPassword: administratorPassword
    storage: {
      storageSizeGB: storageSizeGB
      autoGrow: 'Enabled'
    }
    backup: {
      backupRetentionDays: backupRetentionDays
      geoRedundantBackup: geoRedundantBackup
    }
    highAvailability: {
      mode: highAvailabilityMode
    }
    network: {
      publicNetworkAccess: 'Enabled'
    }
  }
}

// Allow Azure services to access the server
resource firewallRuleAzure 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-03-01-preview' = {
  parent: postgresServer
  name: 'AllowAllAzureServicesAndResourcesWithinAzureIps'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

// Create the orderdb database
resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-03-01-preview' = {
  parent: postgresServer
  name: databaseName
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

// Enable Azure AD authentication (requires managed identity)
resource postgresServerConfig 'Microsoft.DBforPostgreSQL/flexibleServers/configurations@2023-03-01-preview' = {
  parent: postgresServer
  name: 'azure.extensions'
  properties: {
    value: 'PGCRYPTO,UUID-OSSP'
    source: 'user-override'
  }
}

output serverFqdn string = postgresServer.properties.fullyQualifiedDomainName
output serverId string = postgresServer.id
output serverName string = postgresServer.name
output databaseName string = database.name
output administratorLogin string = administratorLogin
