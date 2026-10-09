package com.batterypet.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restarts the pet overlay after a device reboot if the user left it enabled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED &&
            PetKit.prefs(context).getBoolean("overlay_enabled", false)
        ) {
            try {
                context.startForegroundService(Intent(context, PetOverlayService::class.java))
            } catch (_: Exception) {
                // Background foreground-service start not allowed on this device state.
            }
        }
    }
}
