package com.lomork.house_of_cards.data

import kotlinx.serialization.Serializable

/** The local player's own account/profile. */
@Serializable
data class PlayerProfile(
    val userId: String = "me",
    val username: String = "Jack",
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val streak: Int = 0,
    val coins: Int = 500,
    val status: String = "Online",
    val region: String = "Global",
    val equippedChipId: String? = null,
    val ownedItemIds: List<String> = emptyList(),
    val unlockedAchievementIds: List<String> = emptyList(),
) {
    /** Level derives from lifetime results so it can never desync from stats. */
    val level: Int get() = 1 + (totalWins * 2 + totalLosses) / 5
    val gamesPlayed: Int get() = totalWins + totalLosses
    val winRate: Int get() = if (gamesPlayed == 0) 0 else (totalWins * 100) / gamesPlayed
}

/** Another player in the friends list. */
@Serializable
data class FriendEntry(
    val userId: String,
    val username: String,
    val level: Int = 1,
    val streak: Int = 0,
    val status: String = "Online",
    val region: String = "Global",
    val chipId: String? = null,
    val isOnline: Boolean = true,
)

/** A friend request. `incoming == true` means they sent it to you. */
@Serializable
data class FriendRequest(
    val userId: String,
    val username: String,
    val level: Int = 1,
    val incoming: Boolean,
    val timestamp: Long = 0L,
)

/** An invitation to play a game, shown in the friends tab. */
@Serializable
data class GameInvite(
    val id: String,
    val fromUserId: String,
    val fromUsername: String,
    val gameId: String,
    val timestamp: Long = 0L,
)

@Serializable
data class AppSettings(
    val audioEnabled: Boolean = true,
    val vibrationsEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val darkMode: Boolean = true,
)

/** A serialized snapshot of a finished board, stored with each match record. */
@Serializable
data class BoardSnapshot(
    val size: Int,
    val cells: List<Int>,           // 0 empty, 1 = player, 2 = opponent
    val locked: List<Int>,          // 1 if the cell is part of a completed sequence
    val sequences: List<List<Int>>, // each completed sequence as a list of cell indices
    val winner: Int,                // 0 draw, 1 = player, 2 = opponent
    val playerName: String,
    val opponentName: String,
)

@Serializable
data class MatchRecord(
    val id: String,
    val gameId: String,
    val opponentName: String,
    val opponentChipId: String? = null,
    val result: String,             // "WIN" | "LOSS" | "DRAW"
    val durationSeconds: Int,
    val moveCount: Int,
    val timestamp: Long,
    val snapshot: BoardSnapshot,
)

/** Root persisted state. */
@Serializable
data class SavedState(
    val player: PlayerProfile = PlayerProfile(),
    val friends: List<FriendEntry> = emptyList(),
    val requests: List<FriendRequest> = emptyList(),
    val invites: List<GameInvite> = emptyList(),
    val matchHistory: List<MatchRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoggedIn: Boolean = false,
    val authEmail: String? = null,
    val authUid: String? = null,
)