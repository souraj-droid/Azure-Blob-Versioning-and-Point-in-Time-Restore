# Scenario B - Point-in-time restore

Pre-req: versioning + soft delete (31d) + change feed enabled; PIT 30d.

1. Create changes:
   - 10:00 report.txt -> Version A
   - 10:05 report.txt -> Version B
   - 10:10 report.txt -> Version C
   - 10:15 report.txt -> Version D
2. Note restore point: 10:05 UTC (must be ISO-8601 UTC, inside 30-day window, not future).
3. App: `POST /api/restore/point-in-time {"restoreTime":"...T10:05:00Z"}` -> returns restoreId + status VALIDATED + CLI command.
4. Admin runs real restore:
   `az storage blob restore --account-name <acct> --resource-group <rg> --time "<T10:05Z>" --blob-range "data-protection/*"`
5. Only one restore at a time per account (Azure limit). Show restoreId/time/status.
6. Verify blob state matches 10:05.

Note: PIT restores block-blob data only; it does not resurrect deleted containers.
