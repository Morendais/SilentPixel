package com.antigravity.silentpixel;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class ShutterMuteTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        boolean isMuted = MuteController.isCameraMuted(this);
        if (isMuted) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("Shutter: Silent");
            tile.setSubtitle("Active");
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Silence Shutter");
            tile.setSubtitle("Tap to mute");
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        MuteController.setMute(this, true);
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel("Shutter: Silent");
        tile.setSubtitle("Active");
        tile.updateTile();
        Toast.makeText(this, "Camera shutter silenced", Toast.LENGTH_SHORT).show();
    }
}
