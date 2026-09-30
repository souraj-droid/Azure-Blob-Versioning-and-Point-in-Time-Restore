# Testing

Unit (no Azure needed):
- `mvn test` runs ValidationUtilsTests, ControllerTests (MockMvc + mocks), StorageProtectionServiceTests.
- Covers: startup, upload path, overwrite->new version (mocked), list, download, promote, 404 blob/version, invalid/future/out-of-window PIT, PIT validation, Azure error handling.

Integration (needs Azure):
- Set AZURE_STORAGE_ACCOUNT + `az login`, run app, use tests/api/*.http and tests/scenarios/*.
- Do not claim cloud tests passed without credentials.

Verify infra:
- `az bicep build --file infrastructure/main.bicep`
- `verify-azure.ps1` asserts versioning/soft-delete/changefeed/PIT/lifecycle/HNS.
