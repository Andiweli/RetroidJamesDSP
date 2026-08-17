package me.timschneeberger.rootlessjamesdsp.retroid

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.Parcel
import java.nio.charset.Charset

@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
class RetroidRootExec {
    private val binder: IBinder? = runCatching {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val getService = serviceManager.getDeclaredMethod("getService", String::class.java)
        getService.invoke(null, "PServerBinder") as IBinder
    }.getOrNull()

    val available: Boolean
        get() = binder != null

    fun execute(command: String): Result<String?> {
        val b = binder ?: return Result.failure(IllegalStateException("PServerBinder not available"))
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeStringArray(arrayOf(command, "1"))
            b.transact(0, data, reply, 0)
            val result = reply.createByteArray()
                ?.toString(Charset.defaultCharset())
                ?.trim()
                ?.takeUnless { it == "null" }
            Result.success(result)
        } catch (t: Throwable) {
            Result.failure(t)
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
