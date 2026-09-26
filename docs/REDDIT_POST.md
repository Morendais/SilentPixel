# Reddit Post Draft for r/GooglePixel

**Title:**
> Found a clean way to mute the Japanese Pixel camera shutter sound (Stock Camera, No Root, Music doesn't cut out)

**Body:**

Hey r/GooglePixel,

Like many people here, I bought a Japanese Google Pixel. It's a great phone, but that mandatory maximum-volume camera shutter click was driving me crazy — especially in quiet rooms, museums, or when trying to take candid photos of pets.

The existing workarounds were annoying:
* **GCam mod ports:** I didn't want to lose the official Stock Google Camera with lockscreen double-tap shortcuts and regular Play Store updates.
* **Play Store "Mute" apps:** They mute the camera by hijacking audio focus, which causes Spotify/music to abruptly cut out or glitch every single time you open the camera, plus there's an annoying startup delay.

After digging into Android's audio system, I found that modern Android (Android 11–16) maps the enforced shutter sound to **Volume Group 7**, which can be muted directly via ADB without affecting anything else. Background music, videos, phone calls, and alarms keep playing 100% uninterrupted.

I put together an open-source repo called **SilentPixel**:

1. **Windows 1-click script (`mute.bat`):** Just connect your phone to your PC via USB and double-click `mute.bat`. Takes 5 seconds and stays muted until you reboot your phone.
2. **Companion app:** A tiny Material 3 app for toggling the mute on-device using Shizuku.

No root, no data wipe, no battery drain.

**GitHub Repository:** https://github.com/Morendais/SilentPixel

Hope this helps anyone else dealing with Japanese Pixel shutter sounds!
