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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity implements Shizuku.OnRequestPermissionResultListener {

    private static final int SHIZUKU_CODE = 101;
    private Button btnSilence;
    private TextView tvStatusBadge;
    private LinearLayout cardMainAction;
    private Switch switchBoot;
    private LinearLayout rowBoot;
    private LinearLayout rowBattery;
    private SharedPreferences prefs;
    private volatile boolean isUpdatingBootUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSilence = findViewById(R.id.btnSilence);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        cardMainAction = findViewById(R.id.cardMainAction);
        switchBoot = findViewById(R.id.switchBoot);
        rowBoot = findViewById(R.id.rowBoot);
        rowBattery = findViewById(R.id.rowBattery);

        prefs = getSharedPreferences("silent_pixel_prefs", MODE_PRIVATE);

        // Auto boot switch setup
        boolean autoBootEnabled = prefs.getBoolean("auto_boot", false);
        isUpdatingBootUi = true;
        switchBoot.setChecked(autoBootEnabled);
        isUpdatingBootUi = false;

        switchBoot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingBootUi) return;
            if (isChecked) {
                if (!isIgnoringBatteryOptimizations()) {
                    isUpdatingBootUi = true;
                    switchBoot.setChecked(false);
                    isUpdatingBootUi = false;
                    showBatteryOptimizationDialog();
                } else {
                    prefs.edit().putBoolean("auto_boot", true).apply();
                }
            } else {
                prefs.edit().putBoolean("auto_boot", false).apply();
            }
        });

        if (rowBoot != null) {
            rowBoot.setOnClickListener(v -> switchBoot.toggle());
        }

        // Hero card & button click: trigger silence action
        btnSilence.setOnClickListener(v -> handleSilenceAction());
        cardMainAction.setOnClickListener(v -> handleSilenceAction());

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
            boolean canMute = MuteController.isLocalServerRunning() || MuteController.hasShizukuPermission() || MuteController.isRootAvailable();

            runOnUiThread(() -> {
                if (isMuted) {
                    tvStatusBadge.setText(R.string.status_silenced);
                    tvStatusBadge.setBackground(getDrawable(R.drawable.bg_badge_active));
                    tvStatusBadge.setTextColor(getColor(R.color.md3_status_active_text));
                    btnSilence.setText(R.string.btn_silence_reapply);
                } else if (canMute) {
                    tvStatusBadge.setText(R.string.status_not_silenced);
                    tvStatusBadge.setBackground(getDrawable(R.drawable.bg_badge_inactive));
                    tvStatusBadge.setTextColor(getColor(R.color.md3_status_inactive_text));
                    btnSilence.setText(R.string.btn_silence_shutter);
                } else {
                    tvStatusBadge.setText(R.string.status_needs_activation);
                    tvStatusBadge.setBackground(getDrawable(R.drawable.bg_badge_inactive));
                    tvStatusBadge.setTextColor(getColor(R.color.md3_status_inactive_text));
                    btnSilence.setText(R.string.btn_silence_shutter);
                }
            });
        }).start();
    }

    private void handleSilenceAction() {
        btnSilence.setEnabled(false);
        cardMainAction.setEnabled(false);

        new Thread(() -> {
            boolean canMute = MuteController.isLocalServerRunning() || MuteController.hasShizukuPermission() || MuteController.isRootAvailable();
            if (!canMute) {
                runOnUiThread(() -> {
                    btnSilence.setEnabled(true);
                    cardMainAction.setEnabled(true);
                    if (MuteController.isShizukuAvailable() && !MuteController.hasShizukuPermission()) {
                        Shizuku.requestPermission(SHIZUKU_CODE);
                    } else {
                        showActivationDialog();
                    }
                });
                return;
            }

            runOnUiThread(() -> btnSilence.setText("Silencing..."));
            boolean ok = MuteController.setMute(this, true);

            runOnUiThread(() -> {
                btnSilence.setEnabled(true);
                cardMainAction.setEnabled(true);
                if (ok) {
                    tvStatusBadge.setText(R.string.status_silenced);
                    tvStatusBadge.setBackground(getDrawable(R.drawable.bg_badge_active));
                    tvStatusBadge.setTextColor(getColor(R.color.md3_status_active_text));
                    btnSilence.setText(R.string.btn_silence_reapply);
                    Toast.makeText(this, "Camera shutter silenced ✓", Toast.LENGTH_SHORT).show();
                } else {
                    btnSilence.setText(R.string.btn_silence_shutter);
                    showActivationDialog();
                }
            });
        }).start();
    }

    private void showActivationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Activation Required")
            .setMessage("Android resets camera sound on every reboot.\n\nTo re-activate the silent shutter:\n• Connect phone to PC and run mute.bat (1 click), or\n• Install Shizuku for automated on-device activation on boot.")
            .setPositiveButton("OK", null)
            .show();
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
                handleSilenceAction();
            }
        }
    }
}
