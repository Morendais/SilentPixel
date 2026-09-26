# Reddit Post Draft for r/GooglePixel

**Title:**
> Muting the Japanese Pixel camera shutter sound on official Stock Google Camera (No Root, No SIM tricks, No interrupted Spotify/Music)

**Body:**

Hey r/GooglePixel,

Like many people here, I bought a Japanese Google Pixel (SKU AJP / GYPW4). It's an incredible phone, but that mandatory camera shutter sound at maximum volume drove me crazy — especially in quiet rooms, museums, or when trying to take candid photos of my pets or kids.

### Why existing solutions didn't work for me:
1. **GCam Ports (AGC / BSG / BigKaka):** They do let you toggle shutter sound off, but I personally wanted to stay on **100% official stock Google Camera** with seamless lock-screen double-tap power button shortcuts, official Play Store updates, and zero third-party processing quirks.
2. **Play Store "Mute Camera" apps:** They rely on Accessibility Services and aggressively hijack global `AUDIOFOCUS`. Every single time you open the camera, your Spotify, Apple Music, or YouTube abruptly cuts off or glitches. Plus, they introduce a 1–2 second launch delay and sometimes still leak a shutter click if music is streaming.
3. **Foreign SIM roaming:** Android only disables the sound if you connect to a physical non-Japanese base station. If you're in airplane mode, on subway Wi-Fi, or have weak coverage, the shutter sound forcefully comes back.

---

### The Discovery: Android Audio Volume Group 7
I started digging into AOSP's `AudioService` internals. On modern Android (11 through 16), stream volumes are mapped to **Audio Volume Groups**.

Google hardcoded `STREAM_SYSTEM_ENFORCED` (Stream 7) to block normal stream volume reduction on Japanese SKUs. **However**, the low-level Android audio command dispatcher (`cmd audio`) lets you address hardware volume groups directly:

```bash
cmd audio set-group-volume 7 0
cmd audio adj-group-volume 7 MUTE
cmd audio set-group-volume 2 0
cmd audio adj-group-volume 2 MUTE
```

By muting **Volume Group 7**, the camera shutter sound is **100% silenced**, while:
* **Background music (Spotify, YouTube Music, Podcasts) plays completely uninterrupted!**
* Phone calls, alarms, and timers work normally.
* Official stock Google Camera opens with zero lag.

---

### The Tool: SilentPixel (Open Source)
I packaged this into a tiny open-source tool called **SilentPixel**:
* **1-Click PC script (`mute.bat` / `mute.sh`):** If you plug your phone into a PC occasionally, double-click `mute.bat` once and you're done until you reboot.
* **SilentPixel App (~49 KB, Material 3):** If you prefer an on-device toggle, there's a minimal app with Shizuku support that can automatically re-apply the silence on restart.
* **Termux one-liner:** If you're a power user with Rish or local ADB.

**GitHub Repository:** [https://github.com/YOUR_USERNAME/SilentPixel](https://github.com/YOUR_USERNAME/SilentPixel)  
*(Full source code, step-by-step instructions, and APK release available)*

Hope this helps anyone else struggling with the Japanese shutter sound on their Pixels! Tested and verified on Pixel 10 Pro XL, Pixel 9, 8, and 7 series.
