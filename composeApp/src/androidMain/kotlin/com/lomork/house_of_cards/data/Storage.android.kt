package com.lomork.house_of_cards.data

import android.content.Context

private var appContext: Context? = null

/** Call once from the Application/Activity before the UI is created. */
fun initializeStorage(context: Context) {
    appContext = context.applicationContext
}

private fun prefs() =
    appContext?.getSharedPreferences("house_of_cards_store", Context.MODE_PRIVATE)

actual fun storageRead(key: String): String? = prefs()?.getString(key, null)

actual fun storageWrite(key: String, value: String) {
    prefs()?.edit()?.putString(key, value)?.apply()
}

actual fun nowMillis(): Long = System.currentTimeMillis()