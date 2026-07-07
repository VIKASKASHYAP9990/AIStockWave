# AIStockWave JavaFX / MySQL Compiler & Launcher Script
# Run this script to automatically fetch dependencies, compile, and boot the GUI

# 1. Direct environment variable JAVA_HOME to JDK 8
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_202"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 2. Identify local Maven path
$MavenBin = "C:\Users\vikas\.gemini\antigravity-ide\scratch\maven\apache-maven-3.8.8\bin\mvn.cmd"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "      AIStockWave - Compiler & Launcher   " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Using JDK: $env:JAVA_HOME" -ForegroundColor White
Write-Host "Using Maven: $MavenBin" -ForegroundColor White
Write-Host "Compiling source files and pulling dependencies..." -ForegroundColor White

# 3. Clean and compile using Maven
& $MavenBin clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed! Please check code or database configurations." -ForegroundColor Red
    Exit 1
}

Write-Host "Compilation successful! Booting JavaFX GUI..." -ForegroundColor Green
# 4. Launch JavaFX Application
& $MavenBin exec:java
