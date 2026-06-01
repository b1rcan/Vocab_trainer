package com.example.vocabtrainer.data.local

import android.content.Context

class UserNamePrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    fun getName(): String? = prefs.getString(KEY_NAME, null)?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        private const val PREFS_NAME = "user_name_prefs"
        private const val KEY_NAME = "display_name"
    }
}