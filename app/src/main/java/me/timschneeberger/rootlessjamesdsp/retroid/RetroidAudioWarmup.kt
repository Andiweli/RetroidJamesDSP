package me.timschneeberger.rootlessjamesdsp.retroid

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.max

/**
 * Retroid private build: create a tiny local MEDIA playback after the Retroid audio policy
 * has been rebound and session 0 reopened.
 *
 * v36 showed that the DSP preset and global session are correct, but SectorStrike can start
 * with dull highs until a real media app (80s80s) opens playback. This short silent track is
 * a local replacement for that manual 80s80s wake-up: it nudges AudioFlinger/AudioPolicy to
 * rebuild the media route/effect chain without audible output.
 */
object RetroidAudioWarmup {
    private const val TAG = "RetroidAudioWarmup"

    fun playMediaWarmup(reason: String) {
        try {
            val sampleRate = 48000
            val channelMask = AudioFormat.CHANNEL_OUT_STEREO
            val encoding = AudioFormat.ENCODING_PCM_16BIT
            val minBuffer = AudioTrack.getMinBufferSize(sampleRate, channelMask, encoding)
            val bufferSize = max(minBuffer, 8192)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(encoding)
                        .setChannelMask(channelMask)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // Keep the track silent. We need the MEDIA route/effect-chain creation, not audible sound.
            track.setVolume(0.0f)
            track.play()

            val block = ByteArray(bufferSize.coerceAtLeast(8192))
            val endAt = System.currentTimeMillis() + 650L
            while (System.currentTimeMillis() < endAt) {
                track.write(block, 0, block.size)
            }

            runCatching { track.pause() }
            runCatching { track.flush() }
            runCatching { track.release() }
            Log.w(TAG, "Silent MEDIA warmup completed after $reason")
        } catch (t: Throwable) {
            Log.e(TAG, "Silent MEDIA warmup failed after $reason", t)
        }
    }
}
