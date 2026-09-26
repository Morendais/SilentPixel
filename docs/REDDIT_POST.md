# Reddit Post Draft for r/GooglePixel

**Title:**
> Simple way to mute the Japanese Pixel camera shutter sound (Stock Camera, No Root, Music keeps playing)

**Body:**

Hey everyone,

I have a Japanese Google Pixel and the forced loud camera shutter sound was really annoying.

The existing workarounds didn't work for me:
- **GCam mods:** I didn't want a modified camera app, I prefer official stock Google Camera.
- **Play Store mute apps:** Whenever you open the camera, they abruptly pause/glitch your music (Spotify, etc.) and add annoying lag.

I found out that on modern Android, the enforced shutter sound is routed through **Volume Group 7**, which can be muted directly via ADB. Muting it completely silences the camera click, while background music, videos, calls, and notifications keep playing without any interruption.

Because Android resets volume groups on reboot, this works until you restart your phone. After a reboot, you can easily re-apply it in seconds either via the .bat script over USB or directly on your phone using the app with Shizuku.

I put together an open-source project called **SilentPixel**:
- **`mute.bat` (Windows):** Plug phone into PC, double-click the script, done in 5 seconds.
- **Android companion app:** A simple app that works together with Shizuku if you prefer re-applying the mute directly on your phone without a PC.

Tested on Pixel 10 Pro XL.

GitHub repo: https://github.com/Morendais/SilentPixel

Hope this helps anyone who has a Japanese Pixel!
