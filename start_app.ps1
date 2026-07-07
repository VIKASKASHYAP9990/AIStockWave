# Launcher script for the Streamlit Web Application
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "      AIStockWave Streamlit Web Application       " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Booting Streamlit local web server..." -ForegroundColor White
Write-Host "Press Ctrl+C in this terminal to stop the server." -ForegroundColor Yellow
Write-Host ""
Write-Host "👉 Open your web browser at: http://localhost:8501" -ForegroundColor Green
Write-Host ""

streamlit run app.py
