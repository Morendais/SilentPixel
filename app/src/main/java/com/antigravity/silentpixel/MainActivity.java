package com.antigravity.silentpixel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity implements Shizuku.OnRequestPermissionResultListener {

    private static final int SHIZUKU_CODE = 101;
    private static final String GITHUB_URL = "https://github.com/Morendais/SilentPixel";

    private Button btnSilence;
    private TextView tvStatusBadge;
    private LinearLayout cardMainAction;
    private LinearLayout rowGithub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSilence = findViewById(R.id.btnSilence);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        cardMainAction = findViewById(R.id.cardMainAction);
        rowGithub = findViewById(R.id.rowGithub);

        // Hero card & button click: trigger silence action
        btnSilence.setOnClickListener(v -> handleSilenceAction());
        cardMainAction.setOnClickListener(v -> handleSilenceAction());

        // GitHub Repository row click
        if (rowGithub != null) {
            rowGithub.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
            .setMessage("Android resets camera sound on every reboot.\n\nTo activate silent shutter:\n• Connect phone to PC and run mute.bat (1 click), or\n• Install Shizuku for on-device activation.")
            .setPositiveButton("OK", null)
            .show();
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
