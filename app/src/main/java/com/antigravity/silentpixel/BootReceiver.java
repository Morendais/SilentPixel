package com.antigravity.silentpixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            "android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction())) {

            SharedPreferences prefs = context.getSharedPreferences("silent_pixel_prefs", Context.MODE_PRIVATE);
            boolean autoBoot = prefs.getBoolean("auto_boot", true);

            if (autoBoot) {
                new Thread(() -> {
                    // Give Shizuku service a few seconds to initialize
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}

                    MuteController.muteShutter();
                }).start();
            }
        }
    }
}
