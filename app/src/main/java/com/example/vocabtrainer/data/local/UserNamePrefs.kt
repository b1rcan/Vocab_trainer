package com.example.vocabtrainer.data.local

import android.content.Context

/**
 * Stores and retrieves the user's display name in SharedPreferences.
 *
 * - [saveName] → persists the given name locally.
 * - [getName]  → returns the stored name, or null if none has been saved yet.
 */
class UserNamePrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_name_prefs"
        private const val KEY_NAME   = "display_name"
    }

    /** Saves [name] to SharedPreferences. */
    fun saveName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    /**
     * Returns the previously saved display name, or **null** if none exists.
     * An empty/blank string is treated the same as absent.
     */
    fun getName(): String? =
        prefs.getString(KEY_NAME, null)?.takeUnless { it.isBlank() }
}
