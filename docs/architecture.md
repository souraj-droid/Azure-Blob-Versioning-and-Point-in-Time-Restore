# Architecture

```
Browser dashboard (static/)
   -> Spring Boot API (Java 21, /api/*)
   -> Azure Blob Storage via DefaultAzureCredential
   -> Storage Account (GPv2, Std LRS, HNS disabled)
       |- Versioning, Soft delete 31d, Change feed, PITR 30d
       |- Container soft delete 7d
       |- Lifecycle: tier>7d Cool, delete>31d
   -> App Service Linux JAVA|21 + system identity (Blob Data Owner)
```

PIT restore (Restore Blob Ranges) is an admin operation requiring Storage Account Contributor.
App validates PIT requests; admin executes `az storage blob restore`.
Version restore (copy previous -> current) is a normal data operation.
