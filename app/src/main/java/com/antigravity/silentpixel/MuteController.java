package com.antigravity.silentpixel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
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
            s.connect(new InetSocketAddress("127.0.0.1", LocalServer.PORT), 1000);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println(cmd);
            String resp = in.readLine();
            return "OK".equals(resp);
        } catch (Throwable t) {
            return false;
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
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int vol = am.getStreamVolume(STREAM_SYSTEM_ENFORCED);
                return vol == 0;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean setMute(boolean mute) {
        // 1. Try embedded LocalServer first (zero external dependencies)
        if (isLocalServerRunning()) {
            return sendLocalServerCommand(mute ? "MUTE" : "UNMUTE");
        }

        // 2. Try Shizuku if available
        if (hasShizukuPermission()) {
            try {
                String cmd1 = mute ? "cmd audio set-group-volume 7 0" : "cmd audio set-group-volume 7 7";
                String cmd2 = mute ? "cmd audio adj-group-volume 7 MUTE" : "cmd audio adj-group-volume 7 UNMUTE";
                java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                Process p1 = (Process) m.invoke(null, new Object[]{cmd1.split(" "), null, null});
                p1.waitFor();
                Process p2 = (Process) m.invoke(null, new Object[]{cmd2.split(" "), null, null});
                p2.waitFor();
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 3. Try Root
        if (isRootAvailable()) {
            try {
                String cmd = mute ? "cmd audio set-group-volume 7 0 && cmd audio adj-group-volume 7 MUTE"
                                  : "cmd audio set-group-volume 7 7 && cmd audio adj-group-volume 7 UNMUTE";
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                return p.waitFor() == 0;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return false;
    }
}
