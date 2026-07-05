@echo off
cd /d "%~dp0"
title Q-Park - Launcher

echo ============================================
echo    Q-Park - Smart Parking System
echo ============================================
echo.

netstat -an 2>nul | findstr ":8081 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [ALREADY RUNNING] Port 8081 is in use.
    echo Opening browser...
    timeout /t 1 /nobreak >nul
    start http://localhost:8081/
    echo.
    echo Press any key to exit.
    pause >nul
    exit
)

echo [1/3] Starting backend server...
echo.
start "Q-Park-App" /MIN mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"

echo [2/3] Waiting for server (this may take 30-60 sec)...
echo.
:waitloop
timeout /t 3 /nobreak >nul
netstat -an 2>nul | findstr ":8081 " | findstr "LISTENING" >nul
if errorlevel 1 goto waitloop

echo [3/3] Server ready! Opening browser...
timeout /t 1 /nobreak >nul
start http://localhost:8081/
echo.
echo ============================================
echo    App is running at:
echo    http://localhost:8081/
echo.
echo    Login: SPPN / [REDACTED]
echo ============================================
echo.
echo Press any key to STOP the server and exit.
pause >nul

echo.
echo Shutting down...
taskkill /f /fi "WINDOWTITLE eq Q-Park-App" >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081 " ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
)
echo Done.
exit
