package com.antigravity.silentpixel;

import android.content.Context;
import android.content.pm.PackageManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import rikka.shizuku.Shizuku;

public class MuteController {

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

    public static boolean runCommand(String cmd) {
        if (hasShizukuPermission()) {
            try {
                String[] parts = cmd.split(" ");
                java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                Process p = (Process) m.invoke(null, parts, null, null);
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

    public static boolean muteShutter() {
        boolean ok1 = runCommand("cmd audio set-group-volume 7 0");
        boolean ok2 = runCommand("cmd audio adj-group-volume 7 MUTE");
        return ok1 || ok2;
    }

    public static boolean unmuteShutter() {
        boolean ok1 = runCommand("cmd audio set-group-volume 7 7");
        boolean ok2 = runCommand("cmd audio adj-group-volume 7 UNMUTE");
        return ok1 || ok2;
    }

    public static String getStatusString() {
        if (hasShizukuPermission()) {
            try {
                java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                Process p = (Process) m.invoke(null, new Object[]{new String[]{"cmd", "audio", "get-stream-volume", "7"}, null, null});
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                p.waitFor();
                if (line != null && line.contains("-> 0")) {
                    return "MUTED";
                } else if (line != null && line.contains("-> 7")) {
                    return "UNMUTED";
                }
            } catch (Throwable ignored) {}
        }
        return "UNKNOWN";
    }
}
