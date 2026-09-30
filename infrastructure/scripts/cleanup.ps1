# T258 cleanup - delete resource group (asks for confirmation).
param([string]$ResourceGroup = "rg-t258-blob")
$ErrorActionPreference = "Stop"
Write-Host "This will delete RG: $ResourceGroup" -ForegroundColor Yellow
$yn = Read-Host "Type YES to continue"
if ($yn -ne "YES") { Write-Host "Aborted."; exit 0 }
az group delete --name $ResourceGroup --yes --no-wait
Write-Host "Delete requested."
