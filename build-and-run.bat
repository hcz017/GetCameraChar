@echo off
REM Build, Install and Run Android APK Script
REM Package: com.tenthorange.getcamerachar

setlocal enabledelayedexpansion

set PACKAGE_NAME=com.tenthorange.getcamerachar
set MAIN_ACTIVITY=com.tenthorange.getcamerachar.MainActivity
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

echo ========================================
echo   Build, Install and Run Android APK
echo ========================================
echo.

REM Step 1: Build APK
echo [1/4] Building APK...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo Build successful!
echo.

REM Step 2: Check if device is connected
echo [2/4] Checking for connected devices...
adb devices | findstr /R "device$" >nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: No Android device found!
    echo Please connect your device via USB and enable USB debugging.
    echo.
    adb devices
    pause
    exit /b 1
)
echo Device found!
echo.

REM Step 3: Install APK
echo [3/4] Installing APK...
adb install -r "%APK_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Installation failed!
    pause
    exit /b 1
)
echo Installation successful!
echo.

REM Step 4: Launch app
echo [4/4] Launching application...
adb shell am start -n %PACKAGE_NAME%/%MAIN_ACTIVITY%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to launch app!
    pause
    exit /b 1
)
echo.
echo ========================================
echo   Success! App is running on device.
echo ========================================
echo.

@REM pause

