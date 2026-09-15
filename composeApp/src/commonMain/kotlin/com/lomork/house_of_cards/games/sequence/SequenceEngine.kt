package com.lomork.house_of_cards.games.sequence

import com.lomork.house_of_cards.data.BoardSnapshot
import com.lomork.house_of_cards.data.nowMillis
import kotlin.random.Random

const val BOARD_SIZE = 10
const val HAND_SIZE = 6
const val WIN_SEQUENCES = 2
const val SEQUENCE_LENGTH = 5

enum class Suit(val symbol: String, val red: Boolean) {
    CLUBS("\u2663", false),
    DIAMONDS("\u2666", true),
    HEARTS("\u2665", true),
    SPADES("\u2660", false),
}

enum class Rank(val label: String) {
    ACE("A"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"),
    SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"),
    JACK("J"), QUEEN("Q"), KING("K"),
}

data class BoardCard(val rank: Rank, val suit: Suit)

sealed interface Card {
    data class Standard(val rank: Rank, val suit: Suit) : Card {
        val display: String get() = rank.label + suit.symbol
    }
    data class Joker(val type: JokerType) : Card {
        val display: String get() = if (type == JokerType.BLACK) "Black Joker" else "Red Joker"
    }
}

enum class JokerType {
    /** Wild — place a chip on any empty (non-corner) cell. */
    BLACK,
    /** Removal — take an opponent chip off anywhere except a completed sequence. */
    RED,
}

data class Cell(val row: Int, val col: Int) {
    val index: Int get() = row * BOARD_SIZE + col
    val label: String get() = "${col + 1}${('A'..'Z').toList()[row.coerceIn(0, 25)]}"
}

enum class Player(val id: Int) {
    ONE(1), TWO(2);
    fun other(): Player = if (this == ONE) TWO else ONE
}

sealed interface Move {
    val card: Card
    data class Place(override val card: Card, val cell: Cell) : Move
    data class Remove(override val card: Card, val cell: Cell) : Move
}

/**
 * The permanent, fixed board. It is deterministic — every install produces the
 * exact same layout (that is what makes it "permanent" rather than generated).
 *
 * 10 x 10 grid. The four corners are free spaces (they count toward any player's
 * sequence). The remaining 96 cells are labelled with the 48 non-jack cards
 * (each appears exactly twice). Jacks are the special cards and never appear on
 * the board: the "Black Joker" plays anywhere, the "Red Joker" removes a chip.
 */
object BoardLayout {
    val corners: Set<Cell> = setOf(
        Cell(0, 0), Cell(0, BOARD_SIZE - 1),
        Cell(BOARD_SIZE - 1, 0), Cell(BOARD_SIZE - 1, BOARD_SIZE - 1),
    )

    val cells: List<List<BoardCard?>> = buildLayout()

    fun isCorner(cell: Cell): Boolean = cell in corners
    fun cardAt(cell: Cell): BoardCard? = cells[cell.row][cell.col]

    private fun buildLayout(): List<List<BoardCard?>> {
        val types = Rank.entries.filter { it != Rank.JACK }
            .flatMap { r -> Suit.entries.map { s -> BoardCard(r, s) } }
        val all = (types + types).toMutableList()
        val rnd = DeterministicRandom(80807331L)
        for (i in all.size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val t = all[i]; all[i] = all[j]; all[j] = t
        }
        val grid = Array(BOARD_SIZE) { arrayOfNulls<BoardCard>(BOARD_SIZE) }
        var k = 0
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (!isCorner(Cell(row, col))) grid[row][col] = all[k++]
            }
        }
        return grid.map { it.toList() }
    }
}

class DeterministicRandom(private var state: Long) {
    fun nextInt(bound: Int): Int {
        state = state * 6364136223846793005L + 1442695040888963407L
        val v = (state ushr 33).toInt()
        val m = if (bound <= 0) 1 else bound
        return ((v % m) + m) % m
    }
}

class SequenceGame(seed: Long = nowMillis(), private val rng: Random = Random(seed)) {

    val occupancy = IntArray(BOARD_SIZE * BOARD_SIZE) // 0 empty, 1 = ONE, 2 = TWO
    val locked = BooleanArray(BOARD_SIZE * BOARD_SIZE)
    val sequences = mutableListOf<List<Int>>()
    private val seqCount = intArrayOf(0, 0)

    private val deck: MutableList<Card> = buildDeck().also { it.shuffle(rng) }
    private val discardPile = mutableListOf<Card>()
    private val hands = mapOf(Player.ONE to mutableListOf<Card>(), Player.TWO to mutableListOf<Card>())
    private val moveHistory = mutableListOf<Move>()

    var current: Player = Player.ONE
        private set

    val moveCount: Int get() = moveHistory.size

    init {
        for (i in 0 until HAND_SIZE) { draw(Player.ONE); draw(Player.TWO) }
    }

