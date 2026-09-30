# Presentation outline (T258)

1. Title: 24CC3046-P054 Azure Blob Versioning and PIT Restore
2. Team: lead 2400032992, members 2400032987/2887/2886
3. Problem: accidental overwrites, need 30-day recovery
4. Use cases: overwrite recovery, 30-day window
5. Azure architecture (diagram)
6. Blob versioning demo
7. Soft delete + change feed
8. PIT restore (admin, UTC, one-at-a-time)
9. 30-day window + warning
10. Bottleneck: version accumulation cost
11. Lifecycle solution (7d Cool, 31d delete, configurable)
12. Live demo (5-10 min script in demo-script.md)
13. Testing (unit + verify script)
14. Conclusion + cleanup
