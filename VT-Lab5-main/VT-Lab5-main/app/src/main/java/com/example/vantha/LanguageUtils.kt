package com.example.vantha

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

fun Context.getLanguageCode(): String {
    val prefs: SharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(MainActivity.PREF_LANG_CODE, MainActivity.DEFAULT_LANG_CODE)
        ?: MainActivity.DEFAULT_LANG_CODE
}

fun Context.saveLanguageCode(languageCode: String) {
    val prefs: SharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(MainActivity.PREF_LANG_CODE, languageCode).apply() // ← FIXED
}

fun Context.setLocale(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}