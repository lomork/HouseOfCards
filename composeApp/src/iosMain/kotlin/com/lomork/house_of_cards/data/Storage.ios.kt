package com.lomork.house_of_cards.data

import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

actual fun storageRead(key: String): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(key)

actual fun storageWrite(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
}

actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()