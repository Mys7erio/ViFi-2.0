package xyz.alvn.vifi20

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class QuickSettingLoginTile : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (isLocked) {
            unlockAndRun {
                handleWifiLogin()
            }
        } else {
            handleWifiLogin()
        }
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }


    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    private fun handleWifiLogin() {
        val (username, password) = getCredentials(ctx = this)
        Log.e("VIFIDBG", "Username: $username, Password: $password")
        sendLoginRequest(this, username, password)
    }
}