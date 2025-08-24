package com.example.lingbao

import android.content.Context
import android.content.SharedPreferences

object EventLogger {
    private const val PREF = "lingbao_events"
    fun logEvent(context: Context, type: String, detail: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val old = prefs.getString("events", "") ?: ""
        val entry = "${System.currentTimeMillis()}|$type|$detail;"
        prefs.edit().putString("events", old + entry).apply()
    }

    fun readEvents(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString("events", "") ?: ""
    }
}
