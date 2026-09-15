package com.lomork.house_of_cards.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Project id baked into the placeholder `google-services.json`; used to detect "not yet configured". */
private const val PLACEHOLDER_PROJECT_ID = "house-of-cards-placeholder"

actual object AuthService {
    private val configured: Boolean by lazy {
        try {
            val pid = FirebaseApp.getInstance().options.projectId.orEmpty()
            pid.isNotBlank() && pid != PLACEHOLDER_PROJECT_ID
        } catch (_: Throwable) {
            false
        }
    }

    actual val isConfigured: Boolean get() = configured

    actual suspend fun signUp(email: String, password: String): AuthOutcome {
        val e = email.trim()
        if (!configured) return localValidate(e, password)
        if (e.isEmpty() || password.length < 6) {
            return AuthOutcome.Failure("Enter a valid email and a password of at least 6 characters.")
        }
        return try {
            val res = FirebaseAuth.getInstance().createUserWithEmailAndPassword(e, password).await()
            AuthOutcome.Success(res.user?.uid ?: "anon")
        } catch (ex: Exception) {
            AuthOutcome.Failure(ex.message ?: "Sign-up failed")
        }
    }

    actual suspend fun signIn(email: String, password: String): AuthOutcome {
        val e = email.trim()
        if (!configured) return localValidate(e, password)
        if (e.isEmpty() || password.isEmpty()) {
            return AuthOutcome.Failure("Enter your email and password.")
        }
        return try {
            val res = FirebaseAuth.getInstance().signInWithEmailAndPassword(e, password).await()
            AuthOutcome.Success(res.user?.uid ?: "anon")
        } catch (ex: Exception) {
            AuthOutcome.Failure(ex.message ?: "Sign-in failed")
        }
    }

    actual fun signOut() {
        if (configured) {
            try { FirebaseAuth.getInstance().signOut() } catch (_: Throwable) { }
        }
    }

    private fun localValidate(email: String, password: String): AuthOutcome = when {
        email.isEmpty() || !email.contains("@") -> AuthOutcome.Failure("Enter a valid email address.")
        password.length < 6 -> AuthOutcome.Failure("Password must be at least 6 characters.")
        else -> AuthOutcome.Success("local_${email.lowercase()}")
    }

    private suspend fun Task<AuthResult>.await(): AuthResult = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null) {
                    cont.resume(result)
                } else {
                    cont.resumeWithException(IllegalStateException("Authentication failed"))
                }
            } else {
                cont.resumeWithException(task.exception ?: IllegalStateException("Authentication failed"))
            }
        }
    }
}