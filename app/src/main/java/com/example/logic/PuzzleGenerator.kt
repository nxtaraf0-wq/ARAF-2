package com.example.logic

import com.example.model.Category
import com.example.model.CellPos
import com.example.model.PlacedWord
import com.example.model.WordSearchPuzzle
import kotlin.random.Random

object PuzzleGenerator {

    private val DIRECTIONS = listOf(
        Pair(1, 0),   // East (Horizontal right)
        Pair(-1, 0),  // West (Horizontal left)
        Pair(0, 1),   // South (Vertical down)
        Pair(0, -1),  // North (Vertical up)
        Pair(1, 1),   // South-East (Diagonal down-right)
        Pair(-1, 1),  // South-West (Diagonal down-left)
        Pair(1, -1),  // North-East (Diagonal up-right)
        Pair(-1, -1)  // North-West (Diagonal up-left)
    )

    fun calculateGridSize(category: Category): Int {
        val wordCount = category.words.size
        val longestWordLen = category.words.maxOfOrNull { it.length } ?: 0

        return when {
            wordCount >= 16 || longestWordLen >= 10 -> 12
            wordCount >= 12 || longestWordLen >= 7 -> 10
            else -> 8
        }
    }

    fun generatePuzzle(category: Category, randomSeed: Long? = null): WordSearchPuzzle {
        val size = calculateGridSize(category)
        val random = if (randomSeed != null) Random(randomSeed) else Random.Default

        // Sort words by length descending to place harder/longer words first
        val wordsToPlace = category.words.sortedByDescending { it.length }

        var bestGrid: Array<CharArray>? = null
        var bestPlacedWords: List<PlacedWord> = emptyList()

        // Multiple board generation attempts to guarantee all words fit
        for (attempt in 0 until 50) {
            val grid = Array(size) { CharArray(size) { ' ' } }
            val placedList = mutableListOf<PlacedWord>()
            var allPlaced = true

            for ((colorIdx, word) in wordsToPlace.withIndex()) {
                val cleanWord = word.uppercase().filter { it.isLetter() }
                val placed = tryPlaceWord(grid, size, cleanWord, colorIdx, random)
                if (placed != null) {
                    placedList.add(placed)
                } else {
                    allPlaced = false
                    break
                }
            }

            if (allPlaced) {
                bestGrid = grid
                bestPlacedWords = placedList
                break
            } else if (placedList.size > bestPlacedWords.size) {
                bestGrid = grid
                bestPlacedWords = placedList
            }
        }

        val finalGrid = bestGrid ?: Array(size) { CharArray(size) { ' ' } }

        // Fill empty cells with random letters
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (finalGrid[r][c] == ' ') {
                    finalGrid[r][c] = ('A'..'Z').random(random)
                }
            }
        }

        // Sort placed words back to original category order for predictable display
        val originalOrder = category.words.map { it.uppercase().filter { ch -> ch.isLetter() } }
        val orderedPlacedWords = bestPlacedWords.sortedBy { originalOrder.indexOf(it.word) }

        return WordSearchPuzzle(
            category = category,
            rows = size,
            cols = size,
            grid = finalGrid,
            placedWords = orderedPlacedWords
        )
    }

    private fun tryPlaceWord(
        grid: Array<CharArray>,
        size: Int,
        word: String,
        colorIndex: Int,
        random: Random
    ): PlacedWord? {
        val wordLen = word.length
        if (wordLen > size) return null

        val directionsShuffled = DIRECTIONS.shuffled(random)
        val allCoordinates = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                allCoordinates.add(Pair(r, c))
            }
        }
        allCoordinates.shuffle(random)

        for ((startR, startC) in allCoordinates) {
            for ((dx, dy) in directionsShuffled) {
                val endR = startR + dy * (wordLen - 1)
                val endC = startC + dx * (wordLen - 1)

                if (endR in 0 until size && endC in 0 until size) {
                    var canPlace = true
                    val cellList = mutableListOf<CellPos>()

                    for (i in 0 until wordLen) {
                        val r = startR + dy * i
                        val c = startC + dx * i
                        val currentCellChar = grid[r][c]
                        val wordChar = word[i]

                        if (currentCellChar != ' ' && currentCellChar != wordChar) {
                            canPlace = false
                            break
                        }
                        cellList.add(CellPos(r, c))
                    }

                    if (canPlace) {
                        // Place letters onto the grid
                        for (i in 0 until wordLen) {
                            val r = startR + dy * i
                            val c = startC + dx * i
                            grid[r][c] = word[i]
                        }
                        return PlacedWord(
                            word = word,
                            cells = cellList,
                            colorIndex = colorIndex,
                            isFound = false
                        )
                    }
                }
            }
        }
        return null
    }
}
