# Scenario C - 30-day recovery window

Dashboard shows:
- Blob Versioning: ENABLED (live)
- Blob Soft Delete: ENABLED, 31 days (live)
- Change Feed: ENABLED (configured)
- PIT Restore: ENABLED, 30 days (configured)
- Lifecycle: ENABLED (configured)

Checks:
- `verify-azure.ps1` asserts PIT=30, soft-delete>=31, PIT<soft-delete, HNS disabled.
- Warning displayed: "Recovery before the PIT retention start is not available."
- Timestamps must be UTC.
- Lifecycle: versions >7d tier to Cool, >31d delete (configurable). This is the cost/recovery trade-off:
  longer retention = safer but more storage; shorter = cheaper but less history.
