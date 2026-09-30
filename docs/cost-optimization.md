# Cost optimization

Every overwrite of a versioned blob creates a new version -> storage grows.
Without control, cost grows linearly with churn.

Solution: lifecycle policy on previous versions:
- > tierCoolAfterDays (default 7) -> tier to Cool (cheaper)
- > deleteVersionAfterDays (default 31) -> delete

Trade-off: longer delete threshold = longer ad-hoc version history but higher cost;
shorter = cheaper but relies solely on 30-day PIT window + soft delete.
Adjust per institutional policy. PIT retention itself does not control version retention.
