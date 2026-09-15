package com.lomork.house_of_cards.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

val HocJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private const val SAVE_KEY = "saved_state_v1"

/**
 * Single source of truth for the app. Holds the whole [SavedState] in Compose
 * snapshot state and persists it as JSON on every mutation. When Firebase is
 * configured, account fields (coins, stats, inventory) are mirrored up to
 * Firestore so editing this local file can't grant free coins.
 */
class AppStore {
    var saved: SavedState by mutableStateOf(load())
        private set

    val profile: PlayerProfile get() = saved.player

    /** Fire-and-forget scope so cloud writes never block the UI. */
    private val cloudScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun load(): SavedState {
        val raw = storageRead(SAVE_KEY) ?: return SeedData.freshState()
        return try {
            HocJson.decodeFromString<SavedState>(raw)
        } catch (_: Exception) {
            SeedData.freshState()
        }
    }

    fun update(transform: (SavedState) -> SavedState) {
        saved = transform(saved)
        persist()
    }

    private fun persist() {
        try {
            storageWrite(SAVE_KEY, HocJson.encodeToString(saved))
        } catch (_: Exception) {
            // Non-fatal: keep going with in-memory state.
        }
    }

    // ---- Cloud sync ------------------------------------------------------

    private fun cloudUid(): String? =
        if (CloudProfile.isConfigured && saved.isLoggedIn) saved.authUid else null

    private fun push(block: suspend (uid: String) -> Unit) {
        val uid = cloudUid() ?: return
        cloudScope.launch { try { block(uid) } catch (_: Exception) { } }
    }

    private fun pushProfile() {
        val uid = cloudUid() ?: return
        val data = saved.player.toCloudData()
        cloudScope.launch { try { CloudProfile.update(uid, data) } catch (_: Exception) { } }
    }

    /** Create the server-side account doc (grants [STARTING_COINS]). Returns an error message, or null on success. */
    suspend fun provisionCloud(uid: String, username: String): String? {
        if (!CloudProfile.isConfigured) return null
        return when (val r = CloudProfile.create(uid, username.ifBlank { profile.username })) {
            is CloudOutcome.Success -> null
            is CloudOutcome.Failure -> r.message
        }
    }

    /** Pull the server profile into local state after sign-in. Returns an error message, or null on success. */
    suspend fun hydrateFromCloud(uid: String): String? {
        if (!CloudProfile.isConfigured) return null
        return when (val r = CloudProfile.load(uid)) {
            is CloudOutcome.Success -> {
                if (r.data != null) {
                    update { s -> s.copy(player = r.data.toProfile()) }
                    null
                } else {
                    provisionCloud(uid, profile.username) // missing doc — re-seed with starting coins
                }
            }
            is CloudOutcome.Failure -> r.message
        }
    }

    /** Re-sync from the server at startup so a tampered local cache can't persist. */
    suspend fun refreshFromCloud() {
        val uid = saved.authUid ?: return
        if (!CloudProfile.isConfigured || !saved.isLoggedIn) return
        when (val r = CloudProfile.load(uid)) {
            is CloudOutcome.Success -> {
                if (r.data != null) {
                    update { s -> s.copy(player = r.data.toProfile()) }
                }
            }
            is CloudOutcome.Failure -> Unit
        }
    }

    // ---- Profile helpers -------------------------------------------------

    /** Win a game: +1 win, +1 streak, and [WIN_REWARD_COINS] coins (kept in sync with the server). */
    fun recordWin() {
        update { s ->
            val p = s.player
            s.copy(
                player = p.copy(
                    totalWins = p.totalWins + 1,
                    streak = p.streak + 1,
                    coins = p.coins + WIN_REWARD_COINS,
                ),
            )
        }
        push { CloudProfile.recordResult(it, won = true) }
    }

    fun recordLoss() {
        update { s ->
            val p = s.player
            s.copy(player = p.copy(totalLosses = p.totalLosses + 1, streak = 0))
        }
        push { CloudProfile.recordResult(it, won = false) }
    }

    fun recordDraw() {
        update { s -> s.copy(player = s.player.copy(streak = 0)) }
        pushProfile()
    }

    /** Recompute unlocked achievements and return the ones newly earned this game. */
    fun applyAchievements(): List<AchievementDef> {
        val newly = updateForAchievements()
        if (newly.isNotEmpty()) pushProfile()
        return newly
    }

