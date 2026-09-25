package com.antigravity.silentpixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "Received broadcast action: " + action);

        SharedPreferences prefs = context.getSharedPreferences("silent_pixel_prefs", Context.MODE_PRIVATE);
        boolean autoBoot = prefs.getBoolean("auto_boot", false);
        if (!autoBoot) {
            Log.d(TAG, "auto_boot is disabled in preferences");
            return;
        }

        final PendingResult pendingResult = goAsync();

        new Thread(() -> {
            try {
                // Poll every 1.5 seconds, up to 20 times (30 seconds total)
                for (int i = 0; i < 20; i++) {
                    boolean canMute = MuteController.isLocalServerRunning() ||
                                      MuteController.hasShizukuPermission() ||
                                      MuteController.isRootAvailable();

                    if (canMute) {
                        boolean ok = MuteController.setMute(context, true);
                        Log.d(TAG, "Attempted mute in BootReceiver (iteration " + i + "): " + ok);
                        if (ok) {
                            break;
                        }
                    }

                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception during background auto-mute", e);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
