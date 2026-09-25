package com.antigravity.silentpixel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalServer {
    public static final int PORT = 45678;

    public static void main(String[] args) {
        System.out.println("SilentPixel LocalServer started on port " + PORT + ", UID=" + android.os.Process.myUid());
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
                        out.println("OK");
                    } else if ("UNMUTE".equals(cmd)) {
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "set-group-volume", "7", "7"}).waitFor();
                        Runtime.getRuntime().exec(new String[]{"cmd", "audio", "adj-group-volume", "7", "UNMUTE"}).waitFor();
                        out.println("OK");
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
}
