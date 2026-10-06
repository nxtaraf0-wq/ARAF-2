package com.example.model

data class CellPos(
    val row: Int,
    val col: Int
) {
    override fun toString(): String = "($row,$col)"
}

enum class GameMode(val title: String, val description: String) {
    CLASSIC(
        title = "Classic Mode",
        description = "No time limit. Relax and find all the words at your own pace."
    ),
    TIME_ATTACK(
        title = "Time Mode",
        description = "2-Minute countdown! Earn 2x time bonus points for remaining seconds."
    )
}

data class PlacedWord(
    val word: String,
    val cells: List<CellPos>,
    val colorIndex: Int = 0,
    var isFound: Boolean = false
)

data class WordSearchPuzzle(
    val category: Category,
    val rows: Int,
    val cols: Int,
    val grid: Array<CharArray>,
    val placedWords: List<PlacedWord>
) {
    fun findWord(w: String): PlacedWord? {
        val upper = w.uppercase()
        return placedWords.firstOrNull { it.word.equals(upper, ignoreCase = true) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WordSearchPuzzle
        return category == other.category && rows == other.rows && cols == other.cols
    }

    override fun hashCode(): Int {
        var result = category.hashCode()
        result = 31 * result + rows
        result = 31 * result + cols
        return result
    }
}
