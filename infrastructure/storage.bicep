// T258 storage account: GPv2, Std LRS, HNS disabled, versioning + soft-delete + changefeed + PITR.
@description('Globally unique storage account name (3-24 lowercase alphanumerics).')
param storageAccountName string

@description('Azure region.')
param location string

@description('Blob container name.')
param containerName string = 'data-protection'

@description('Blob soft-delete retention days. Must be > pitRestoreDays.')
@minValue(2)
@maxValue(365)
param softDeleteDays int = 31

@description('PIT restore retention days. Must be < softDeleteDays.')
@minValue(1)
@maxValue(364)
param pitRestoreDays int = 30

resource st 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowBlobPublicAccess: false
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
    isHnsEnabled: false
  }
}

resource blobSvc 'Microsoft.Storage/storageAccounts/blobServices@2023-05-01' = {
  parent: st
  name: 'default'
  properties: {
    changeFeed: {
      enabled: true
    }
    deleteRetentionPolicy: {
      enabled: true
      days: softDeleteDays
    }
    containerDeleteRetentionPolicy: {
      enabled: true
      days: 7
    }
    isVersioningEnabled: true
    restorePolicy: {
      enabled: true
      days: pitRestoreDays
    }
  }
}

resource container 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  parent: blobSvc
  name: containerName
  properties: {
    publicAccess: 'None'
  }
}

output storageAccountId string = st.id
output storageAccountName string = st.name
