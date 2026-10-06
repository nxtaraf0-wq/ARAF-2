package com.example

import com.example.data.BonusWordDictionary
import com.example.logic.PuzzleGenerator
import com.example.model.GameCategories
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCategoryOrderAndCount() {
        val categories = GameCategories.ALL
        assertEquals(15, categories.size)

        val expectedNames = listOf(
            "Animals", "Food", "Sports", "Countries", "Colors",
            "Fruits", "Space", "Ocean Life", "Music", "Movies",
            "Nature", "Technology", "Jobs", "Clothing", "Vegetables"
        )

        for (i in 0 until 15) {
            assertEquals(expectedNames[i], categories[i].name)
            assertEquals(i, categories[i].index)
        }
    }

    @Test
    fun testGridSizeRules() {
        // ≥16 words OR longest word ≥10 chars → 12×12 grid
        val animals = GameCategories.ALL[0]
        assertEquals(18, animals.words.size)
        assertEquals(12, PuzzleGenerator.calculateGridSize(animals))

        // ≥12 words OR longest word ≥7 chars → 10×10 grid
        val food = GameCategories.ALL[1]
        assertEquals(15, food.words.size)
        assertEquals(10, PuzzleGenerator.calculateGridSize(food))
    }

    @Test
    fun testPuzzleGeneration() {
        val animals = GameCategories.ALL[0]
        val puzzle = PuzzleGenerator.generatePuzzle(animals, randomSeed = 42L)

        assertEquals(12, puzzle.rows)
        assertEquals(12, puzzle.cols)
        assertTrue(puzzle.placedWords.isNotEmpty())

        // Verify all placed words have valid coordinates
        for (placed in puzzle.placedWords) {
            for (cell in placed.cells) {
                assertTrue("Row out of bounds: ${cell.row}", cell.row in 0 until puzzle.rows)
                assertTrue("Col out of bounds: ${cell.col}", cell.col in 0 until puzzle.cols)
            }
        }
    }

    @Test
    fun testBonusWordDictionary() {
        val puzzleWords = listOf("PIZZA", "BURGER", "SUSHI")

        // Words in puzzle list should NOT be bonus words
        assertFalse(BonusWordDictionary.isBonusWord("PIZZA", puzzleWords))

        // Common valid 3+ letter English words should be detected as bonus words
        assertTrue(BonusWordDictionary.isBonusWord("CAT", puzzleWords))
        assertTrue(BonusWordDictionary.isBonusWord("DOG", puzzleWords))
        assertTrue(BonusWordDictionary.isBonusWord("THE", puzzleWords))
        assertTrue(BonusWordDictionary.isBonusWord("AND", puzzleWords))
        assertTrue(BonusWordDictionary.isBonusWord("BLUE", puzzleWords))

        // Less than 3 letters should NOT be bonus
        assertFalse(BonusWordDictionary.isBonusWord("NO", puzzleWords))
        assertFalse(BonusWordDictionary.isBonusWord("A", puzzleWords))
    }
}
