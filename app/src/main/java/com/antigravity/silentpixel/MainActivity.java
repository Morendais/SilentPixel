package com.antigravity.silentpixel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity implements Shizuku.OnRequestPermissionResultListener {

    private static final int SHIZUKU_CODE = 101;
    private Switch switchMain;
    private Switch switchBoot;
    private LinearLayout cardMainSwitch;
    private LinearLayout rowBattery;
    private TextView tvEngineInfo;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchMain = findViewById(R.id.switchMain);
        switchBoot = findViewById(R.id.switchBoot);
        cardMainSwitch = findViewById(R.id.cardMainSwitch);
        rowBattery = findViewById(R.id.rowBattery);
        tvEngineInfo = findViewById(R.id.tvEngineInfo);

        prefs = getSharedPreferences("silent_pixel_prefs", MODE_PRIVATE);

        // Auto boot switch setup
        boolean autoBootEnabled = prefs.getBoolean("auto_boot", false);
        switchBoot.setChecked(autoBootEnabled);

        switchBoot.setOnClickListener(v -> {
            boolean wantEnabled = switchBoot.isChecked();
            if (wantEnabled) {
                if (!isIgnoringBatteryOptimizations()) {
                    switchBoot.setChecked(false);
                    showBatteryOptimizationDialog();
                } else {
                    prefs.edit().putBoolean("auto_boot", true).apply();
                }
            } else {
                prefs.edit().putBoolean("auto_boot", false).apply();
            }
        });

        // ONLY cardMainSwitch handles the click to prevent double-firing events
        cardMainSwitch.setOnClickListener(v -> {
            boolean targetMuteState = !switchMain.isChecked();
            switchMain.setChecked(targetMuteState);
            handleToggle(targetMuteState);
        });

        rowBattery.setOnClickListener(v -> openBatterySettings());

        Shizuku.addRequestPermissionResultListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();

        if (prefs.getBoolean("pending_boot_enable", false)) {
            if (isIgnoringBatteryOptimizations()) {
                switchBoot.setChecked(true);
                prefs.edit().putBoolean("auto_boot", true).putBoolean("pending_boot_enable", false).apply();
                Toast.makeText(this, "Auto-silence on restart enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(this);
    }

    private boolean isIgnoringBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            return pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        return false;
    }

    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Unrestricted Battery Required")
            .setMessage("To automatically silence the camera whenever your Pixel reboots, Android requires battery usage for this app to be set to 'Unrestricted'.")
            .setPositiveButton("Settings", (dialog, which) -> {
                prefs.edit().putBoolean("pending_boot_enable", true).apply();
                openBatterySettings();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                switchBoot.setChecked(false);
                prefs.edit().putBoolean("auto_boot", false).apply();
            })
            .show();
    }

    private void updateUI() {
        new Thread(() -> {
            boolean isMuted = MuteController.isCameraMuted(this);
            boolean localOk = MuteController.isLocalServerRunning();

            runOnUiThread(() -> {
                switchMain.setChecked(isMuted);

                if (localOk) {
                    tvEngineInfo.setText("Engine: Active • Hardware SKU: Japan");
                } else if (MuteController.hasShizukuPermission()) {
                    tvEngineInfo.setText("Engine: Shizuku • Hardware SKU: Japan");
                } else if (MuteController.isRootAvailable()) {
                    tvEngineInfo.setText("Engine: Root • Hardware SKU: Japan");
                } else {
                    tvEngineInfo.setText("Engine: Standalone • Hardware SKU: Japan");
                }
            });
        }).start();
    }

    private void handleToggle(boolean mute) {
        cardMainSwitch.setEnabled(false);
        new Thread(() -> {
            boolean ok = MuteController.setMute(this, mute);
            runOnUiThread(() -> {
                cardMainSwitch.setEnabled(true);
                if (ok) {
                    switchMain.setChecked(mute);
                    Toast.makeText(this, mute ? "Shutter silenced" : "Shutter sound restored", Toast.LENGTH_SHORT).show();
                } else {
                    switchMain.setChecked(!mute);
                    Toast.makeText(this, "Failed to toggle sound", Toast.LENGTH_SHORT).show();
                }
                updateUI();
            });
        }).start();
    }

    private void openBatterySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        }
    }
}
