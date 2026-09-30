# T258 verify-azure.ps1 - fails if any protection requirement is unmet.
param(
  [string]$ResourceGroup = "rg-t258-blob",
  [string]$StorageAccountName = ""
)
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($StorageAccountName)) { throw "Pass -StorageAccountName <name>" }

function Fail($m) { Write-Host "FAIL: $m" -ForegroundColor Red; exit 1 }
function Ok($m) { Write-Host "PASS: $m" -ForegroundColor Green }

$acct = az storage account show --name $StorageAccountName --resource-group $ResourceGroup -o json | ConvertFrom-Json
if (-not $acct) { Fail "account not found" }
if ($acct.kind -ne "StorageV2") { Fail "kind=$($acct.kind), expected StorageV2" } else { Ok "kind=StorageV2" }
if ($acct.sku.name -notlike "Standard*") { Fail "sku=$($acct.sku.name), expected Standard" } else { Ok "sku=$($acct.sku.name)" }
if ($acct.isHnsEnabled -eq $true) { Fail "HNS must be disabled" } else { Ok "HNS disabled" }

$svc = az storage account blob-service-properties show --account-name $StorageAccountName --resource-group $ResourceGroup -o json | ConvertFrom-Json
if ($svc.isVersioningEnabled -ne $true) { Fail "versioning disabled" } else { Ok "versioning enabled" }
if ($svc.deleteRetentionPolicy.enabled -ne $true) { Fail "blob soft delete disabled" } else { Ok "blob soft delete enabled" }
$sd = [int]$svc.deleteRetentionPolicy.days
if ($sd -le 30) { Fail "soft-delete retention=$sd, must be >=31" } else { Ok "soft-delete retention=$sd" }
if ($svc.changeFeed.enabled -ne $true) { Fail "change feed disabled" } else { Ok "change feed enabled" }
if ($svc.restorePolicy.enabled -ne $true) { Fail "PIT restore disabled" } else { Ok "PIT restore enabled" }
$pit = [int]$svc.restorePolicy.days
if ($pit -ne 30) { Fail "PIT retention=$pit, expected 30" } else { Ok "PIT retention=30" }
if ($pit -ge $sd) { Fail "PIT ($pit) must be < soft-delete ($sd)" } else { Ok "PIT < soft-delete rule holds" }

$pol = az storage account management-policy show --account-name $StorageAccountName --resource-group $ResourceGroup -o json 2>$null | ConvertFrom-Json
if (-not $pol -or -not $pol.policy.rules -or $pol.policy.rules.Count -eq 0) { Fail "lifecycle policy missing" } else { Ok "lifecycle policy exists ($($pol.policy.rules.Count) rules)" }

Write-Host "ALL CHECKS PASSED" -ForegroundColor Green
