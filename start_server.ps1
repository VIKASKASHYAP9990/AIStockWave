# AIStockWave Web App Launcher Script
# Run this script to start a local HTTP server and view the website

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "       AIStockWave Web Application Server         " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Starting local web server using Python..." -ForegroundColor White
Write-Host "Press Ctrl+C in this terminal to stop the server." -ForegroundColor Yellow
Write-Host ""
Write-Host "👉 Open your web browser at: http://localhost:8000" -ForegroundColor Green
Write-Host ""

python -m http.server 8000
