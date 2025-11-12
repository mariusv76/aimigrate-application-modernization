// main.bicep - Orchestrates core Azure resources for Customer Order Services
// NOTE: Draft scaffold. Parameters intentionally minimal; expand as implementation proceeds.
// Security: Do NOT disable purge protection; Do NOT allow public network access where private endpoints planned later.

@description('Base name prefix for resources')
param baseName string = 'customerorder'

@description('Deployment environment (dev|staging|prod)')
param environment string = 'dev'

@description('Primary location')
param location string = resourceGroup().location

@description('Enable Azure Container Apps instead of App Service (future)')
param useContainerApps bool = false

var nameSuffix = '${baseName}-${environment}'

// Resource naming
var kvName = 'kv-${baseName}-${environment}'
var appConfigName = 'appconfig-${baseName}'
var insightsName = '${baseName}-insights'
var acrName = toLower(replace('${baseName}${environment}acr','-',''))

// Module deployments
module keyVault './modules/keyvault.bicep' = {
  name: 'kvDeploy'
  params: {
    name: kvName
    location: location
  }
}

module appConfig './modules/appconfig.bicep' = {
  name: 'appConfigDeploy'
  params: {
    name: appConfigName
    location: location
  }
}

module insights './modules/insights.bicep' = {
  name: 'insightsDeploy'
  params: {
    name: insightsName
    location: location
  }
}

module acr './modules/acr.bicep' = {
  name: 'acrDeploy'
  params: {
    name: acrName
    location: location
    sku: 'Basic'
  }
}

module appService './modules/appservice.bicep' = if (!useContainerApps) {
  name: 'appServiceDeploy'
  params: {
    baseName: baseName
    environment: environment
    location: location
    planSku: 'B1'
  }
}

module containerApps './modules/containerapps.bicep' = if (useContainerApps) {
  name: 'containerAppsDeploy'
  params: {
    baseName: baseName
    environment: environment
    location: location
    acrLoginServer: acr.outputs.loginServer
    appInsightsConnectionString: insights.outputs.connectionString
    keyVaultName: kvName
    tenantId: tenant().tenantId
    appConfigEndpoint: appConfig.outputs.endpoint
    dbHost: 'psql-${nameSuffix}.postgres.database.azure.com'
    dbName: 'orderdb'
    dbUser: 'dbadmin'
  }
}

// Role assignments for Container App managed identity
module roleAssignments './modules/roleassignments.bicep' = if (useContainerApps) {
  name: 'roleAssignmentsDeploy'
  params: {
    principalId: containerApps.outputs.containerAppPrincipalId
    keyVaultName: kvName
    appConfigName: appConfigName
    acrName: acrName
    location: location
  }
  dependsOn: [
    keyVault
    appConfig
  ]
}

@description('Outputs for wiring other systems')
output keyVaultName string = kvName
output appConfigEndpoint string = appConfig.outputs.endpoint
output appInsightsConnectionString string = insights.outputs.connectionString
output acrLoginServer string = acr.outputs.loginServer
