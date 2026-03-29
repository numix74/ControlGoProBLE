package com.actioncam.airbuble.diagnostic

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implémentation debug du diagnostic :
 * - Double écriture logcat + buffer mémoire (2000 lignes max)
 * - Export en fichier .txt partageable via Share Sheet Android
 */
class DebugDiagnosticProvider : DiagnosticProvider {

    companion object {
        private const val LOGCAT_TAG = "AirbubleLog"
        private const val MAX_LINES = 2000
    }

    private val buffer = ArrayDeque<String>()
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun log(tag: String, message: String) {
        Log.i(LOGCAT_TAG, "[$tag] $message")
        append("I", tag, message)
    }

    override fun warn(tag: String, message: String) {
        Log.w(LOGCAT_TAG, "[$tag] $message")
        append("W", tag, message)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(LOGCAT_TAG, "[$tag] $message", throwable)
        val full = if (throwable != null)
            "$message — ${throwable.javaClass.simpleName}: ${throwable.message}"
        else message
        append("E", tag, full)
    }

    override fun getLineCount(): Int = synchronized(buffer) { buffer.size }

    override fun buildShareIntent(context: Context): Intent? {
        val lines = synchronized(buffer) { buffer.toList() }
        if (lines.isEmpty()) return null

        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "airbuble_diag_$ts.txt")
        file.writeText(buildHeader(context) + "\n\n" + lines.joinToString("\n"))

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Airbuble diagnostic — ${deviceLabel()}")
            putExtra(Intent.EXTRA_TEXT, "Logs de session Airbuble (${lines.size} lignes)\nAppareil : ${deviceLabel()}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun clear() = synchronized(buffer) { buffer.clear() }

    private fun append(level: String, tag: String, message: String) {
        val line = "${timeFmt.format(Date())} $level/$tag: $message"
        synchronized(buffer) {
            if (buffer.size >= MAX_LINES) buffer.removeFirst()
            buffer.addLast(line)
        }
    }

    private fun buildHeader(context: Context): String {
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }

        return """
            === AIRBUBLE DIAGNOSTIC LOG ===
            Date    : ${Date()}
            Appareil: ${Build.MANUFACTURER} ${Build.MODEL}
            Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            App     : $versionName
            Lignes  : ${synchronized(buffer) { buffer.size }}
            ================================
        """.trimIndent()
    }

    private fun deviceLabel() = "${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE}"
}
