# Open Liberty Production Mode Startup Script
# This script starts Open Liberty in standard run mode

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Customer Order Services - Production" -ForegroundColor Cyan
Write-Host " Open Liberty with Jakarta EE 10" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set database environment variables
Write-Host "Configuring database connection..." -ForegroundColor Yellow
$env:DB_HOST = "localhost"
$env:DB_PORT = "50000"
$env:DB_NAME = "ORDERDB"
$env:DB_USER = "db2inst1"
$env:DB_PASSWORD = "db2inst1"
$env:DB_SCHEMA = "DB2INST1"

Write-Host "  DB_HOST: $env:DB_HOST" -ForegroundColor Gray
Write-Host "  DB_PORT: $env:DB_PORT" -ForegroundColor Gray
Write-Host "  DB_NAME: $env:DB_NAME" -ForegroundColor Gray
Write-Host "  DB_USER: $env:DB_USER" -ForegroundColor Gray
Write-Host ""

# Navigate to application module
Write-Host "Navigating to CustomerOrderServicesApp module..." -ForegroundColor Yellow
Set-Location -Path "$PSScriptRoot\CustomerOrderServicesApp"

Write-Host ""
Write-Host "Starting Open Liberty Server..." -ForegroundColor Green
Write-Host "Endpoints:" -ForegroundColor Gray
Write-Host "  - HTTP: http://localhost:9080/CustomerOrderServicesWeb" -ForegroundColor Gray
Write-Host "  - HTTPS: https://localhost:9443/CustomerOrderServicesWeb" -ForegroundColor Gray
Write-Host "  - REST API: http://localhost:9080/CustomerOrderServicesWeb/jaxrs" -ForegroundColor Gray
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Start Liberty in production mode
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd liberty:run

# Return to original directory on exit
Set-Location -Path $PSScriptRoot
