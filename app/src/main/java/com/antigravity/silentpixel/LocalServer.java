package com.antigravity.silentpixel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalServer {
    public static final int PORT = 45678;
    private static volatile boolean isMuted = true;

    public static void main(String[] args) {
        System.out.println("SilentPixel LocalServer started on port " + PORT + ", UID=" + android.os.Process.myUid());
        isMuted = checkAudioGroupMuted();
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", PORT));
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    client.setSoTimeout(3000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    String cmd = in.readLine();
                    if ("MUTE".equals(cmd)) {
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "set-group-volume", "7", "0"}).waitFor();
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "adj-group-volume", "7", "MUTE"}).waitFor();
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "set-group-volume", "2", "0"}).waitFor();
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "adj-group-volume", "2", "MUTE"}).waitFor();
                        isMuted = true;
                        out.println("OK");
                    } else if ("UNMUTE".equals(cmd)) {
                        // Deprecated to avoid triggering AOSP cameraSoundForced lock
                        out.println("OK");
                    } else if ("STATUS".equals(cmd)) {
                        boolean muted = checkAudioGroupMuted();
                        out.println(muted ? "MUTED" : "UNMUTED");
                    } else if ("PING".equals(cmd)) {
                        out.println("PONG");
                    } else {
                        out.println("UNKNOWN");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean checkAudioGroupMuted() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "dumpsys audio | grep -A 2 'VOLUME GROUP AUDIO_STREAM_ENFORCED_AUDIBLE'"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Muted: true")) {
                    p.waitFor();
                    isMuted = true;
                    return true;
                }
                if (line.contains("Muted: false")) {
                    p.waitFor();
                    isMuted = false;
                    return false;
                }
            }
            p.waitFor();
        } catch (Throwable ignored) {}
        return isMuted;
    }
}
