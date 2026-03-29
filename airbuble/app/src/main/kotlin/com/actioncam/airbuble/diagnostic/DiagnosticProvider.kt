package com.actioncam.airbuble.diagnostic

import android.content.Context
import android.content.Intent

/**
 * Interface pour le module diagnostic.
 * - En debug : implémentation réelle (buffer mémoire + export)
 * - En release : NoopDiagnosticProvider (ne fait rien)
 */
interface DiagnosticProvider {
    fun log(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
    fun getLineCount(): Int
    fun buildShareIntent(context: Context): Intent?
    fun clear()
}

/** Implémentation vide pour la release — aucun overhead. */
class NoopDiagnosticProvider : DiagnosticProvider {
    override fun log(tag: String, message: String) {}
    override fun warn(tag: String, message: String) {}
    override fun error(tag: String, message: String, throwable: Throwable?) {}
    override fun getLineCount(): Int = 0
    override fun buildShareIntent(context: Context): Intent? = null
    override fun clear() {}
}
