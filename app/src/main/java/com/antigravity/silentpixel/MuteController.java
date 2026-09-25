package com.antigravity.silentpixel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import rikka.shizuku.Shizuku;

public class MuteController {

    public static final int STREAM_SYSTEM_ENFORCED = 7;

    public static boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        try {
            if (!isShizukuAvailable()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isRootAvailable() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            int code = p.waitFor();
            return code == 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isCameraMuted(Context context) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int vol = am.getStreamVolume(STREAM_SYSTEM_ENFORCED);
                return vol == 0;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean runCommand(String cmd) {
        if (hasShizukuPermission()) {
            try {
                String[] parts = cmd.split(" ");
                java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                Process p = (Process) m.invoke(null, new Object[]{parts, null, null});
                return p.waitFor() == 0;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (isRootAvailable()) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                return p.waitFor() == 0;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return false;
    }

    public static boolean setMute(boolean mute) {
        if (mute) {
            boolean ok1 = runCommand("cmd audio set-group-volume 7 0");
            boolean ok2 = runCommand("cmd audio adj-group-volume 7 MUTE");
            return ok1 || ok2;
        } else {
            boolean ok1 = runCommand("cmd audio set-group-volume 7 7");
            boolean ok2 = runCommand("cmd audio adj-group-volume 7 UNMUTE");
            return ok1 || ok2;
        }
    }
}
