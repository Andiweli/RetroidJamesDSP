package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import me.timschneeberger.rootlessjamesdsp.utils.Constants
import kotlin.math.abs

/**
 * Retroid private build: keep the selected Retroid DSP profile sticky.
 *
 * v36 keeps the v35 Flip 2 Quiet default seed, but writes the full private default
 * during app init before the first root legacy session can be opened. AudioPolicy restart
 * handling deliberately avoids forcing preferences while the AudioEffect is detached.
 */
object RetroidDspSettingsGuard : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val TAG = "RetroidDspGuard"

    private const val OUTPUT = Constants.PREF_OUTPUT
    private const val GRAPHIC_EQ = Constants.PREF_GEQ
    private const val STEREO_WIDE = Constants.PREF_STEREOWIDE
    private const val AUDIO_FORMAT = "audio_format"
    private const val APP = Constants.PREF_APP

    private const val KEY_LIMITER_THRESHOLD = "limiter_threshold"
    private const val KEY_LIMITER_RELEASE = "limiter_release"
    private const val KEY_OUTPUT_POSTGAIN = "output_postgain"
    private const val KEY_GEQ_ENABLE = "geq_enable"
    private const val KEY_GEQ_NODES = "geq_nodes"
    private const val KEY_STEREOWIDE_ENABLE = "stereowide_enable"
    private const val KEY_STEREOWIDE_MODE = "stereowide_mode"
    private const val KEY_AUDIOFORMAT_PROCESSING = "audioformat_processing"
    private const val KEY_POWERED_ON = "powered_on"

    private const val EXPECTED_LIMITER_THRESHOLD = -0.10f
    private const val EXPECTED_LIMITER_RELEASE = 500.00f
    private const val EXPECTED_OUTPUT_POSTGAIN = 15.00f
    private const val EXPECTED_STEREOWIDE_MODE = 2.0f

    @Volatile private var appContext: Context? = null
    @Volatile private var restoring = false
    @Volatile private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        appContext = app
        register(app, OUTPUT)
        register(app, GRAPHIC_EQ)
        register(app, STEREO_WIDE)
        register(app, AUDIO_FORMAT)
        register(app, APP)
        initialized = true
        Log.i(TAG, "DSP settings guard initialized")
        restoreIfNeeded(app, "init", force = true)
    }

    fun restoreIfNeeded(context: Context, reason: String, force: Boolean = false) {
        val app = context.applicationContext
        val preset = activePreset(app) ?: seedDefaultPreset(app, reason)

        synchronized(this) {
            if (restoring) return
            restoring = true
            try {
                var changed = false
                changed = guardOutput(app, force) || changed
                changed = guardGraphicEq(app, preset, force) || changed
                changed = guardStereoWide(app, force) || changed
                changed = guardAudioFormat(app, force) || changed
                changed = guardPoweredOn(app, force) || changed

                if (changed) {
                    Log.w(TAG, "Restored locked DSP profile '${preset.title}' after $reason")
                    logCurrentValues(app, preset, "after restore: $reason")
                    notifyPreferencesUpdated(app)
                } else {
                    Log.i(TAG, "DSP profile '${preset.title}' already locked ($reason)")
                    logCurrentValues(app, preset, "already locked: $reason")
                }
            } finally {
                restoring = false
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (restoring || key == null) return
        if (key !in watchedKeys) return
        val ctx = appContext ?: return
        Log.w(TAG, "Observed DSP preference change: $key")
        restoreIfNeeded(ctx, "preference change '$key'", force = false)
    }

    private fun register(context: Context, name: String) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this)
    }


    private fun seedDefaultPreset(context: Context, reason: String): RetroidPreset {
        val preset = RetroidPresets.all.firstOrNull { it.id == "flip2_quiet" }
            ?: RetroidPresets.all.first { it.title == "Flip 2 Quiet" }
        context.getSharedPreferences(RetroidPreferenceApplier.RETROID_PREFS, Context.MODE_PRIVATE).edit()
            .putString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_ID, preset.id)
            .putString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_TITLE, preset.title)
            .commit()
        context.getSharedPreferences(Constants.PREF_VAR, Context.MODE_PRIVATE).edit()
            .putString("retroid_last_preset", preset.title)
            .commit()
        Log.w(TAG, "Seeded default Retroid preset '${preset.title}' ($reason)")
        return preset
    }

    private fun activePreset(context: Context): RetroidPreset? {
        val retroidPrefs = context.getSharedPreferences(RetroidPreferenceApplier.RETROID_PREFS, Context.MODE_PRIVATE)
        val id = retroidPrefs.getString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_ID, null)
        val byId = RetroidPresets.all.firstOrNull { it.id == id }
        if (byId != null) return byId

        // v32 recovery path: older/test builds sometimes had only the title in the var namespace,
        // or the app process started before the Retroid prefs were written. Recover and persist it.
        val title = context.getSharedPreferences(Constants.PREF_VAR, Context.MODE_PRIVATE)
            .getString("retroid_last_preset", null)
        val byTitle = RetroidPresets.all.firstOrNull { it.title == title }
        if (byTitle != null) {
            retroidPrefs.edit()
                .putString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_ID, byTitle.id)
                .putString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_TITLE, byTitle.title)
                .commit()
            Log.w(TAG, "Recovered active Retroid preset from var namespace: ${byTitle.title}")
            return byTitle
        }

        return null
    }

    private fun guardOutput(context: Context, force: Boolean): Boolean {
        val prefs = context.getSharedPreferences(OUTPUT, Context.MODE_PRIVATE)
        val needsWrite = force ||
            !floatClose(prefs.getFloat(KEY_LIMITER_THRESHOLD, Float.NaN), EXPECTED_LIMITER_THRESHOLD) ||
            !floatClose(prefs.getFloat(KEY_LIMITER_RELEASE, Float.NaN), EXPECTED_LIMITER_RELEASE) ||
            !floatClose(prefs.getFloat(KEY_OUTPUT_POSTGAIN, Float.NaN), EXPECTED_OUTPUT_POSTGAIN)
        if (!needsWrite) return false
        prefs.edit()
            .putFloat(KEY_LIMITER_THRESHOLD, EXPECTED_LIMITER_THRESHOLD)
            .putFloat(KEY_LIMITER_RELEASE, EXPECTED_LIMITER_RELEASE)
            .putFloat(KEY_OUTPUT_POSTGAIN, EXPECTED_OUTPUT_POSTGAIN)
            .commit()
        return true
    }

    private fun guardGraphicEq(context: Context, preset: RetroidPreset, force: Boolean): Boolean {
        val prefs = context.getSharedPreferences(GRAPHIC_EQ, Context.MODE_PRIVATE)
        val needsWrite = force ||
            !prefs.getBoolean(KEY_GEQ_ENABLE, false) ||
            prefs.getString(KEY_GEQ_NODES, null) != preset.graphicEq
        if (!needsWrite) return false
        prefs.edit()
            .putBoolean(KEY_GEQ_ENABLE, true)
            .putString(KEY_GEQ_NODES, preset.graphicEq)
            .commit()
        return true
    }

    private fun guardStereoWide(context: Context, force: Boolean): Boolean {
        val prefs = context.getSharedPreferences(STEREO_WIDE, Context.MODE_PRIVATE)
        val needsWrite = force ||
            !prefs.getBoolean(KEY_STEREOWIDE_ENABLE, false) ||
            !floatClose(prefs.getFloat(KEY_STEREOWIDE_MODE, Float.NaN), EXPECTED_STEREOWIDE_MODE)
        if (!needsWrite) return false
        prefs.edit()
            .putBoolean(KEY_STEREOWIDE_ENABLE, true)
            .putFloat(KEY_STEREOWIDE_MODE, EXPECTED_STEREOWIDE_MODE)
            .commit()
        return true
    }

    private fun guardAudioFormat(context: Context, force: Boolean): Boolean {
        val prefs = context.getSharedPreferences(AUDIO_FORMAT, Context.MODE_PRIVATE)
        val needsWrite = force || !prefs.getBoolean(KEY_AUDIOFORMAT_PROCESSING, false)
        if (!needsWrite) return false
        prefs.edit().putBoolean(KEY_AUDIOFORMAT_PROCESSING, true).commit()
        return true
    }

    private fun guardPoweredOn(context: Context, force: Boolean): Boolean {
        val prefs = context.getSharedPreferences(APP, Context.MODE_PRIVATE)
        val needsWrite = force || !prefs.getBoolean(KEY_POWERED_ON, true)
        if (!needsWrite) return false
        prefs.edit().putBoolean(KEY_POWERED_ON, true).commit()
        return true
    }

    private fun notifyPreferencesUpdated(context: Context) {
        RetroidPreferenceApplier.notifyJamesDsp(context)
    }

    private fun logCurrentValues(context: Context, preset: RetroidPreset, reason: String) {
        val out = context.getSharedPreferences(OUTPUT, Context.MODE_PRIVATE)
        val geq = context.getSharedPreferences(GRAPHIC_EQ, Context.MODE_PRIVATE)
        val stereo = context.getSharedPreferences(STEREO_WIDE, Context.MODE_PRIVATE)
        val fmt = context.getSharedPreferences(AUDIO_FORMAT, Context.MODE_PRIVATE)
        val app = context.getSharedPreferences(APP, Context.MODE_PRIVATE)
        Log.i(TAG, "DSP snapshot ($reason): preset=${preset.title}, postgain=${out.getFloat(KEY_OUTPUT_POSTGAIN, Float.NaN)}, limThr=${out.getFloat(KEY_LIMITER_THRESHOLD, Float.NaN)}, limRel=${out.getFloat(KEY_LIMITER_RELEASE, Float.NaN)}, geq=${geq.getBoolean(KEY_GEQ_ENABLE, false)}, geqMatches=${geq.getString(KEY_GEQ_NODES, null) == preset.graphicEq}, stereo=${stereo.getBoolean(KEY_STEREOWIDE_ENABLE, false)}, stereoMode=${stereo.getFloat(KEY_STEREOWIDE_MODE, Float.NaN)}, processing=${fmt.getBoolean(KEY_AUDIOFORMAT_PROCESSING, false)}, powered=${app.getBoolean(KEY_POWERED_ON, false)}")
    }

    private fun floatClose(a: Float, b: Float): Boolean = !a.isNaN() && abs(a - b) < 0.0001f

    private val watchedKeys = setOf(
        KEY_LIMITER_THRESHOLD,
        KEY_LIMITER_RELEASE,
        KEY_OUTPUT_POSTGAIN,
        KEY_GEQ_ENABLE,
        KEY_GEQ_NODES,
        KEY_STEREOWIDE_ENABLE,
        KEY_STEREOWIDE_MODE,
        KEY_AUDIOFORMAT_PROCESSING,
        KEY_POWERED_ON
    )
}
