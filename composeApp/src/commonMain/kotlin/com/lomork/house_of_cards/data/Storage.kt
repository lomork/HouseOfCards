package com.lomork.house_of_cards.data

/**
 * Platform key-value persistence. Android backs this with SharedPreferences,
 * iOS with NSUserDefaults — see the matching `Storage.kt` in androidMain/iosMain.
 */
expect fun storageRead(key: String): String?
expect fun storageWrite(key: String, value: String)

/** Wall-clock time in milliseconds, for ids and timestamps. */
expect fun nowMillis(): Long