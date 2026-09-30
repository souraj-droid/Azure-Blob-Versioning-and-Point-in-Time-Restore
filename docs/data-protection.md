# Data protection

Two distinct mechanisms:

1. Version recovery (per-blob): list versions, download any version, restore by copying
   previous version bytes to a new current version. Fast, user-level.

2. Point-in-time restore (account/range): Restore Blob Ranges to a UTC timestamp
   inside the 30-day window. Atomic per range, one at a time per account.
   Admin-only (Storage Account Contributor). App validates; CLI executes.

Limits: PIT covers block-blob data ops only, not deleted containers.
Recovery before retention start is unavailable. Use UTC.
