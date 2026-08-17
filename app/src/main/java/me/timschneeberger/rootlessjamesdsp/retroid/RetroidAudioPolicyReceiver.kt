package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import me.timschneeberger.rootlessjamesdsp.service.RootAudioProcessorService

/**
 * Retroid private build: called by the temp-root setup script after audioserver/media restart.
 *
 * v35 proved that the Quiet preset seed is correct, but it also touched JamesDSP preferences
 * while AudioEffect session 0 was still detached. That produced the visible "Engine crashed"
 * warning before the delayed reopen could repair the session.
 *
 * v37 keeps that safe delayed reopen and then creates one silent MEDIA AudioTrack. The user test
 * showed that opening 80s80s after SectorStrike immediately fixed the dull highs. This warmup is
 * the internal, inaudible equivalent of that media-session wake-up.
 */
class RetroidAudioPolicyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        val app = context.applicationContext
        Thread {
            try {
                Log.w("RetroidAudioPolicy", "Audio-policy restart receiver invoked: action=${intent?.action}")
                RetroidDspSettingsGuard.init(app)

                // Do not force preferences immediately here. Right after AudioPolicyService/AudioFlinger
                // restart the old AudioEffect can still be detached; forcing a pref sync in that window
                // caused the release UI to show "Engine crashed" in v35.
                Thread.sleep(2300L)
                requestLegacyReopen(app, "audio policy receiver delayed single legacy reopen 2300ms")

                // Let session 0 attach, then mimic the external 80s80s wake-up with a silent MEDIA track.
                Thread.sleep(450L)
                RetroidAudioWarmup.playMediaWarmup("audio policy receiver delayed legacy reopen")

                // Give the newly opened session and media route a moment to settle, then only verify.
                Thread.sleep(900L)
                RetroidDspSettingsGuard.restoreIfNeeded(app, "audio policy receiver verify after media warmup", force = false)
            } catch (t: Throwable) {
                Log.e("RetroidAudioPolicy", "Audio-policy receiver failed", t)
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun requestLegacyReopen(context: Context, reason: String) {
        try {
            Log.w("RetroidAudioPolicy", "Requesting delayed safe root legacy session reopen after $reason")
            RootAudioProcessorService.updateLegacyMode(context, true)
        } catch (t: Throwable) {
            Log.e("RetroidAudioPolicy", "Delayed safe root legacy session reopen failed after $reason", t)
        }
    }
}
