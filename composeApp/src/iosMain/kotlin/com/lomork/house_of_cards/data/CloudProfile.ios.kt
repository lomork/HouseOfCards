package com.lomork.house_of_cards.data

/**
 * Cloud persistence is a no-op on iOS until Firebase is wired in via CocoaPods
 * (`FirebaseFirestore`) and a `GoogleService-Info.plist` is added. Until then the
 * app falls back to local-only storage, exactly like the Android placeholder —
 * mirror the Android `actual` here once the plist is in place.
 */
actual object CloudProfile {
    actual val isConfigured: Boolean get() = false

    actual suspend fun create(uid: String, username: String): CloudOutcome = CloudOutcome.Success()
    actual suspend fun load(uid: String): CloudOutcome = CloudOutcome.Success(null)
    actual suspend fun update(uid: String, data: CloudUserData): CloudOutcome = CloudOutcome.Success()
    actual suspend fun recordResult(uid: String, won: Boolean): CloudOutcome = CloudOutcome.Success()
}