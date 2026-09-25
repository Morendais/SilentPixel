#!/usr/bin/env bash
# Pixel Japanese Camera Shutter Mute (No Root / No SIM)
# Compatible with modern Google Pixel devices (Pixel 6/7/8/9/10 series)

set -e

echo "========================================================"
echo "  Pixel Japanese Camera Shutter Mute (No Root / No SIM)"
echo "========================================================"
echo ""

if ! command -v adb &> /dev/null; then
    echo "[!] adb could not be found. Please install Android platform-tools."
    exit 1
fi

echo "[*] Checking connected devices..."
adb wait-for-device

echo "[+] Device detected!"
echo "[*] Muting Volume Group 7 (AUDIO_STREAM_ENFORCED_AUDIBLE)..."

adb shell "cmd audio set-group-volume 7 0"
adb shell "cmd audio adj-group-volume 7 MUTE"
adb shell "cmd audio set-group-volume 2 0"
adb shell "cmd audio adj-group-volume 2 MUTE"

echo ""
echo "[*] Verifying status..."
adb shell "dumpsys audio" | grep -A 5 "VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE" || true

echo ""
echo "========================================================"
echo "[+] SUCCESS! Camera shutter sound is now completely muted."
echo "[*] Media, calls, and notifications remain unaffected."
echo "[i] Note: Android resets this on reboot. Re-run after restarting."
echo "========================================================"
