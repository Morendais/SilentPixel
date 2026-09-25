# Pixel Japanese Camera Shutter Mute (No Root, No SIM, No Interrupted Music)

> **Permanently silence the mandatory Japanese Google Pixel camera shutter sound without Root, without connecting to foreign cell towers, and without pausing or muting your background music/calls.**

Tested and verified on **Google Pixel 10 Pro XL**, **Pixel 9 Pro**, **Pixel 8**, and other modern Pixel devices on Android 14+.

---

## 💡 The Discovery: Why This Works

### The Problem
* Google Pixels manufactured for the Japanese market (`ro.boot.warranty.sku=AJP`) enforce a mandatory camera shutter sound.
* Outside Japan, Google allows muting **only if the phone connects to a foreign cell tower** and receives a non-Japanese `MCC` (Mobile Country Code).
* If you are in an area without working cellular towers, without roaming coverage, in airplane mode, or using an eSIM that fails to register on a physical base station, the phone defaults back to hardware SKU enforcement.
* **Why traditional ADB commands failed:** Legacy commands like `setStreamVolume(7, 0)` or `isCameraSoundForced` are explicitly protected by Android's `AudioService`. The stream `STREAM_SYSTEM_ENFORCED` ignores standard stream volume sliders.
* **Why third-party Play Store apps ("Mute Camera", etc.) suck:** They hijack global `AUDIOFOCUS_GAIN` with `USAGE_MEDIA` and mute media volume, which abruptly cuts off Spotify, Apple Music, and YouTube, and still leaks shutter clicks if music was actively streaming.

### The Solution: Android Audio Volume Groups
Modern Android (Android 11 through Android 16) manages audio streams using **Audio Volume Groups**.
* The protected stream `STREAM_SYSTEM_ENFORCED` is mapped to **Volume Group 7** (`AUDIO_STREAM_ENFORCED_AUDIBLE`).
* The system service `cmd audio` provides direct access to group volume manipulation:
  ```bash
  cmd audio set-group-volume 7 0
  cmd audio adj-group-volume 7 MUTE
  ```
* This directly sets the hardware speaker volume for the enforced group to `0` and flags it as `Muted: true` — **completely bypassing the legacy stream block without affecting media (Group 4) or calls (Group 1)!**

---

## 📱 SilentPixel App (The Easiest Way)

We built an open-source Android app: **SilentPixel** (`SilentPixel.apk`, ~37 KB).

### Features:
* 🟢 **One-Click Mute / Unmute**: Clean Material 3 UI with instant state indicator.
* 📲 **Quick Settings Tile**: Add a toggle button directly to your Android notification shade (next to Wi-Fi / Bluetooth).
* 🔄 **Auto-Mute on Reboot**: Listens to `BOOT_COMPLETED` so your camera stays silent even after restarting the phone.
* ⚡ **Zero Background Battery Drain**: Runs only when toggled or on reboot.
* 🛡️ **Powered by Shizuku / Root**: Works seamlessly without needing PC cables once configured.

### How to use:
1. Download and install **`SilentPixel.apk`** from [Releases](https://github.com).
2. Install and activate **[Shizuku](https://shizuku.rikka.app/)** (free on Google Play, activates in 10s via Wireless Debugging).
3. Open **SilentPixel**, allow Shizuku access, and tap **"Заглушить затвор"**!
4. *(Optional)* Pull down your notification shade, tap the edit (pencil) icon, and drag the **"Звук камеры"** tile into your active quick settings.

---

## 💻 Quick Start via PC (No App / Script Only)

### 1. Prerequisites
1. Enable **Developer Options** on your Pixel:
   * Go to **Settings → About Phone** and tap **Build Number** 7 times.
2. Enable **USB Debugging**:
   * Go to **Settings → System → Developer Options** → toggle **USB Debugging** ON.
3. Connect your phone to your computer via USB and tap **"Always allow from this computer"** on the phone screen.

### 2. Run the Script

#### Windows:
Double-click `mute.bat`.

#### macOS / Linux:
```bash
chmod +x mute.sh
./mute.sh
```

#### Manual ADB Command:
```bash
adb shell "cmd audio set-group-volume 7 0"
adb shell "cmd audio adj-group-volume 7 MUTE"
```

---

## 📱 On-Device Automation (No PC after reboot)

Because Android's `AudioService` resets volume groups back to default when the phone reboots, you can automate this command directly on your phone using **Shizuku**:

1. Install **[Shizuku](https://shizuku.rikka.app/)** from Google Play or GitHub.
2. Start Shizuku via **Wireless Debugging** (Settings → Developer Options → Wireless Debugging) or via PC once.
3. Use **MacroDroid**, **Tasker**, or **Termux** with Shizuku support:
   * **Trigger:** Device Boot (запуск устройства).
   * **Action:** Run Shell Command via Shizuku:
     ```sh
     cmd audio set-group-volume 7 0
     cmd audio adj-group-volume 7 MUTE
     ```
4. Now, every time you reboot your phone, the camera will be automatically silenced without ever needing to plug it into a PC again!

---

## 🔍 Verification

To verify that Volume Group 7 is muted, run:
```bash
adb shell dumpsys audio | grep -A 5 "VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE"
```
Output should show:
```text
- VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE:
     Muted: true
     Min: 0
     Max: 7
     Current: 2 (speaker): 0
```

---

## 📄 License
MIT License. Feel free to share and star!
