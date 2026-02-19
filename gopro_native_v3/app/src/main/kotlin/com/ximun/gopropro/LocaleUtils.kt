package com.ximun.gopropro

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Gestion de la locale de l'application.
 * - Stockage dans SharedPreferences
 * - Application via createConfigurationContext (API 26+)
 * - Déclaration native Android 13+ via localeConfig dans le Manifest
 */
object LocaleUtils {

    private const val PREFS_NAME = "app_prefs"
    private const val PREF_LOCALE = "app_locale"

    /** Retourne le tag de la locale stockée, ou "" pour "automatique (système)". */
    fun getCurrentLocale(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(PREF_LOCALE, "") ?: ""
    }

    /**
     * Enregistre la locale et la notifie au système Android 13+.
     * L'appelant doit appeler Activity.recreate() après pour appliquer le changement.
     *
     * @param languageTag BCP 47 tag ("fr", "en", "eu"…) ou "" pour suivre le système.
     */
    fun setLocale(context: Context, languageTag: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_LOCALE, languageTag)
            .apply()

        // Android 13+ : notifier LocaleManager pour le réglage per-app natif
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            localeManager?.applicationLocales = if (languageTag.isEmpty()) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(languageTag)
            }
        }
    }

    /**
     * À appeler dans MainActivity.attachBaseContext().
     * Applique la locale stockée au contexte de base de l'Activity.
     */
    fun applyLocale(context: Context): Context {
        val tag = getCurrentLocale(context).takeIf { it.isNotEmpty() } ?: return context
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
