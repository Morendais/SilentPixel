package com.antigravity.silentpixel;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

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
            tile.setLabel("Shutter: Muted");
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Shutter: Loud");
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        boolean currentlyMuted = (tile.getState() == Tile.STATE_ACTIVE);
        MuteController.setMute(!currentlyMuted);
        updateTile();
    }
}
