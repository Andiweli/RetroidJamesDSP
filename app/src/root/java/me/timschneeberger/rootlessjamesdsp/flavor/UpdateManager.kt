package me.timschneeberger.rootlessjamesdsp.flavor

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.timschneeberger.rootlessjamesdsp.utils.Result

/**
 * RetroidJamesDSP does not use the upstream RootlessJamesDSP self-update service.
 *
 * Public releases are distributed independently. Keeping this implementation as a
 * no-op prevents the Retroid root build from contacting the upstream update server
 * or offering an APK intended for a different project/package lifecycle.
 */
class UpdateManager(@Suppress("UNUSED_PARAMETER") context: Context) {
    fun getUpdateVersionInfo(): Pair<String, Int>? = null

    suspend fun isUpdateAvailable(): Flow<Result<Boolean>> =
        flowOf(Result.Success(false))

    fun installUpdate(@Suppress("UNUSED_PARAMETER") context: Context) = Unit
}
