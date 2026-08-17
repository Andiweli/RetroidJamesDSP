package me.timschneeberger.rootlessjamesdsp.activity

import android.os.Bundle
import me.timschneeberger.rootlessjamesdsp.retroid.RetroidDspSettingsGuard
import me.timschneeberger.rootlessjamesdsp.service.RootAudioProcessorService
import me.timschneeberger.rootlessjamesdsp.utils.isRoot
import timber.log.Timber

/**
 * Retroid private root/effect-chain build.
 *
 * The upstream EngineLauncherActivity falls back to the rootless MediaProjection path when
 * normal root detection is false. On Retroid this can happen after AudioPolicyService restarts
 * or while the temporary pservice-root path is still settling, which then opens the unwanted
 * screen/audio capture pipeline and can disturb the JamesDSP effect chain.
 *
 * v32 blocks the rootless fallback completely. It never asks for MediaProjection and never starts
 * RootlessAudioProcessorService. If root is visible, it starts the normal root service; otherwise
 * it only reasserts the locked Retroid profile and exits.
 */
class EngineLauncherActivity : BaseActivity() {
    override val disableAppTheme: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetroidDspSettingsGuard.restoreIfNeeded(this, "EngineLauncherActivity start", force = false)

        if (isRoot()) {
            Timber.d("Retroid EngineLauncherActivity: root visible, starting RootAudioProcessorService")
            RootAudioProcessorService.startServiceEnhanced(this)
        } else {
            Timber.w("Retroid EngineLauncherActivity: rootless MediaProjection fallback blocked")
        }

        finish()
    }
}
