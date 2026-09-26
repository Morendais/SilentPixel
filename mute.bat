@echo off
chcp 65001 >nul
title Pixel Camera Shutter Mute

echo ========================================================
echo   Pixel Camera Shutter Mute (No Root / No SIM)
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
        echo Please install platform-tools or add adb to your PATH.
        pause
        exit /b 1
    )
)

echo [*] Checking connected devices...
%ADB_CMD% get-state >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [!] No authorized device found!
    echo Please connect your Pixel and enable USB Debugging in Developer Options.
    pause
    exit /b 1
)

echo [+] Device connected!
echo [*] Muting Volume Group 7 (AUDIO_STREAM_ENFORCED_AUDIBLE)...

%ADB_CMD% shell "cmd audio set-group-volume 7 0"
%ADB_CMD% shell "cmd audio adj-group-volume 7 MUTE"
%ADB_CMD% shell "cmd audio set-group-volume 2 0"
%ADB_CMD% shell "cmd audio adj-group-volume 2 MUTE"

echo.
echo ========================================================
echo [+] SUCCESS! Japanese camera shutter sound has been muted.
echo [*] Music, videos, ringtones, and notifications are unaffected!
echo [*] Note: Android resets this setting when the phone reboots.
echo     Simply re-run this .bat file whenever you restart your device.
echo ========================================================
echo.
pause
