package com.antigravity.silentpixel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import rikka.shizuku.Shizuku;

public class SilentPixelApp extends Application {

    private static final String TAG = "SilentPixelApp";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Shizuku.addBinderReceivedListenerSticky(() -> {
                Log.d(TAG, "Shizuku binder received in Application");
                SharedPreferences prefs = getSharedPreferences("silent_pixel_prefs", MODE_PRIVATE);
                boolean autoBoot = prefs.getBoolean("auto_boot", false);
                if (autoBoot) {
                    new Thread(() -> {
                        for (int i = 0; i < 15; i++) {
                            if (MuteController.hasShizukuPermission()) {
                                boolean ok = MuteController.setMute(SilentPixelApp.this, true);
                                Log.d(TAG, "Auto-silenced on Shizuku connect: " + ok);
                                if (ok) break;
                            }
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException ignored) {}
                        }
                    }).start();
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Failed to register sticky binder listener", t);
        }
    }
}
