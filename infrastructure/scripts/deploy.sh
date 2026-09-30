#!/usr/bin/env bash
# Equivalent Linux/macOS deploy flow for T258.
set -e
SUBSCRIPTION_ID="${SUBSCRIPTION_ID:-1ea51f6c-6825-4ece-9d0f-30fc3ee5dd82}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-t258-blob}"
LOCATION="${LOCATION:-centralindia}"
STORAGE_ACCOUNT="${STORAGE_ACCOUNT:-t258blob$RANDOM}"
APP_NAME="${APP_NAME:-t258-blob-$RANDOM}"
CONTAINER="${CONTAINER:-data-protection}"
SKU="${SKU:-B1}"

az account show --output table
az account set --subscription "$SUBSCRIPTION_ID"
az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --output table
az deployment group create \
  --resource-group "$RESOURCE_GROUP" \
  --template-file ../main.bicep \
  --parameters storageAccountName="$STORAGE_ACCOUNT" appName="$APP_NAME" location="$LOCATION" containerName="$CONTAINER" softDeleteDays=31 pitRestoreDays=30 tierCoolAfterDays=7 deleteVersionAfterDays=31 appServicePlanSku="$SKU" \
  --output table
echo "Verify with: pwsh ./verify-azure.ps1 -ResourceGroup $RESOURCE_GROUP -StorageAccountName $STORAGE_ACCOUNT"
echo "Build: (cd ../../backend && mvn clean package)"
echo "Deploy jar: az webapp deploy -g $RESOURCE_GROUP -n $APP_NAME --src-path ../../backend/target/dataprotection.jar --type jar"
