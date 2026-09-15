package com.lomork.house_of_cards.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Project id baked into the placeholder `google-services.json`; used to detect "not yet configured". */
private const val PLACEHOLDER_PROJECT_ID = "house-of-cards-placeholder"

actual object CloudProfile {
    private val configured: Boolean by lazy {
        try {
            val pid = FirebaseApp.getInstance().options.projectId.orEmpty()
            pid.isNotBlank() && pid != PLACEHOLDER_PROJECT_ID
        } catch (_: Throwable) {
            false
        }
    }

    actual val isConfigured: Boolean get() = configured

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private fun doc(uid: String) = db.collection("users").document(uid)

    actual suspend fun create(uid: String, username: String): CloudOutcome {
        if (!configured) return CloudOutcome.Success()
        return try {
            doc(uid).set(
                CloudUserData(username = username.ifBlank { "Jack" }, coins = STARTING_COINS).toMap(),
            ).awaitVoid()
            CloudOutcome.Success()
        } catch (ex: Exception) {
            CloudOutcome.Failure(ex.message ?: "Could not create account data")
        }
    }

    actual suspend fun load(uid: String): CloudOutcome {
        if (!configured) return CloudOutcome.Success(null)
        return try {
            val snap = doc(uid).get().await()
            if (snap.exists()) CloudOutcome.Success(snap.toCloudUserData())
            else CloudOutcome.Success(null)
        } catch (ex: Exception) {
            CloudOutcome.Failure(ex.message ?: "Could not load account data")
        }
    }

    actual suspend fun update(uid: String, data: CloudUserData): CloudOutcome {
        if (!configured) return CloudOutcome.Success()
        return try {
            doc(uid).set(data.toMap(), SetOptions.merge()).awaitVoid()
            CloudOutcome.Success()
        } catch (ex: Exception) {
            CloudOutcome.Failure(ex.message ?: "Could not save account data")
        }
    }

    actual suspend fun recordResult(uid: String, won: Boolean): CloudOutcome {
        if (!configured) return CloudOutcome.Success()
        return try {
            val updates: Map<String, Any> = if (won) {
                mapOf(
                    "coins" to FieldValue.increment(WIN_REWARD_COINS.toLong()),
                    "totalWins" to FieldValue.increment(1L),
                    "streak" to FieldValue.increment(1L),
                )
            } else {
                mapOf(
                    "totalLosses" to FieldValue.increment(1L),
                    "streak" to 0L,
                )
            }
            doc(uid).update(updates).awaitVoid()
            CloudOutcome.Success()
        } catch (ex: Exception) {
            CloudOutcome.Failure(ex.message ?: "Could not save match result")
        }
    }

    // ---- mapping ---------------------------------------------------------

    private fun CloudUserData.toMap(): Map<String, Any?> = mapOf(
        "username" to username,
        "totalWins" to totalWins,
        "totalLosses" to totalLosses,
        "streak" to streak,
        "coins" to coins,
        "status" to status,
        "region" to region,
        "equippedChipId" to equippedChipId,
        "ownedItemIds" to ownedItemIds,
        "unlockedAchievementIds" to unlockedAchievementIds,
    )

    private fun DocumentSnapshot.toCloudUserData(): CloudUserData {
        val m = data ?: emptyMap()
        fun int(key: String, d: Int): Int = (m[key] as? Number)?.toInt() ?: d
        fun str(key: String, d: String): String = m[key] as? String ?: d
        fun strList(key: String): List<String> = (m[key] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        return CloudUserData(
            username = str("username", "Jack"),
            totalWins = int("totalWins", 0),
            totalLosses = int("totalLosses", 0),
            streak = int("streak", 0),
            coins = int("coins", STARTING_COINS),
            status = str("status", "Online"),
            region = str("region", "Global"),
            equippedChipId = m["equippedChipId"] as? String,
            ownedItemIds = strList("ownedItemIds"),
            unlockedAchievementIds = strList("unlockedAchievementIds"),
        )
    }

    // ---- await helpers ---------------------------------------------------

    private suspend fun Task<Void>.awaitVoid() = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) cont.resume(Unit)
            else cont.resumeWithException(task.exception ?: IllegalStateException("Firestore write failed"))
        }
    }

    private suspend fun Task<DocumentSnapshot>.await(): DocumentSnapshot = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snap = task.result
                if (snap != null) cont.resume(snap)
                else cont.resumeWithException(IllegalStateException("Firestore read failed"))
            } else {
                cont.resumeWithException(task.exception ?: IllegalStateException("Firestore read failed"))
            }
        }
    }
}