# SilentPixel: Pixel Japanese Camera Shutter Mute

[![Platform](https://img.shields.io/badge/Platform-Android%2011%E2%80%9316-3DDC84.svg?logo=android&logoColor=white)](#)
[![Device](https://img.shields.io/badge/Tested%20on-Pixel%2010%20Pro%20XL-4285F4.svg?logo=google&logoColor=white)](#)
[![Root](https://img.shields.io/badge/Root-Not%20Required-brightgreen.svg)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Silence the mandatory Japanese Google Pixel camera shutter sound on official Stock Google Camera — without Root, without SIM card tricks, and without pausing or glitching background music or calls.**

Tested and verified directly on **Google Pixel 10 Pro XL** (Android 15 / 16).

---

<p align="center">
  <img src="docs/screenshots/app_main.png" alt="SilentPixel App UI" width="360" />
</p>

---

## Why This Project Exists

Google Pixels manufactured for the Japanese domestic market enforce a loud, mandatory camera shutter sound.

Outside Japan, Google's firmware is supposed to allow muting only if the phone connects to a cellular base station broadcasting a foreign Mobile Country Code (MCC). However:
* If you are in airplane mode, subway tunnels, or areas with poor cellular coverage, the shutter sound forcefully comes back.
* If you use certain eSIMs or carriers that don't continuously update MCC, the click stays permanently loud.

### Comparison with other workarounds

| Method | Works on Stock Camera? | Music / Spotify Plays Cleanly? | Zero Camera Startup Lag? | No Root / No Unlock? |
| :--- | :---: | :---: | :---: | :---: |
| **SilentPixel (This tool)** | **Yes (100% Stock)** | **Yes (Zero interruption)** | **Yes (0 ms delay)** | **Yes** |
| **Play Store "Mute Camera" apps** | Glitchy | Abruptly pauses/ducks audio | 1–2 second lag on launch | Yes |
| **GCam Ports (AGC / BSG)** | Replaces stock camera app | Yes | Varies by port stability | Yes |
| **Root (Magisk / KernelSU)** | Yes | Yes | Yes | Wipes phone data |

---

## How to Use

Choose the method that best fits your workflow:

### Method 1: One-Click PC Script (Recommended — Fastest & Simplest)

If you have a computer, this takes **literally 5 seconds** and requires no extra apps:

1. **Enable USB Debugging on your Pixel**:
   * Go to **Settings → About Phone** → tap **Build Number** 7 times.
   * Go to **Settings → System → Developer Options** → enable **USB Debugging**.
2. **Connect phone to your PC via USB** and tap *"Always allow from this computer"*.
3. **Run the script**:
   * **Windows**: Double-click [`mute.bat`](mute.bat).
   * **macOS / Linux**: Open Terminal and run:
     ```bash
     chmod +x mute.sh
     ./mute.sh
     ```
4. **Done!** The camera shutter is completely silent. It will remain silent until you reboot your phone.

---

### Method 2: SilentPixel App + Shizuku (On-Device Toggle)

If you prefer an on-device toggle without needing a computer:

1. Download and install **`SilentPixel.apk`** from [Releases](https://github.com/Morendais/SilentPixel/releases).
2. Install **Shizuku** from Google Play:  
   https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api
3. Open Shizuku and start it via **Wireless Debugging**:
   * Tap *"Start via Wireless debugging"*.
   * Developer Options → Wireless Debugging → *"Pair device with pairing code"*.
   * Enter the 6-digit pairing code in the Shizuku prompt.
4. Open **SilentPixel** and tap **"Silence Shutter"**.
5. **Done!** Camera shutter sound is now completely muted on your stock camera.

---

## Technical Details: Audio Volume Group 7

### Why traditional volume sliders fail
In AOSP's `AudioService`, the camera sound stream (`STREAM_SYSTEM_ENFORCED`, Stream 7) is hardcoded to reject volume reduction when `isCameraSoundForced()` is true:
```java
// AudioService.java
if (mCameraSoundForced) {
    // Normal stream volume adjustment for Stream 7 is silently ignored!
}
```

### The Solution: Direct Audio Policy Group Routing
Modern Android abstracts stream types into **Audio Volume Groups**.
* `STREAM_SYSTEM_ENFORCED` is mapped to **Volume Group 7** (`AUDIO_STREAM_ENFORCED_AUDIBLE`).
* The low-level Android audio command-line tool `cmd audio` allows setting volume directly on the group level:
  ```bash
  cmd audio set-group-volume 7 0
  cmd audio adj-group-volume 7 MUTE
  cmd audio set-group-volume 2 0
  cmd audio adj-group-volume 2 MUTE
  ```
This directly forces the hardware speaker volume index for the enforced group to `0` and sets its status to `Muted: true`. 
* **Media playback (Volume Group 4)** is completely unaffected — your Spotify, YouTube, or podcasts keep playing with zero interruption.
* **Voice calls (Volume Group 1)** and notifications remain fully functional.

---

## Verification

To verify that the enforced audio group is currently muted on your device, run:
```bash
adb shell "dumpsys audio | grep -E 'VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE' -A 4"
```

Expected output:
```text
- VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE:
   Muted: true
   Min: 0
   Max: 7
   Current: 2 (speaker): 0
```

---

## Frequently Asked Questions (FAQ)

#### Does this survive a phone reboot?
In Android, audio volume groups are loaded into RAM upon boot. When the phone is rebooted, Android resets all volume groups to default. 
* With **Method 1 (PC)**: Simply plug in your phone and double-click `mute.bat`.
* With **Method 2 (Shizuku)**: Open SilentPixel and tap "Re-apply Silence".

#### Will Google patch this?
Unlikely. The `cmd audio` CLI tool is an official developer interface provided by AOSP for Audio Policy HAL testing. Because modifying volume groups requires developer permissions (`AID_SHELL`), Google treats it as an intentional developer configuration. It does not tamper with system partitions or trigger Play Integrity / SafetyNet flags.

#### Does this break phone ringers, alarms, or timers?
No. Alarm audio uses `AUDIO_STREAM_ALARM` (Group 6), ringers use `AUDIO_STREAM_RING` (Group 2), and timers use their respective audio usage profiles. The commands target only `AUDIO_STREAM_ENFORCED_AUDIBLE`.

---

## Privacy & Security
* **Zero network tracking**: SilentPixel contains no analytics, telemetry, trackers, or ads.
* **System integrity preserved**: Does not modify `/system`, `/vendor`, or `/data`.
* **Open Source**: Full source code is available in this repository under the MIT License.

---

## License
This project is licensed under the [MIT License](LICENSE).
