package com.lomork.house_of_cards.data

/**
 * Platform auth backend. On Android/iOS this is Firebase Auth (email/password).
 * Each `actual` also carries a graceful local fallback so the app remains fully
 * usable before the real `google-services.json` / `GoogleService-Info.plist`
 * has been dropped in — in that state [isConfigured] reports false and sign-up /
 * sign-in validate locally instead of failing on the network.
 */
sealed interface AuthOutcome {
    data class Success(val uid: String) : AuthOutcome
    data class Failure(val message: String) : AuthOutcome
}

expect object AuthService {
    /** True when a real Firebase project is configured (not the placeholder). */
    val isConfigured: Boolean

    suspend fun signUp(email: String, password: String): AuthOutcome
    suspend fun signIn(email: String, password: String): AuthOutcome
    fun signOut()
}