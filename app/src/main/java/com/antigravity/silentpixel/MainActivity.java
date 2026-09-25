package com.antigravity.silentpixel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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
        switchBoot.setChecked(prefs.getBoolean("auto_boot", true));

        switchBoot.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("auto_boot", isChecked).apply();
        });

        cardMainSwitch.setOnClickListener(v -> {
            switchMain.toggle();
        });

        switchMain.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!btn.isPressed() && !cardMainSwitch.isPressed()) return;
            handleToggle(isChecked);
        });

        rowBattery.setOnClickListener(v -> {
            openBatterySettings();
        });

        Shizuku.addRequestPermissionResultListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(this);
    }

    private void updateUI() {
        boolean isMuted = MuteController.isCameraMuted(this);
        switchMain.setChecked(isMuted);

        if (MuteController.hasShizukuPermission()) {
            tvEngineInfo.setText("Engine: Shizuku (Active) • SKU: Japan (GYPW4)");
        } else if (MuteController.isRootAvailable()) {
            tvEngineInfo.setText("Engine: Root (Active) • SKU: Japan (GYPW4)");
        } else {
            tvEngineInfo.setText("Status: Ready • SKU: Japan (GYPW4)");
        }
    }

    private void handleToggle(boolean mute) {
        if (MuteController.isShizukuAvailable() && !MuteController.hasShizukuPermission()) {
            Shizuku.requestPermission(SHIZUKU_CODE);
            switchMain.setChecked(!mute);
            return;
        }

        if (!MuteController.hasShizukuPermission() && !MuteController.isRootAvailable()) {
            showSetupDialog();
            switchMain.setChecked(!mute);
            return;
        }

        new Thread(() -> {
            boolean ok = MuteController.setMute(mute);
            runOnUiThread(() -> {
                if (ok) {
                    Toast.makeText(this, mute ? "Shutter silenced" : "Shutter sound restored", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to apply audio setting", Toast.LENGTH_SHORT).show();
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

    private void showSetupDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("To control system audio groups without Root, please grant access via Shizuku (Wireless Debugging) or run the one-line command via PC once:\n\nadb shell \"cmd audio set-group-volume 7 0\"")
            .setPositiveButton("OK", null)
            .show();
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
