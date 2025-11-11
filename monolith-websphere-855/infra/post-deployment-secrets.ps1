# Post-Deployment Script: Configure Container App Secrets with Key Vault References
# Purpose: Two-stage deployment to avoid chicken-and-egg problem with role assignments
# Stage 1: Bicep deploys Container App with placeholder DB_PASSWORD
# Stage 2: This script updates secrets to use Key Vault references after role assignments complete

param(
    [Parameter(Mandatory=$true)]
    [string]$ResourceGroupName,
    
    [Parameter(Mandatory=$true)]
    [string]$ContainerAppName,
    
    [Parameter(Mandatory=$true)]
    [string]$KeyVaultName
)

Write-Host "=== Post-Deployment: Configuring Container App Secrets with Key Vault References ===" -ForegroundColor Cyan
Write-Host "Resource Group: $ResourceGroupName" -ForegroundColor Gray
Write-Host "Container App: $ContainerAppName" -ForegroundColor Gray
Write-Host "Key Vault: $KeyVaultName" -ForegroundColor Gray
Write-Host ""

# Step 1: Verify Container App exists and get managed identity
Write-Host "[1/4] Verifying Container App deployment..." -ForegroundColor Yellow
$containerApp = az containerapp show `
    --name $ContainerAppName `
    --resource-group $ResourceGroupName `
    --query "{principalId:identity.principalId, state:properties.provisioningState}" `
    --output json | ConvertFrom-Json

if ($containerApp.state -ne "Succeeded") {
    Write-Error "Container App provisioning state is '$($containerApp.state)'. Expected 'Succeeded'."
    exit 1
}

Write-Host "  ✓ Container App deployed successfully" -ForegroundColor Green
Write-Host "  Managed Identity: $($containerApp.principalId)" -ForegroundColor Gray
Write-Host ""

# Step 2: Verify role assignment exists
Write-Host "[2/4] Verifying Key Vault role assignments..." -ForegroundColor Yellow
$roleAssignment = az role assignment list `
    --scope "/subscriptions/$((az account show --query id -o tsv))/resourceGroups/$ResourceGroupName/providers/Microsoft.KeyVault/vaults/$KeyVaultName" `
    --query "[?principalId=='$($containerApp.principalId)'].roleDefinitionName" `
    --output tsv

if (-not $roleAssignment) {
    Write-Warning "No role assignment found. Waiting for roleAssignments module to complete..."
    Start-Sleep -Seconds 30
    
    $roleAssignment = az role assignment list `
        --scope "/subscriptions/$((az account show --query id -o tsv))/resourceGroups/$ResourceGroupName/providers/Microsoft.KeyVault/vaults/$KeyVaultName" `
        --query "[?principalId=='$($containerApp.principalId)'].roleDefinitionName" `
        --output tsv
    
    if (-not $roleAssignment) {
        Write-Error "Role assignment still not found. Please verify roleAssignments module deployed successfully."
        exit 1
    }
}

Write-Host "  ✓ Role assignment verified: $roleAssignment" -ForegroundColor Green
Write-Host ""

# Step 3: Configure Container App secret with Key Vault reference
Write-Host "[3/4] Configuring Container App secret with Key Vault reference..." -ForegroundColor Yellow
$keyVaultUrl = "https://$KeyVaultName.vault.azure.net/secrets/db-password"

az containerapp secret set `
    --name $ContainerAppName `
    --resource-group $ResourceGroupName `
    --secrets "db-password=keyvaultref:$keyVaultUrl,identityref:system" `
    --output none

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to configure Container App secret with Key Vault reference."
    exit 1
}

Write-Host "  ✓ Secret 'db-password' configured with Key Vault reference" -ForegroundColor Green
Write-Host ""

# Step 4: Update environment variable to use secret reference
Write-Host "[4/4] Updating DB_PASSWORD environment variable to use secret reference..." -ForegroundColor Yellow

az containerapp update `
    --name $ContainerAppName `
    --resource-group $ResourceGroupName `
    --set-env-vars "DB_PASSWORD=secretref:db-password" `
    --output none

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to update DB_PASSWORD environment variable."
    exit 1
}

Write-Host "  ✓ DB_PASSWORD now references Key Vault secret" -ForegroundColor Green
Write-Host ""

# Verification
Write-Host "=== Verification ===" -ForegroundColor Cyan
$dbPasswordConfig = az containerapp show `
    --name $ContainerAppName `
    --resource-group $ResourceGroupName `
    --query "properties.template.containers[0].env[?name=='DB_PASSWORD']" `
    --output json | ConvertFrom-Json

if ($dbPasswordConfig.secretRef -eq "db-password") {
    Write-Host "✓ SUCCESS: DB_PASSWORD is secured with Key Vault reference" -ForegroundColor Green
    Write-Host "  Configuration: secretRef='$($dbPasswordConfig.secretRef)'" -ForegroundColor Gray
} else {
    Write-Warning "DB_PASSWORD configuration may not be correct."
    Write-Host "  Current config: $($dbPasswordConfig | ConvertTo-Json -Compress)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Post-Deployment Complete ===" -ForegroundColor Cyan
Write-Host "Container App secrets are now secured with Key Vault references." -ForegroundColor Green
Write-Host "DB password is no longer visible in Azure Portal environment variables." -ForegroundColor Green
