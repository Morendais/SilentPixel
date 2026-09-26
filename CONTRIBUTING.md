# Contributing to SilentPixel

Thank you for your interest in contributing to **SilentPixel**!

## How You Can Contribute

1. **Reporting Issues**:
   - If a specific Android version or Pixel device behaves unexpectedly, open an issue with:
     - Device model (e.g. Pixel 10 Pro XL, Pixel 8a)
     - Android build number (Settings → About phone → Build number)
     - Carrier / SIM setup
     - Output of `adb shell dumpsys audio | grep -A 5 "AUDIO_STREAM_ENFORCED_AUDIBLE"`

2. **Feature Requests & Code Contributions**:
   - Fork the repository.
   - Create a feature branch (`git checkout -b feature/amazing-feature`).
   - Commit your changes with clear messages.
   - Open a Pull Request against `master`.

## Building from Source

To compile `SilentPixel.apk` locally:
```powershell
# Requirements: Android SDK build-tools and JDK 11+
powershell -ExecutionPolicy Bypass -File .\build_apk.ps1
```

The compiled APK will be output to `./SilentPixel.apk`.
