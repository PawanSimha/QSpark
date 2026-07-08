@echo off
cd /d "%~dp0"
title QSpark - Launcher

echo ============================================
echo    QSpark - Smart Parking System
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
start "QSpark-App" /MIN mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"

echo [2/3] Waiting for server (this may take 30-90 sec)...
echo.
set "QPARK_WAIT=0"
:waitloop
timeout /t 3 /nobreak >nul
netstat -an 2>nul | findstr ":8081 " | findstr "LISTENING" >nul
if not errorlevel 1 goto serverup
set /a "QPARK_WAIT+=3"
if %QPARK_WAIT% GEQ 180 (
    echo.
    echo [TIMEOUT] Server did not start within 3 minutes.
    echo Check the QSpark-App window for Maven/Java errors.
    echo Press any key to exit.
    pause >nul
    taskkill /f /fi "WINDOWTITLE eq QSpark-App" >nul 2>&1
    exit
)
goto waitloop

:serverup
echo [3/3] Server ready! Opening browser...
timeout /t 1 /nobreak >nul
start http://localhost:8081/
echo.
echo ============================================
echo    App is running at:
echo    http://localhost:8081/
echo.
echo    Login: QSparker / QSpark@Secure
echo ============================================
echo.
echo Press any key to STOP the server and exit.
pause >nul

echo.
echo Shutting down...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081 " ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
)
taskkill /f /fi "WINDOWTITLE eq QSpark-App" >nul 2>&1
echo Done.
exit
