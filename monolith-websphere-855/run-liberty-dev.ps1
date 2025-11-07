# Open Liberty Development Mode Startup Script
# This script starts Open Liberty in development mode with hot reload enabled

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Customer Order Services - Dev Mode" -ForegroundColor Cyan
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
Write-Host "Starting Open Liberty in Development Mode..." -ForegroundColor Green
Write-Host "Features:" -ForegroundColor Gray
Write-Host "  - Hot reload enabled (code changes auto-deploy)" -ForegroundColor Gray
Write-Host "  - Test execution on changes" -ForegroundColor Gray
Write-Host "  - HTTP: http://localhost:9080" -ForegroundColor Gray
Write-Host "  - HTTPS: https://localhost:9443" -ForegroundColor Gray
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Start Liberty in dev mode
C:\Users\mvorster\.maven\maven-3.9.11\bin\mvn.cmd liberty:dev

# Return to original directory on exit
Set-Location -Path $PSScriptRoot
