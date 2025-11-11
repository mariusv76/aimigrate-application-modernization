# Azure Infrastructure Deployment Script
# Deploys Customer Order Services infrastructure with automated role assignments

param(
    [Parameter(Mandatory=$false)]
    [string]$ResourceGroup = "rg-customerorder-dev",
    
    [Parameter(Mandatory=$false)]
    [string]$Location = "northeurope",
    
    [Parameter(Mandatory=$false)]
    [string]$BaseName = "customerorder",
    
    [Parameter(Mandatory=$false)]
    [string]$Environment = "dev",
    
    [Parameter(Mandatory=$false)]
    [bool]$UseContainerApps = $true,
    
    [Parameter(Mandatory=$false)]
    [string]$SubscriptionId = ""
)

$ErrorActionPreference = "Stop"

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Customer Order Services - Infrastructure Deployment" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Set subscription if provided
if ($SubscriptionId) {
    Write-Host "Setting Azure subscription to: $SubscriptionId" -ForegroundColor Yellow
    az account set --subscription $SubscriptionId
}

# Verify current subscription
Write-Host "Current subscription:" -ForegroundColor Yellow
az account show --query "[name,id]" -o table

Write-Host ""
Write-Host "Deployment Configuration:" -ForegroundColor Green
Write-Host "  Resource Group:   $ResourceGroup" -ForegroundColor White
Write-Host "  Location:         $Location" -ForegroundColor White
Write-Host "  Base Name:        $BaseName" -ForegroundColor White
Write-Host "  Environment:      $Environment" -ForegroundColor White
Write-Host "  Container Apps:   $UseContainerApps" -ForegroundColor White
Write-Host ""

# Check if resource group exists, create if not
Write-Host "Checking resource group..." -ForegroundColor Yellow
$rgExists = az group exists --name $ResourceGroup
if ($rgExists -eq "false") {
    Write-Host "Creating resource group: $ResourceGroup" -ForegroundColor Yellow
    az group create --name $ResourceGroup --location $Location
    Write-Host "✓ Resource group created" -ForegroundColor Green
} else {
    Write-Host "✓ Resource group exists" -ForegroundColor Green
}

Write-Host ""
Write-Host "Starting Bicep deployment..." -ForegroundColor Yellow
Write-Host ""

# Deploy infrastructure
$deploymentName = "customerorder-infra-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

az deployment group create `
    --name $deploymentName `
    --resource-group $ResourceGroup `
    --template-file ./main.bicep `
    --parameters baseName=$BaseName environment=$Environment useContainerApps=$UseContainerApps `
    --verbose

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host "✓ Deployment completed successfully!" -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "Retrieving deployment outputs..." -ForegroundColor Yellow
    $outputs = az deployment group show `
        --name $deploymentName `
        --resource-group $ResourceGroup `
        --query properties.outputs `
        -o json | ConvertFrom-Json
    
    Write-Host ""
    Write-Host "Deployment Outputs:" -ForegroundColor Cyan
    Write-Host "==================" -ForegroundColor Cyan
    Write-Host "Key Vault:          $($outputs.keyVaultName.value)" -ForegroundColor White
    Write-Host "App Config:         $($outputs.appConfigEndpoint.value)" -ForegroundColor White
    Write-Host "ACR Login Server:   $($outputs.acrLoginServer.value)" -ForegroundColor White
    Write-Host ""
    
    if ($UseContainerApps) {
        Write-Host "✓ Role assignments created automatically:" -ForegroundColor Green
        Write-Host "  - Key Vault Secrets User" -ForegroundColor White
        Write-Host "  - App Configuration Data Reader" -ForegroundColor White
        Write-Host "  - AcrPull" -ForegroundColor White
        Write-Host ""
    }
    
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Store database password in Key Vault:" -ForegroundColor White
    Write-Host "   az keyvault secret set --vault-name $($outputs.keyVaultName.value) --name db-password --value '<your-password>'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Configure App Configuration values (if needed):" -ForegroundColor White
    Write-Host "   az appconfig kv set --name appconfig-$BaseName --key db:host --value '<db-host>'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Build and push Docker image:" -ForegroundColor White
    Write-Host "   docker build -f Deployment/Dockerfile.liberty -t customerorder-api:latest ." -ForegroundColor Gray
    Write-Host "   az acr login --name $($outputs.acrLoginServer.value -replace '.azurecr.io','')" -ForegroundColor Gray
    Write-Host "   docker tag customerorder-api:latest $($outputs.acrLoginServer.value)/customerorder-api:latest" -ForegroundColor Gray
    Write-Host "   docker push $($outputs.acrLoginServer.value)/customerorder-api:latest" -ForegroundColor Gray
    Write-Host ""
    Write-Host "4. Update Container App with environment variables (if needed)" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Red
    Write-Host "✗ Deployment failed!" -ForegroundColor Red
    Write-Host "=====================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the error messages above for details." -ForegroundColor Yellow
    exit 1
}
