package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.Context
import java.io.File

object RetroidAssets {
    // Versioned payload directory: the Retroid root script may chown bind-mounted
    // config files to root. Reusing an old directory can make later app-side
    // overwrites fail with EACCES, so each integration revision gets a new root. v36 intentionally uses a fresh folder so the delayed-reopen/no-detached-ping test is copied.
    private const val ASSET_ROOT = "retroid"
    private const val FILES_ROOT = "retroid_v36"

    fun copyToFiles(context: Context): File {
        val target = File(context.filesDir, FILES_ROOT)
        copyAssetTree(context, ASSET_ROOT, target)
        return target
    }

    private fun copyAssetTree(context: Context, assetPath: String, output: File) {
        val list = context.assets.list(assetPath) ?: return
        if (list.isEmpty()) {
            // If a previous root-side bind/chown made this file read-only for the app,
            // do not try to overwrite it. Existing payload files are immutable enough
            // for this private integration build.
            if (output.exists() && output.length() > 0L) return
            output.parentFile?.mkdirs()
            try {
                context.assets.open(assetPath).use { input ->
                    output.outputStream().use { out -> input.copyTo(out) }
                }
            } catch (security: SecurityException) {
                if (!output.exists()) throw security
            } catch (access: java.io.FileNotFoundException) {
                if (!output.exists()) throw access
            }
            return
        }

        output.mkdirs()
        list.forEach { child ->
            copyAssetTree(context, "$assetPath/$child", File(output, child))
        }
    }
}
