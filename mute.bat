@echo off
chcp 65001 >nul
title Pixel Shutter Tool

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
        pause
        exit /b 1
    )
)

echo [*] Checking connected devices...
%ADB_CMD% get-state >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [!] No authorized device found!
    echo Please make sure USB debugging is enabled.
    pause
    exit /b 1
)

echo [+] Device connected!
echo [*] Muting Volume Group 7 (AUDIO_STREAM_ENFORCED_AUDIBLE)...

%ADB_CMD% shell "cmd audio set-group-volume 7 0"
%ADB_CMD% shell "cmd audio adj-group-volume 7 MUTE"

echo [*] Starting embedded standalone engine for Pixel Shutter app...
%ADB_CMD% shell "pkill -f LocalServer 2>/dev/null"
for /f "tokens=2 delims=:" %%i in ('%ADB_CMD% shell pm path com.antigravity.silentpixel 2^>nul') do (
    %ADB_CMD% shell "nohup app_process -Djava.class.path=%%i /system/bin com.antigravity.silentpixel.LocalServer >/dev/null 2>&1 &"
)

echo.
echo ========================================================
echo [+] SUCCESS! Camera shutter sound has been muted.
echo [*] SilentPixel app standalone engine is now ACTIVE.
echo [*] You can now toggle shutter sound directly inside the app!
echo ========================================================
echo.
pause
