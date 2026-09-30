# Azure configuration

- Kind: StorageV2, Performance: Standard, Replication: LRS (demo)
- HNS: Disabled (PITR unsupported with HNS)
- Blob versioning: Enabled
- Blob soft delete: Enabled, 31 days
- Container soft delete: Enabled, 7 days
- Change feed: Enabled
- PIT restore: Enabled, 30 days (must be < soft-delete 31d)
- Lifecycle: tier versions >7d to Cool; delete versions >31d (configurable via Bicep params)
- App Service: Linux, JAVA|21-java21, B1, system identity + Blob Data Owner role
- Auth: DefaultAzureCredential only, no keys in code. Local: `az login`.

Verify: `infrastructure/scripts/verify-azure.ps1 -ResourceGroup <rg> -StorageAccountName <acct>`
