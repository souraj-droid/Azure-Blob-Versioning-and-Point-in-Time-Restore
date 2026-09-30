# 24CC3046-P054 — Azure Blob Versioning and Point-in-Time Restore (T258)

## 2. Team
- Project: 24CC3046-P054, Team T258, Domain: DATA PROTECTION
- Lead: 2400032992
- Members: 2400032987, 2400032887, 2400032886

## 3. Problem statement
Accidental blob overwrites cause data loss. Need a 30-day recovery window on Azure with verifiable protection.

## 4. Use cases
1. Recover from accidental blob overwrites (per-version restore).
2. Provide a 30-day recovery window (point-in-time restore).

## 5. Bottleneck / 16. Storage-cost bottleneck
Every write to a versioned blob creates a new version -> storage and cost grow.

## 6. Proposed solution / 17. Lifecycle solution
Azure-first: Blob Storage is the protection platform; Spring Boot is the demo/API layer.
- Versioning + soft delete (31d) + change feed + PITR (30d) + container soft delete.
- Lifecycle: previous versions >7d tier to Cool, >31d delete (configurable). Cost/recovery trade-off documented in docs/cost-optimization.md.

## 7. Azure architecture
See docs/architecture.md. GPv2 Standard LRS, HNS disabled, App Service Linux Java 21 + system identity.

## 8. Technologies
Java 21, Spring Boot 3.3.5, Maven, azure-storage-blob, azure-identity (DefaultAzureCredential), Bicep, HTML/CSS/JS, JUnit.

## 9. Azure prerequisites
- Subscription: Azure for Students (tested 1ea51f6c-...), `az login --use-device-code`
- Resource group + unique storage/app names. Centralindia default.

## 10. Deployment
```powershell
cd infrastructure/scripts
./deploy.ps1 -SubscriptionId 1ea51f6c-6825-4ece-9d0f-30fc3ee5dd82 -ResourceGroup rg-t258-blob -Location centralindia
./verify-azure.ps1 -ResourceGroup rg-t258-blob -StorageAccountName <acct>
```
Bash: see deploy.sh. Bicep validate: `az bicep build --file infrastructure/main.bicep`.

## 11. Local development
```powershell
az login --use-device-code
$env:AZURE_STORAGE_ACCOUNT="<acct>"; $env:AZURE_STORAGE_CONTAINER="data-protection"
cd backend; mvn spring-boot:run
# UI: http://localhost:8080
```

## 12. Authentication
DefaultAzureCredential only. No keys in code. App Service uses system identity (Blob Data Owner). PIT restore needs Storage Account Contributor (admin only, not granted to app users).

## 13. Environment configuration
See .env.example: AZURE_STORAGE_ACCOUNT, AZURE_STORAGE_CONTAINER, AZURE_SUBSCRIPTION_ID, AZURE_RESOURCE_GROUP, PIT_RESTORE_DAYS=30, SOFT_DELETE_DAYS=31, TIER_COOL_AFTER_DAYS=7, DELETE_VERSION_AFTER_DAYS=31.

## 14. API documentation
- POST /api/blobs/upload (multipart file) -> blobName, versionId
- GET /api/blobs
- GET /api/blobs/{n}/download
- GET /api/blobs/{n}/versions -> versions[{versionId,isCurrent,lastModified,contentLength}]
- GET /api/blobs/{n}/versions/{v} ; GET .../{v}/download
- POST /api/blobs/{n}/restore/{v} (copy previous -> new current)
- POST /api/restore/point-in-time {"restoreTime":"2026-09-20T14:30:00Z"} (validated; returns restoreId + CLI)
- GET /api/restore/status/{id}
- GET /api/health, GET /api/protection/status

## 15. Version recovery demo
See tests/scenarios/accidental-overwrite.md. Upload original, overwrite with incorrect data, restore V1.

## 16. PIT restore demo
See tests/scenarios/point-in-time-restore.md. App validates UTC inside 30d window; admin runs `az storage blob restore --account-name <a> --resource-group <rg> --time "<UTC>" --blob-range "data-protection/*"`.

## 17. 30-day recovery explanation
PIT=30d, soft-delete=31d (PIT must be < soft-delete). Dashboard + verify script show this. Warning: recovery before retention start unavailable. PIT covers block-blob data only, not deleted containers. Version retention is separate (lifecycle).

## 18. Testing
`cd backend; mvn test` (unit, no Azure). Integration via tests/api/*.http with real account. See docs/testing.md.

## 19. Troubleshooting
- 404 blob/version -> check names/ids via /versions.
- 400 PIT -> must be ISO-8601 UTC, not future, inside 30d.
- 500 Azure auth -> `az login`, check role, check AZURE_STORAGE_ACCOUNT.
- Bicep fail -> name not unique, or PIT>=soft-delete.

## 20. Limitations
- PIT is block-blob data only; cannot resurrect deleted containers.
- One PIT restore at a time per account.
- Change feed/PIT/lifecycle status in /protection/status are configured values; authoritative check is verify-azure.ps1.
- No Docker; Azure-first per spec.

## 21. Cleanup
`./cleanup.ps1 -ResourceGroup rg-t258-blob` or `az group delete -n rg-t258-blob --yes`.

## Files created
Backend (config/controllers/services/dtos/exceptions/util + static UI + tests), infrastructure (main/storage/lifecycle + params + 4 scripts), tests/api + scenarios, docs (5), presentation (2), README, pom files, .env.example, team.yaml.
