@description('Principal ID (Managed Identity) to assign roles to')
param principalId string
@description('Key Vault name for scope')
param keyVaultName string
@description('App Configuration name for scope')
param appConfigName string
@description('ACR name for scope')
param acrName string
@description('Location')
param location string

// NOTE: This module expects resources already exist; supply their names.
// Uses latest role definition IDs for RBAC at resource scope.
// Role Definitions:
// Key Vault Secrets User: 4633458b-17de-408a-b874-1327992a3a45
// App Configuration Data Reader: 516239f1-63e1-4d78-a4de-a74fb236a071
// AcrPull: 7f951dda-4ed3-4680-a7ca-43fe172d538d

resource kv 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}
resource appConfig 'Microsoft.AppConfiguration/configurationStores@2023-03-01' existing = {
  name: appConfigName
}
resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' existing = {
  name: acrName
}

resource kvRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(keyVaultName, principalId, 'kv-secrets-user')
  scope: kv
  properties: {
    principalId: principalId
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-1327992a3a45')
    principalType: 'ServicePrincipal'
  }
}

resource appConfigRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(appConfigName, principalId, 'appconfig-data-reader')
  scope: appConfig
  properties: {
    principalId: principalId
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '516239f1-63e1-4d78-a4de-a74fb236a071')
    principalType: 'ServicePrincipal'
  }
}

resource acrRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(acrName, principalId, 'acr-pull')
  scope: acr
  properties: {
    principalId: principalId
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '7f951dda-4ed3-4680-a7ca-43fe172d538d')
    principalType: 'ServicePrincipal'
  }
}

output kvRoleId string = kvRole.name
output appConfigRoleId string = appConfigRole.name
output acrRoleId string = acrRole.name