    private fun updateForAchievements(): List<AchievementDef> {
        var newly: List<AchievementDef> = emptyList()
        update { s ->
            val p = s.player
            val ids = p.unlockedAchievementIds.toMutableSet()
            val earned = mutableListOf<AchievementDef>()
            fun grant(id: String) {
                if (ids.add(id)) SeedData.achievements.firstOrNull { it.id == id }?.let(earned::add)
            }
            if (p.totalWins >= 1) grant("firstWin")
            if (p.streak >= 3) grant("streak3")
            if (p.streak >= 5) grant("streak5")
            if (p.totalWins >= 10) grant("wins10")
            if (p.ownedItemIds.count { it.startsWith("chip_") } >= 3) grant("chip3")
            if (s.friends.size >= 5) grant("social5")
            newly = earned.toList()
            s.copy(player = p.copy(unlockedAchievementIds = ids.toList()))
        }
        return newly
    }

    // ---- Friends helpers (local/social for now) --------------------------

    fun addFriend(entry: FriendEntry) = update { s ->
        if (s.friends.any { it.userId == entry.userId }) s
        else s.copy(friends = s.friends + entry)
    }

    fun removeFriend(userId: String) = update { s ->
        s.copy(friends = s.friends.filterNot { it.userId == userId })
    }

    fun friendById(userId: String): FriendEntry? = saved.friends.firstOrNull { it.userId == userId }

    fun friendByUsername(name: String): FriendEntry? =
        saved.friends.firstOrNull { it.username.equals(name, ignoreCase = true) }

    fun acceptRequest(userId: String) = update { s ->
        val req = s.requests.firstOrNull { it.userId == userId && it.incoming }
        if (req == null) s
        else {
            val entry = FriendEntry(
                userId = req.userId,
                username = req.username,
                level = req.level,
                chipId = null,
                isOnline = true,
            )
            s.copy(
                requests = s.requests.filterNot { it.userId == userId },
                friends = s.friends + entry,
            )
        }
    }

    fun declineRequest(userId: String) = update { s ->
        s.copy(requests = s.requests.filterNot { it.userId == userId })
    }

    fun addMatch(record: MatchRecord) = update { s ->
        s.copy(matchHistory = (listOf(record) + s.matchHistory).take(200))
    }

    // ---- Store helpers ---------------------------------------------------

    fun owns(itemId: String): Boolean = saved.player.ownedItemIds.contains(itemId)

    fun spendCoins(amount: Int): Boolean {
        if (saved.player.coins < amount) return false
        update { s -> s.copy(player = s.player.copy(coins = s.player.coins - amount)) }
        pushProfile()
        return true
    }

    fun unlockItem(itemId: String) {
        update { s ->
            val p = s.player
            if (itemId in p.ownedItemIds) s
            else s.copy(player = p.copy(ownedItemIds = p.ownedItemIds + itemId))
        }
        pushProfile()
    }

    fun equipChip(chipId: String?) {
        update { s -> s.copy(player = s.player.copy(equippedChipId = chipId)) }
        pushProfile()
    }

    // ---- Settings / account ---------------------------------------------

    fun setSetting(audio: Boolean? = null, vibration: Boolean? = null, chat: Boolean? = null, darkMode: Boolean? = null) = update { s ->
        val st = s.settings
        s.copy(
            settings = AppSettings(
                audioEnabled = audio ?: st.audioEnabled,
                vibrationsEnabled = vibration ?: st.vibrationsEnabled,
                chatEnabled = chat ?: st.chatEnabled,
                darkMode = darkMode ?: st.darkMode,
            ),
        )
    }

    /** Forfeit an ongoing match: deduct [QUIT_PENALTY_COINS] and sync to the server. */
    fun forfeitPenalty() {
        update { s ->
            val p = s.player
            s.copy(player = p.copy(coins = (p.coins - QUIT_PENALTY_COINS).coerceAtLeast(0)))
        }
        pushProfile()
    }

    fun setStatus(value: String) {
        update { s -> s.copy(player = s.player.copy(status = value)) }
        pushProfile()
    }

    fun setRegion(value: String) {
        update { s -> s.copy(player = s.player.copy(region = value)) }
        pushProfile()
    }

    /** Editable display name. */
    fun setUsername(name: String) {
        update { s ->
            val trimmed = name.trim()
            if (trimmed.isEmpty()) s else s.copy(player = s.player.copy(username = trimmed))
        }
        pushProfile()
    }

    /** Mark the current session as authenticated (local or Firebase). */
    fun markLoggedIn(email: String, uid: String? = null) = update { s ->
        s.copy(isLoggedIn = true, authEmail = email.trim().ifBlank { null }, authUid = uid)
    }

    /** Sign out: return to the login screen but keep local progress. */
    fun signOut() = update { s -> s.copy(isLoggedIn = false, authUid = null) }

    /** Delete account: reset everything to a fresh identity (also logs out). */
    fun resetAccount() = update { 
        val newState = SeedData.freshState()
        newState.copy(friends = emptyList()) 
    }
}