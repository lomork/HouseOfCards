package com.lomork.house_of_cards.games

/** How a match is being played. */
enum class GameMode {
    ONLINE,   // vs a stranger (online; falls back to a human-like AI when no one is found)
    FRIENDS,  // vs a friend
    OFFLINE,  // vs the local AI
}

data class GameDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: String,
)

object GameRegistry {
    val games: List<GameDef> = listOf(
        GameDef(
            id = "sequence",
            title = "Jack's Lines",
            subtitle = "Sequence",
            description = "Five in a row to win. The Black Joker plays anywhere; the Red Joker removes an opponent's chip — but never from a finished sequence.",
            icon = "\u2660",
        ),
    )

    fun byId(id: String): GameDef? = games.firstOrNull { it.id == id }
}