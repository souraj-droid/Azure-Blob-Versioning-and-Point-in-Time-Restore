# Scenario A - Accidental overwrite recovery

1. Upload `report.txt` with content: "This is the original project report."
   - `POST /api/blobs/upload` -> returns versionId V1.
2. Overwrite `report.txt` with: "This report contains incorrect data."
   - `POST /api/blobs/report.txt/overwrite` (or upload same name) -> V2 (current).
3. `GET /api/blobs/report.txt/versions` shows V1 (previous) + V2 (current).
4. `POST /api/blobs/report.txt/restore/<V1>` copies V1 bytes to new current V3.
5. `GET /api/blobs/report.txt/download` returns original text.

Expected: current content == original. Azure versioning preserves V1/V2; restore promotes via copy.
