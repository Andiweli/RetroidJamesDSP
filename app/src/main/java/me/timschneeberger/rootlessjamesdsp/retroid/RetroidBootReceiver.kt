package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RetroidBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences(RetroidPreferenceApplier.RETROID_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(RetroidPreferenceApplier.KEY_APPLY_ON_BOOT, false)) {
            RetroidSystemPatch.enable(context)
        }
    }
}
