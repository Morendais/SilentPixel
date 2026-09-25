package com.antigravity.silentpixel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import rikka.shizuku.Shizuku;

public class MuteController {

    public static final int STREAM_SYSTEM_ENFORCED = 7;

    public static boolean isLocalServerRunning() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", LocalServer.PORT), 500);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println("PING");
            String resp = in.readLine();
            return "PONG".equals(resp);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean sendLocalServerCommand(String cmd) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", LocalServer.PORT), 1500);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println(cmd);
            String resp = in.readLine();
            return "OK".equals(resp) || "MUTED".equals(resp) || "UNMUTED".equals(resp);
        } catch (Throwable t) {
            return false;
        }
    }

    public static String getLocalServerStatus() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", LocalServer.PORT), 1500);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println("STATUS");
            return in.readLine();
        } catch (Throwable t) {
            return null;
        }
    }

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
        // 1. Check directly via LocalServer if running
        if (isLocalServerRunning()) {
            String status = getLocalServerStatus();
            if ("MUTED".equals(status)) return true;
            if ("UNMUTED".equals(status)) return false;
        }

        // 2. Fallback to cached preference
        SharedPreferences prefs = context.getSharedPreferences("silent_pixel_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_muted", false);
    }

    public static boolean setMute(Context context, boolean mute) {
        boolean success = false;

        // 1. Try embedded LocalServer first
        if (isLocalServerRunning()) {
            success = sendLocalServerCommand(mute ? "MUTE" : "UNMUTE");
        }

        // 2. Try Shizuku if available
        if (!success && hasShizukuPermission()) {
            try {
                String cmd1 = mute ? "cmd audio set-group-volume 7 0" : "cmd audio set-group-volume 7 7";
                String cmd2 = mute ? "cmd audio adj-group-volume 7 MUTE" : "cmd audio adj-group-volume 7 UNMUTE";
                String cmd3 = mute ? "cmd audio set-group-volume 2 0" : "cmd audio set-group-volume 2 7";
                String cmd4 = mute ? "cmd audio adj-group-volume 2 MUTE" : "cmd audio adj-group-volume 2 UNMUTE";
                java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                ((Process) m.invoke(null, new Object[]{cmd1.split(" "), null, null})).waitFor();
                ((Process) m.invoke(null, new Object[]{cmd2.split(" "), null, null})).waitFor();
                ((Process) m.invoke(null, new Object[]{cmd3.split(" "), null, null})).waitFor();
                ((Process) m.invoke(null, new Object[]{cmd4.split(" "), null, null})).waitFor();
                success = true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 3. Try Root
        if (!success && isRootAvailable()) {
            try {
                String cmd = mute ? "cmd audio set-group-volume 7 0 && cmd audio adj-group-volume 7 MUTE && cmd audio set-group-volume 2 0 && cmd audio adj-group-volume 2 MUTE"
                                  : "cmd audio set-group-volume 7 7 && cmd audio adj-group-volume 7 UNMUTE && cmd audio set-group-volume 2 7 && cmd audio adj-group-volume 2 UNMUTE";
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                success = (p.waitFor() == 0);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (success) {
            context.getSharedPreferences("silent_pixel_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("is_muted", mute).apply();
        }

        return success;
    }
}
