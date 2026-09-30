// T258 lifecycle policy: cost control for previous versions (configurable thresholds).
@description('Existing storage account name.')
param storageAccountName string

@description('Container prefix to apply policy to.')
param containerName string = 'data-protection'

@description('Tier previous versions to Cool after N days.')
@minValue(0)
@maxValue(365)
param tierCoolAfterDays int = 7

@description('Delete previous versions after N days. Cost/recovery trade-off - adjust per policy.')
@minValue(1)
@maxValue(365)
param deleteVersionAfterDays int = 31

resource st 'Microsoft.Storage/storageAccounts@2023-05-01' existing = {
  name: storageAccountName
}

resource policy 'Microsoft.Storage/storageAccounts/managementPolicies@2023-05-01' = {
  parent: st
  name: 'default'
  properties: {
    policy: {
      rules: [
        {
          enabled: true
          name: 't258-tier-old-versions'
          type: 'Lifecycle'
          definition: {
            actions: {
              version: {
                tierToCool: {
                  daysAfterCreationGreaterThan: tierCoolAfterDays
                }
              }
            }
            filters: {
              blobTypes: [
                'blockBlob'
              ]
              prefixMatch: [
                '${containerName}/'
              ]
            }
          }
        }
        {
          enabled: true
          name: 't258-delete-old-versions'
          type: 'Lifecycle'
          definition: {
            actions: {
              version: {
                delete: {
                  daysAfterCreationGreaterThan: deleteVersionAfterDays
                }
              }
            }
            filters: {
              blobTypes: [
                'blockBlob'
              ]
              prefixMatch: [
                '${containerName}/'
              ]
            }
          }
        }
      ]
    }
  }
}
