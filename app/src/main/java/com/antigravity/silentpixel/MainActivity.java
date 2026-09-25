package com.antigravity.silentpixel;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity implements Shizuku.OnRequestPermissionResultListener {

    private static final int SHIZUKU_CODE = 101;
    private TextView tvStatus;
    private TextView tvEngineInfo;
    private Button btnMute;
    private Button btnUnmute;
    private Switch switchBoot;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvEngineInfo = findViewById(R.id.tvEngineInfo);
        btnMute = findViewById(R.id.btnMute);
        btnUnmute = findViewById(R.id.btnUnmute);
        switchBoot = findViewById(R.id.switchBoot);

        prefs = getSharedPreferences("silent_pixel_prefs", MODE_PRIVATE);
        switchBoot.setChecked(prefs.getBoolean("auto_boot", true));

        switchBoot.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("auto_boot", isChecked).apply();
        });

        Shizuku.addRequestPermissionResultListener(this);

        btnMute.setOnClickListener(v -> {
            ensurePermissionsAndRun(true);
        });

        btnUnmute.setOnClickListener(v -> {
            ensurePermissionsAndRun(false);
        });

        updateUI();
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
        if (MuteController.hasShizukuPermission()) {
            tvEngineInfo.setText("Движок: Shizuku (Активен)");
        } else if (MuteController.isShizukuAvailable()) {
            tvEngineInfo.setText("Shizuku найден, требуется разрешение...");
        } else if (MuteController.isRootAvailable()) {
            tvEngineInfo.setText("Движок: Root (SuperSU/Magisk)");
        } else {
            tvEngineInfo.setText("Требуется запуск Shizuku (по Wi-Fi)");
        }

        String st = MuteController.getStatusString();
        if ("MUTED".equals(st)) {
            tvStatus.setText(R.string.status_muted);
        } else if ("UNMUTED".equals(st)) {
            tvStatus.setText(R.string.status_unmuted);
        } else {
            tvStatus.setText(R.string.status_unknown);
        }
    }

    private void ensurePermissionsAndRun(boolean mute) {
        if (MuteController.isShizukuAvailable()) {
            if (!MuteController.hasShizukuPermission()) {
                Shizuku.requestPermission(SHIZUKU_CODE);
                return;
            }
        }

        executeAction(mute);
    }

    private void executeAction(boolean mute) {
        new Thread(() -> {
            boolean success = mute ? MuteController.muteShutter() : MuteController.unmuteShutter();
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, mute ? "Затвор камеры заглушен!" : "Звук затвора возвращен!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка выполнения. Проверьте статус Shizuku.", Toast.LENGTH_LONG).show();
                }
                updateUI();
            });
        }).start();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение Shizuku получено!", Toast.LENGTH_SHORT).show();
                updateUI();
            } else {
                Toast.makeText(this, "В доступе Shizuku отказано", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
