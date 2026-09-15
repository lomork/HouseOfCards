package com.lomork.house_of_cards.data

/** Static catalogues that ship with the app. Owned/unlocked state lives on [PlayerProfile]. */

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val points: Int,
)

data class StoreItemDef(
    val id: String,
    val name: String,
    val type: String,          // "chip" | "board" | "emote" ...
    val description: String,
    val coinPrice: Int,
    val cashPriceUsd: Int?,    // null = coins only
    val primary: Long,         // ARGB of chip face
    val secondary: Long,       // ARGB of chip rim / trim
    val label: String,
)

object SeedData {

    val achievements: List<AchievementDef> = listOf(
        AchievementDef("firstWin", "First Blood", "Win your first match", "\uD83C\uDFC5", 100),
        AchievementDef("streak3", "On a Roll", "Reach a 3-game win streak", "\uD83D\uDD25", 150),
        AchievementDef("streak5", "Unstoppable", "Reach a 5-game win streak", "\u26A1", 300),
        AchievementDef("wins10", "Sequence Master", "Win 10 games", "\u265B", 500),
        AchievementDef("chip3", "Collector", "Own 3 custom chips", "\uD83E\uDE99", 200),
        AchievementDef("social5", "Social Butterfly", "Add 5 friends", "\uD83E\uDD1D", 150),
    )

    val defaultChip = StoreItemDef(
        "chip_default", "Classic", "chip",
        "The standard house chip — the one everyone starts with.",
        0, null, 0xFFD9B56A, 0xFF15181E, "\u2605",
    )

    val storeItems: List<StoreItemDef> = listOf(
        defaultChip,
        StoreItemDef("chip_gold", "Royal Gold", "chip", "A regal two-tone chip for high rollers.", 400, 199, 0xFFE6C35A, 0xFF2A2112, "\u265B"),
        StoreItemDef("chip_crimson", "Crimson", "chip", "Deep red with a gold trim.", 300, 99, 0xFFB04A4A, 0xFF5A1414, "\u2660"),
        StoreItemDef("chip_emerald", "Emerald", "chip", "Felt green and ivory — dealer's choice.", 350, 99, 0xFF3E8A66, 0xFF143526, "\u2665"),
        StoreItemDef("chip_sapphire", "Sapphire", "chip", "Cool blue with a silver edge.", 350, 99, 0xFF4570B8, 0xFF16263F, "\u2666"),
        StoreItemDef("chip_obsidian", "Obsidian", "chip", "Black glass with a gold glow.", 500, 299, 0xFF272B33, 0xFFD9B56A, "\u2663"),
        StoreItemDef("chip_rosewood", "Rosewood", "chip", "Warm mahogany and cream.", 300, 99, 0xFF8A5A44, 0xFF3A2018, "\u25C6"),
        StoreItemDef("chip_ivory", "Ivory", "chip", "Cream and gold — understated luxury.", 450, 249, 0xFFF1EBDD, 0xFFD9B56A, "\u2726"),
        StoreItemDef("chip_amethyst", "Amethyst", "chip", "Violet with a silver edge.", 400, 199, 0xFF8A5FBF, 0xFF2A1A40, "\u2727"),
    )

    fun chipById(id: String?): StoreItemDef =
        storeItems.firstOrNull { it.id == id } ?: defaultChip

    fun freshState(): SavedState = SavedState(
        player = PlayerProfile(username = "Jack", coins = 500),
        friends = listOf(
            FriendEntry("f1", "Aria", level = 4, streak = 2, region = "Europe"),
            FriendEntry("f2", "Kian", level = 3, streak = 0, region = "Asia"),
            FriendEntry("f3", "Noor", level = 7, streak = 3, region = "North America"),
        ),
        requests = listOf(
            FriendRequest("req_1", "Milo", level = 5, incoming = true, timestamp = nowMillis() - 3_600_000),
            FriendRequest("req_2", "Sofia", level = 2, incoming = true, timestamp = nowMillis() - 7_200_000),
            FriendRequest("req_3", "Theo", level = 6, incoming = true, timestamp = nowMillis() - 10_800_000),
            FriendRequest("req_4", "Lena", level = 1, incoming = false, timestamp = nowMillis() - 14_400_000),
        ),
        invites = listOf(
            GameInvite("inv_1", "f1", "Aria", "sequence", nowMillis() - 1_800_000),
            GameInvite("inv_2", "req_2", "Sofia", "sequence", nowMillis() - 5_400_000),
        ),
    )
}