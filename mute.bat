@echo off
chcp 65001 >nul
title Pixel Japanese Camera Shutter Mute Tool

echo ========================================================
echo   Pixel Japanese Camera Shutter Mute (No Root / No SIM)
echo ========================================================
echo.

:: Check for adb
where adb >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set ADB_CMD=adb
) else (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set ADB_CMD="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    ) else (
        echo [!] ADB not found in PATH or Android SDK.
        echo Please ensure ADB is installed and added to PATH.
        echo.
        pause
        exit /b 1
    )
)

echo [*] Checking connected devices...
%ADB_CMD% get-state >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [!] No authorized device found!
    echo Please make sure:
    echo  1. USB Debugging is ENABLED in Developer Options.
    echo  2. Phone is connected via USB.
    echo  3. You accepted the 'Allow USB debugging' prompt on your phone screen.
    echo.
    pause
    exit /b 1
)

echo [+] Device connected!
echo [*] Muting Volume Group 7 (AUDIO_STREAM_ENFORCED_AUDIBLE)...

%ADB_CMD% shell "cmd audio set-group-volume 7 0"
%ADB_CMD% shell "cmd audio adj-group-volume 7 MUTE"

echo.
echo [*] Verifying status:
%ADB_CMD% shell "dumpsys audio | grep -A 5 'VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE'"

echo.
echo ========================================================
echo [+] SUCCESS! Camera shutter sound has been muted.
echo [*] Background music and calls will NOT be affected.
echo [i] Note: Run this script again if you reboot your phone.
echo ========================================================
echo.
pause