    private fun buildDeck(): MutableList<Card> {
        val cards = mutableListOf<Card>()
        val ranks = Rank.entries.filter { it != Rank.JACK }
        repeat(2) { for (r in ranks) for (s in Suit.entries) cards.add(Card.Standard(r, s)) }
        repeat(4) { cards.add(Card.Joker(JokerType.BLACK)) }
        repeat(4) { cards.add(Card.Joker(JokerType.RED)) }
        return cards
    }

    fun ownerAt(cell: Cell): Player? = when (occupancy[cell.index]) {
        1 -> Player.ONE
        2 -> Player.TWO
        else -> null
    }

    fun isLocked(cell: Cell): Boolean = locked[cell.index]
    fun isCorner(cell: Cell): Boolean = BoardLayout.isCorner(cell)
    fun boardCardAt(cell: Cell): BoardCard? = BoardLayout.cardAt(cell)
    fun sequencesFor(p: Player): Int = seqCount[p.ordinal]
    fun handOf(p: Player): List<Card> = hands.getValue(p).toList()

    fun winner(): Player? = when {
        seqCount[0] >= WIN_SEQUENCES -> Player.ONE
        seqCount[1] >= WIN_SEQUENCES -> Player.TWO
        else -> null
    }

    fun legalMoves(p: Player): List<Move> {
        val opp = p.other()
        val out = mutableListOf<Move>()
        for (card in handOf(p)) {
            when (card) {
                is Card.Standard -> {
                    val target = BoardCard(card.rank, card.suit)
                    forEachCell { cell ->
                        if (!isCorner(cell) && occupancy[cell.index] == 0 && boardCardAt(cell) == target) {
                            out.add(Move.Place(card, cell))
                        }
                    }
                }
                is Card.Joker -> when (card.type) {
                    JokerType.BLACK -> forEachCell { cell ->
                        if (!isCorner(cell) && occupancy[cell.index] == 0) out.add(Move.Place(card, cell))
                    }
                    JokerType.RED -> forEachCell { cell ->
                        if (!isCorner(cell) && occupancy[cell.index] == opp.id && !locked[cell.index]) {
                            out.add(Move.Remove(card, cell))
                        }
                    }
                }
            }
        }
        return out
    }

    fun applyMove(p: Player, move: Move): Boolean {
        if (p != current) return false
        if (move !in legalMoves(p)) return false
        when (move) {
            is Move.Place -> {
                occupancy[move.cell.index] = p.id
                discardFromHand(p, move.card)
                draw(p)
                afterPlace(p, move.cell)
            }
            is Move.Remove -> {
                occupancy[move.cell.index] = 0
                discardFromHand(p, move.card)
                draw(p)
            }
        }
        moveHistory.add(move)
        current = p.other()
        return true
    }

    private fun discardFromHand(p: Player, card: Card) {
        val hand = hands.getValue(p)
        val idx = hand.indexOf(card)
        if (idx >= 0) { hand.removeAt(idx); discardPile.add(card) }
    }

    private fun draw(p: Player) {
        if (deck.isEmpty() && discardPile.isNotEmpty()) {
            deck.addAll(discardPile)
            discardPile.clear()
            deck.shuffle(rng)
        }
        if (deck.isNotEmpty()) hands.getValue(p).add(deck.removeAt(deck.lastIndex))
    }

    private fun afterPlace(p: Player, placed: Cell) {
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

        // Collect every *new* sequence completed by this placement across all four
        // lines before locking anything, so sequences that share a single chip (the
        // one just placed) or a free corner still each count independently.
        val newSequences = mutableListOf<List<Int>>()
        for ((dr, dc) in directions) {
            val run = collectRun(p, placed, dr, dc)
            if (run.size < SEQUENCE_LENGTH) continue
            for (start in 0..run.size - SEQUENCE_LENGTH) {
                val window = run.subList(start, start + SEQUENCE_LENGTH)
                // Only a drop of five cells with no previously locked chip counts;
                // a later chip that merely extends an existing sequence is ignored.
                if (window.none { locked[it.index] }) {
                    newSequences.add(window.map { it.index })
                    break
                }
            }
        }

        val seen = mutableSetOf<Set<Int>>()
        for (window in newSequences) {
            val key = window.toSet()
            if (key.size < SEQUENCE_LENGTH || !seen.add(key)) continue
            // Lock only the player's own chips — free corners stay shared forever.
            for (idx in window) if (occupancy[idx] == p.id) locked[idx] = true
            seqCount[p.ordinal]++
            sequences.add(window)
        }
    }

    private fun collectRun(p: Player, start: Cell, dr: Int, dc: Int): List<Cell> {
        val cells = mutableListOf<Cell>()
        var r = start.row; var c = start.col
        while (inBounds(r, c) && belongsTo(p, Cell(r, c))) { cells.add(Cell(r, c)); r += dr; c += dc }
        val front = mutableListOf<Cell>()
        r = start.row - dr; c = start.col - dc
        while (inBounds(r, c) && belongsTo(p, Cell(r, c))) { front.add(Cell(r, c)); r -= dr; c -= dc }
        return front.asReversed() + cells
    }

