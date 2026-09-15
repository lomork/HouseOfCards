package com.lomork.house_of_cards.data

/**
 * iOS auth currently uses the local fallback so the shared UI keeps working end
 * to end. To enable real email/password sign-in on Apple devices, add the
 * Firebase iOS SDK via CocoaPods (`FirebaseAuth`) plus a `GoogleService-Info.plist`,
 * then mirror the Android `actual` here using the Firebase Native bindings.
 */
actual object AuthService {
    actual val isConfigured: Boolean get() = false

    actual suspend fun signUp(email: String, password: String): AuthOutcome =
        localValidate(email, password)

    actual suspend fun signIn(email: String, password: String): AuthOutcome =
        localValidate(email, password)

    actual fun signOut() { }

    private fun localValidate(email: String, password: String): AuthOutcome = when {
        email.isBlank() || !email.contains("@") -> AuthOutcome.Failure("Enter a valid email address.")
        password.length < 6 -> AuthOutcome.Failure("Password must be at least 6 characters.")
        else -> AuthOutcome.Success("local_${email.lowercase()}")
    }
}