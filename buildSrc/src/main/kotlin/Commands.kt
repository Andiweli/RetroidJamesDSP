import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

// Git metadata is used when available. Exported source archives may not contain
// a .git directory, so release builds must remain possible with safe fallbacks.
fun Project.getCommitCount(): String {
    return runCommandOrFallback("git rev-list --count HEAD", "1")
}

fun Project.getGitSha(): String {
    return runCommandOrFallback("git rev-parse --short HEAD", "unknown")
}

fun Project.getBuildTime(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun Project.runCommandOrFallback(command: String, fallback: String): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        val byteErr = ByteArrayOutputStream()
        val result = project.exec {
            commandLine = command.split(" ")
            standardOutput = byteOut
            errorOutput = byteErr
            isIgnoreExitValue = true
        }

        if (result.exitValue == 0) {
            String(byteOut.toByteArray()).trim().ifBlank { fallback }
        } else {
            logger.warn("Command failed; using fallback '$fallback': $command")
            fallback
        }
    } catch (ex: Exception) {
        logger.warn("Command unavailable; using fallback '$fallback': $command", ex)
        fallback
    }
}
