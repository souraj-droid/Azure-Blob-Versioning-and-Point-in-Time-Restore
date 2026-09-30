// T258 main: storage + lifecycle + App Service (Java 21 Linux) + managed identity role.
// targetScope resourceGroup
targetScope = 'resourceGroup'

@description('Azure region.')
param location string = 'centralindia'

@description('Globally unique storage account name.')
param storageAccountName string

@description('Blob container.')
param containerName string = 'data-protection'

@description('Soft-delete days (> PIT days).')
param softDeleteDays int = 31

@description('PIT restore days (< soft-delete days).')
param pitRestoreDays int = 30

@description('Tier previous versions to Cool after N days.')
param tierCoolAfterDays int = 7

@description('Delete previous versions after N days.')
param deleteVersionAfterDays int = 31

@description('Globally unique Web App name.')
param appName string

@description('App Service Plan SKU.')
@allowed(['F1', 'B1', 'P0v3'])
param appServicePlanSku string = 'B1'

@description('Project code.')
param projectCode string = '24CC3046-P054'

@description('Team code.')
param teamCode string = 'T258'

@description('Lead student id (metadata only, never a secret).')
param studentLeadId string = '2400032992'

module storage './storage.bicep' = {
  name: 't258-storage'
  params: {
    storageAccountName: storageAccountName
    location: location
    containerName: containerName
    softDeleteDays: softDeleteDays
    pitRestoreDays: pitRestoreDays
  }
}

module lifecycle './lifecycle.bicep' = {
  name: 't258-lifecycle'
  params: {
    storageAccountName: storageAccountName
    containerName: containerName
    tierCoolAfterDays: tierCoolAfterDays
    deleteVersionAfterDays: deleteVersionAfterDays
  }
  dependsOn: [storage]
}

resource plan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: '${appName}-plan'
  location: location
  sku: {
    name: appServicePlanSku
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
  tags: {
    project: projectCode
    team: teamCode
    lead: studentLeadId
  }
}

resource web 'Microsoft.Web/sites@2023-12-01' = {
  name: appName
  location: location
  kind: 'app,linux'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: plan.id
    reserved: true
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|21-java21'
      appSettings: [
        {
          name: 'AZURE_STORAGE_ACCOUNT'
          value: storageAccountName
        }
        {
          name: 'AZURE_STORAGE_CONTAINER'
          value: containerName
        }
        {
          name: 'PIT_RESTORE_DAYS'
          value: string(pitRestoreDays)
        }
        {
          name: 'SOFT_DELETE_DAYS'
          value: string(softDeleteDays)
        }
        {
          name: 'TIER_COOL_AFTER_DAYS'
          value: string(tierCoolAfterDays)
        }
        {
          name: 'DELETE_VERSION_AFTER_DAYS'
          value: string(deleteVersionAfterDays)
        }
        {
          name: 'SCM_DO_BUILD_DURING_DEPLOYMENT'
          value: 'true'
        }
      ]
    }
  }
  tags: {
    project: projectCode
    team: teamCode
  }
}

// Storage Blob Data Owner for version restore (data plane).
// PIT restore itself needs Storage Account Contributor - grant separately to admins only.
// Note: if this fails with RoleDefinitionDoesNotExist on first deploy (AAD replication),
// re-run deploy or assign manually:
// az role assignment create --assignee <web-principalId> --role "Storage Blob Data Owner"
//   --scope $(az storage account show -n <acct> -g <rg> --query id -o tsv)
var blobDataOwnerRole = 'b7e6dc6d-f1e8-4753-8033-0f276bb0955b'

resource roleAssign 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, web.id, blobDataOwnerRole, storageAccountName)
  scope: resourceGroup()
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', blobDataOwnerRole)
    principalId: web.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

output storageAccountName string = storage.outputs.storageAccountName
output webAppName string = web.name
output webAppUrl string = 'https://${web.properties.defaultHostName}'
