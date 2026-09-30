# T258 deploy: login check + subscription + RG + Bicep + build + App Service deploy.
param(
  [string]$SubscriptionId = "1ea51f6c-6825-4ece-9d0f-30fc3ee5dd82",
  [string]$ResourceGroup = "rg-t258-blob",
  [string]$Location = "centralindia",
  [string]$StorageAccountName = "",
  [string]$AppName = "",
  [string]$ContainerName = "data-protection",
  [string]$Sku = "B1"
)
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($StorageAccountName)) {
  $StorageAccountName = "t258blob" + (Get-Random -Minimum 1000 -Maximum 9999)
  Write-Host "Generated storage account: $StorageAccountName"
}
if ([string]::IsNullOrWhiteSpace($AppName)) {
  $AppName = "t258-blob-" + (Get-Random -Minimum 1000 -Maximum 9999)
  Write-Host "Generated app name: $AppName"
}

Write-Host "== az login check =="
az account show --output table
if (-not $?) { throw "Run: az login --use-device-code" }

Write-Host "== set subscription =="
az account set --subscription $SubscriptionId

Write-Host "== resource group =="
az group create --name $ResourceGroup --location $Location --output table

Write-Host "== Bicep deploy =="
az deployment group create `
  --resource-group $ResourceGroup `
  --template-file ../main.bicep `
  --parameters storageAccountName=$StorageAccountName appName=$AppName location=$Location containerName=$ContainerName softDeleteDays=31 pitRestoreDays=30 tierCoolAfterDays=7 deleteVersionAfterDays=31 appServicePlanSku=$Sku `
  --output table

Write-Host "== verify =="
./verify-azure.ps1 -ResourceGroup $ResourceGroup -StorageAccountName $StorageAccountName

Write-Host "== build backend =="
$mvn = "C:\Users\soura\AppData\Local\Temp\opencode\maven\apache-maven-3.9.9\bin\mvn.cmd"
if (Test-Path $mvn) { $env:Path = "C:\Users\soura\AppData\Local\Temp\opencode\maven\apache-maven-3.9.9\bin;" + $env:Path }
Push-Location ../../backend
mvn -q clean package -DskipTests
if (-not $?) { throw "Maven build failed" }
$jar = Get-ChildItem target/*.jar | Select-Object -First 1
Write-Host "Built: $($jar.FullName)"
Pop-Location

Write-Host "== deploy to App Service =="
az webapp deploy --resource-group $ResourceGroup --name $AppName --src-path backend/target/dataprotection.jar --type jar --output table

$url = (az webapp show --resource-group $ResourceGroup --name $AppName --query defaultHostName -o tsv)
Write-Host ""
Write-Host "DONE. App: https://$url"
Write-Host "Storage: $StorageAccountName  Container: $ContainerName"
Write-Host "Set env AZURE_STORAGE_ACCOUNT=$StorageAccountName if running locally."
