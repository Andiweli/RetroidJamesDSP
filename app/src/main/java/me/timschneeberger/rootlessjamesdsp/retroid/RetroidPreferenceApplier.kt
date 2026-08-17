package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import me.timschneeberger.rootlessjamesdsp.BuildConfig
import me.timschneeberger.rootlessjamesdsp.service.RootAudioProcessorService
import me.timschneeberger.rootlessjamesdsp.utils.Constants

object RetroidPreferenceApplier {
    const val RETROID_PREFS = "retroid_jdsp"
    const val KEY_APPLY_ON_BOOT = "apply_on_boot"
    const val KEY_ACTIVE_PRESET_ID = "active_preset_id"
    const val KEY_ACTIVE_PRESET_TITLE = "active_preset_title"

    private const val OUTPUT = Constants.PREF_OUTPUT
    private const val GRAPHIC_EQ = Constants.PREF_GEQ
    private const val STEREO_WIDE = Constants.PREF_STEREOWIDE
    private const val AUDIO_FORMAT = "audio_format"
    private const val APP = Constants.PREF_APP
    private const val VAR = Constants.PREF_VAR

    fun applyPreset(context: Context, preset: RetroidPreset, enableStereoWide: Boolean = true) {
        val app = context.applicationContext

        // v32: store the active Retroid preset BEFORE touching any DSP namespace.
        // The diagnostic guard can then recover immediately if JamesDSP or the audio policy
        // stack changes preferences while the preset is being applied.
        app.getSharedPreferences(RETROID_PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE_PRESET_ID, preset.id)
            .putString(KEY_ACTIVE_PRESET_TITLE, preset.title)
            .commit()

        app.getSharedPreferences(VAR, Context.MODE_PRIVATE).edit()
            .putString("retroid_last_preset", preset.title)
            .commit()

        app.getSharedPreferences(OUTPUT, Context.MODE_PRIVATE).edit()
            .putFloat("limiter_threshold", -0.10f)
            .putFloat("limiter_release", 500.00f)
            .putFloat("output_postgain", 15.00f)
            .commit()

        app.getSharedPreferences(GRAPHIC_EQ, Context.MODE_PRIVATE).edit()
            .putBoolean("geq_enable", true)
            .putString("geq_nodes", preset.graphicEq)
            .commit()

        app.getSharedPreferences(STEREO_WIDE, Context.MODE_PRIVATE).edit()
            .putBoolean("stereowide_enable", enableStereoWide)
            .putFloat("stereowide_mode", if (enableStereoWide) 2.0f else 0.0f)
            .commit()

        app.getSharedPreferences(AUDIO_FORMAT, Context.MODE_PRIVATE).edit()
            .putBoolean("audioformat_processing", true)
            .commit()

        app.getSharedPreferences(APP, Context.MODE_PRIVATE).edit()
            .putBoolean("powered_on", true)
            .commit()

        RetroidDspSettingsGuard.restoreIfNeeded(app, "manual preset apply", force = true)

        notifyJamesDsp(app)
        requestSingleLegacyReopen(app, "manual preset apply")
    }

    private fun requestSingleLegacyReopen(context: Context, reason: String) {
        val app = context.applicationContext
        Thread {
            try {
                // Manual apply already wrote the preset and notified JamesDSP. Wait a little longer
                // and reopen session 0 once, but do not force another pref sync before the reopen.
                Thread.sleep(1500L)
                Log.w("RetroidPreset", "Requesting delayed safe root legacy session reopen after $reason")
                RootAudioProcessorService.updateLegacyMode(app, true)
                Thread.sleep(450L)
                RetroidAudioWarmup.playMediaWarmup("manual preset apply delayed legacy reopen")
                Thread.sleep(700L)
                RetroidDspSettingsGuard.restoreIfNeeded(app, "$reason verify after media warmup", force = false)
            } catch (t: Throwable) {
                Log.e("RetroidPreset", "Delayed safe root legacy session reopen failed after $reason", t)
            }
        }.start()
    }

    fun notifyJamesDsp(context: Context) {
        val app = context.applicationContext
        LocalBroadcastManager.getInstance(app).sendBroadcast(Intent(Constants.ACTION_GRAPHIC_EQ_CHANGED))
        LocalBroadcastManager.getInstance(app).sendBroadcast(Intent(Constants.ACTION_PREFERENCES_UPDATED))
        LocalBroadcastManager.getInstance(app).sendBroadcast(Intent(Constants.ACTION_SERVICE_RELOAD_LIVEPROG))

        // Keep the old package-scoped broadcasts too. They are harmless in the root build and useful
        // for logcat comparison with the earlier v28/v31 behavior.
        app.sendBroadcast(Intent(Constants.ACTION_GRAPHIC_EQ_CHANGED).setPackage(BuildConfig.APPLICATION_ID))
        app.sendBroadcast(Intent(Constants.ACTION_PREFERENCES_UPDATED).setPackage(BuildConfig.APPLICATION_ID))
        app.sendBroadcast(Intent(Constants.ACTION_SERVICE_RELOAD_LIVEPROG).setPackage(BuildConfig.APPLICATION_ID))
    }
}
