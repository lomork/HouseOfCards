package com.lomork.house_of_cards.data

/** Every new account starts with this many coins. */
const val STARTING_COINS = 500

/** Coins awarded for each game the player wins. */
const val WIN_REWARD_COINS = 100

/** Coins deducted when a player quits an ongoing match. */
const val QUIT_PENALTY_COINS = 50

/**
 * The account fields that live in the cloud under `users/{uid}`. This is the
 * server-side source of truth for anything a cheater might otherwise edit on
 * the device — coins, stats and inventory — while local storage is only a
 * cache (and the fallback used before Firebase is configured).
 */
data class CloudUserData(
    val username: String = "Jack",
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val streak: Int = 0,
    val coins: Int = STARTING_COINS,
    val status: String = "Online",
    val region: String = "Global",
    val equippedChipId: String? = null,
    val ownedItemIds: List<String> = emptyList(),
    val unlockedAchievementIds: List<String> = emptyList(),
) {
    val level: Int get() = 1 + (totalWins * 2 + totalLosses) / 5
}

fun CloudUserData.toProfile(): PlayerProfile = PlayerProfile(
    username = username,
    totalWins = totalWins,
    totalLosses = totalLosses,
    streak = streak,
    coins = coins,
    status = status,
    region = region,
    equippedChipId = equippedChipId,
    ownedItemIds = ownedItemIds,
    unlockedAchievementIds = unlockedAchievementIds,
)

fun PlayerProfile.toCloudData(): CloudUserData = CloudUserData(
    username = username,
    totalWins = totalWins,
    totalLosses = totalLosses,
    streak = streak,
    coins = coins,
    status = status,
    region = region,
    equippedChipId = equippedChipId,
    ownedItemIds = ownedItemIds,
    unlockedAchievementIds = unlockedAchievementIds,
)

sealed interface CloudOutcome {
    data class Success(val data: CloudUserData? = null) : CloudOutcome
    data class Failure(val message: String) : CloudOutcome
}

/**
 * Platform cloud-persistence backend. On Android this is Firestore; on iOS
 * (and on Android before a real `google-services.json` is present) it degrades
 * to a no-op so the app keeps running with local-only storage.
 */
expect object CloudProfile {
    /** True when a real Firebase project is configured (not the placeholder). */
    val isConfigured: Boolean

    /** Create the account doc; grants [STARTING_COINS]. */
    suspend fun create(uid: String, username: String): CloudOutcome

    /** Fetch the account doc; [CloudOutcome.Success.data] is null when the doc is missing. */
    suspend fun load(uid: String): CloudOutcome

    /** Merge-write the full snapshot. Economy deltas go through [recordResult] instead. */
    suspend fun update(uid: String, data: CloudUserData): CloudOutcome

    /** Atomically record a finished game: +100 coins / +1 win / streak+1 on win; +1 loss / streak 0 on loss. */
    suspend fun recordResult(uid: String, won: Boolean): CloudOutcome
}