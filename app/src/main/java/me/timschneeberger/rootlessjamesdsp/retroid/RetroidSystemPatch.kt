package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.Context
import java.io.File
import me.timschneeberger.rootlessjamesdsp.BuildConfig

object RetroidSystemPatch {
    fun enable(context: Context): Result<String?> {
        val root = RetroidAssets.copyToFiles(context)
        val script = File(root, "support/subscripts/jdsp.setup.sh")
        val log = File(context.getExternalFilesDir(null), "retroid-jdsp-lastlog.txt")
        val cmd = "APPLICATION_ID='${BuildConfig.APPLICATION_ID}' sh ${script.absolutePath} ${root.absolutePath} > ${log.absolutePath} 2>&1"
        return RetroidRootExec().execute(cmd)
    }

    fun disable(context: Context): Result<String?> {
        val root = RetroidAssets.copyToFiles(context)
        val script = File(root, "support/subscripts/jdsp.cleanup.sh")
        val log = File(context.getExternalFilesDir(null), "retroid-jdsp-lastlog.txt")
        val cmd = "APPLICATION_ID='${BuildConfig.APPLICATION_ID}' sh ${script.absolutePath} ${root.absolutePath} > ${log.absolutePath} 2>&1"
        return RetroidRootExec().execute(cmd)
    }
}