    private fun belongsTo(p: Player, cell: Cell): Boolean =
        occupancy[cell.index] == p.id || isCorner(cell)

    private fun inBounds(r: Int, c: Int) = r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE

    private inline fun forEachCell(block: (Cell) -> Unit) {
        for (row in 0 until BOARD_SIZE) for (col in 0 until BOARD_SIZE) block(Cell(row, col))
    }

    fun snapshot(playerName: String, opponentName: String, winnerId: Int): BoardSnapshot = BoardSnapshot(
        size = BOARD_SIZE,
        cells = occupancy.toList(),
        locked = locked.map { if (it) 1 else 0 },
        sequences = sequences.toList(),
        winner = winnerId,
        playerName = playerName,
        opponentName = opponentName,
    )
}

/** Simple line / threat evaluation shared by the AI. */
private fun inBounds(r: Int, c: Int) = r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE

private fun isRunCell(game: SequenceGame, p: Player, cell: Cell, extra: Cell): Boolean =
    if (cell == extra) true else game.ownerAt(cell) == p || game.isCorner(cell)

private fun lineLength(game: SequenceGame, p: Player, extra: Cell, dr: Int, dc: Int): Int {
    var len = 1
    var r = extra.row + dr; var c = extra.col + dc
    while (inBounds(r, c) && isRunCell(game, p, Cell(r, c), extra)) { len++; r += dr; c += dc }
    r = extra.row - dr; c = extra.col - dc
    while (inBounds(r, c) && isRunCell(game, p, Cell(r, c), extra)) { len++; r -= dr; c -= dc }
    return len
}

fun maxLine(game: SequenceGame, p: Player, extra: Cell): Int =
    listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        .maxOf { lineLength(game, p, extra, it.first, it.second) }

private fun moveValue(game: SequenceGame, p: Player, move: Move): Int = when (move) {
    is Move.Place -> maxLine(game, p, move.cell) * 10 + (if (move.card is Card.Joker) 2 else 0)
    is Move.Remove -> maxLine(game, p.other(), move.cell) * 10
}

class SequenceAi(private val rng: Random = Random.Default) {

    /**
     * Picks a human-plausible move: complete our own sequence, block the
     * opponent's immediate win, extend our best line, then prune the opponent's
     * strongest line. Probability gates and value jitter add realism.
     */
    fun chooseMove(game: SequenceGame, p: Player): Move {
        val moves = game.legalMoves(p)
        if (moves.isEmpty()) return Move.Place(Card.Joker(JokerType.BLACK), Cell(0, 0))
        val opp = p.other()
        val places = moves.filterIsInstance<Move.Place>()
        val removals = moves.filterIsInstance<Move.Remove>()

        val winning = places.filter { maxLine(game, p, it.cell) >= SEQUENCE_LENGTH }
        if (winning.isNotEmpty() && rng.nextFloat() < 0.96f) {
            return best(winning) { moveValue(game, p, it) }
        }

        val danger = dangerCells(game, opp)
        if (danger.isNotEmpty() && rng.nextFloat() < 0.9f) {
            val blockers = places.filter { it.cell in danger }
            if (blockers.isNotEmpty()) return best(blockers) { moveValue(game, p, it) }
        }

        if (places.isNotEmpty() && rng.nextFloat() < 0.85f) {
            val top = best(places) { maxLine(game, p, it.cell) }
            if (maxLine(game, p, top.cell) >= 3) return top
        }

        if (removals.isNotEmpty() && rng.nextFloat() < 0.7f) {
            val threat = removals.maxByOrNull { maxLine(game, opp, it.cell) }
            if (threat != null && maxLine(game, opp, threat.cell) >= 3) return threat
        }

        return if (places.isNotEmpty()) best(places) { moveValue(game, p, it) }
            else moves[rng.nextInt(moves.size)]
    }

    /** The worst legal move — used when a player runs out of time. */
    fun worstMove(game: SequenceGame, p: Player): Move {
        val moves = game.legalMoves(p)
        if (moves.isEmpty()) return Move.Place(Card.Joker(JokerType.BLACK), Cell(0, 0))
        return moves.minByOrNull { moveValue(game, p, it) } ?: moves.first()
    }

    private fun dangerCells(game: SequenceGame, opp: Player): Set<Cell> {
        val cells = mutableSetOf<Cell>()
        for (row in 0 until BOARD_SIZE) for (col in 0 until BOARD_SIZE) {
            val cell = Cell(row, col)
            if (!game.isCorner(cell) && game.ownerAt(cell) == null &&
                maxLine(game, opp, cell) >= SEQUENCE_LENGTH
            ) cells.add(cell)
        }
        return cells
    }

    private inline fun <T> best(items: List<T>, score: (T) -> Int): T =
        items.maxByOrNull { score(it) + rng.nextInt(4) } ?: items.first()
}