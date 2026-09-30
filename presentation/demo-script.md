# Demo script (5-10 min)

1. Show `/api/protection/status` + verify-azure.ps1 output (versioning/soft-delete/changefeed/PIT 30d/lifecycle).
2. Upload report.txt: "This is the original project report." (V1)
3. Overwrite: "This report contains incorrect data." (V2 current)
4. Show versions (V1 previous, V2 current).
5. Restore V1 -> new current V3; download to prove "original" back.
6. Make timestamped edits A/B/C/D; pick 10:05 UTC; POST /api/restore/point-in-time; show restoreId + CLI.
7. (Admin) run `az storage blob restore ...`; show restored state.
8. Show 30-day config + warning + lifecycle policy; explain Cool/delete trade-off.
9. Q&A: roles (Blob Data Owner vs Account Contributor), PIT limits (no container resurrect, one-at-a-time).
