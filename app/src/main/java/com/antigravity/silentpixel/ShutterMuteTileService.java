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

        String st = MuteController.getStatusString();
        if ("MUTED".equals(st)) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("Затвор: Выкл");
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Затвор: Вкл");
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        if (tile.getState() == Tile.STATE_ACTIVE) {
            MuteController.unmuteShutter();
        } else {
            MuteController.muteShutter();
        }
        updateTile();
    }
}
