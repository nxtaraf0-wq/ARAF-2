package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.BonusWordDictionary
import com.example.data.GamePreferences
import com.example.logic.PuzzleGenerator
import com.example.logic.SoundEvent
import com.example.logic.SoundManager
import com.example.model.Category
import com.example.model.CellPos
import com.example.model.GameCategories
import com.example.model.GameMode
import com.example.model.PlacedWord
import com.example.model.WordSearchPuzzle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

enum class Screen {
    HOME,
    CATEGORY_SELECT,
    MODE_SELECT,
    GAME,
    RESULTS
}

data class GameUiState(
    val currentScreen: Screen = Screen.HOME,
    val selectedCategory: Category = GameCategories.ALL[0],
    val selectedMode: GameMode = GameMode.CLASSIC,
    val puzzle: WordSearchPuzzle? = null,
    val foundWords: Set<String> = emptySet(),
    val bonusWordsFound: Set<String> = emptySet(),
    val score: Int = 0,
    val timeElapsed: Int = 0,
    val timeRemaining: Int = 120, // 2 minutes countdown for Time mode
    val hintsRemaining: Int = 3,
    val hintCells: Set<CellPos> = emptySet(),
    val isHintFlashing: Boolean = false,
    val selectedCells: List<CellPos> = emptyList(),
    val coins: Int = 300,
    val completedLevels: Set<String> = emptySet(),
    val isLevelCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val earnedCoinsThisRound: Int = 0,
    val timeBonus: Int = 0,
    val soundEnabled: Boolean = true,
    val toastMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GamePreferences(application)
    val soundManager = SoundManager(application)
    val adManager = AdManager(application)

    private val _uiState = MutableStateFlow(
        GameUiState(
            coins = prefs.coins,
            completedLevels = prefs.completedLevels,
            soundEnabled = prefs.soundEnabled
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var dragStartCell: CellPos? = null

    init {
        soundManager.isSoundEnabled = prefs.soundEnabled
    }

    fun navigateTo(screen: Screen) {
        soundManager.play(SoundEvent.BUTTON_TAP)
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun toggleSound() {
        val next = !uiState.value.soundEnabled
        prefs.soundEnabled = next
        soundManager.isSoundEnabled = next
        _uiState.update { it.copy(soundEnabled = next) }
        if (next) soundManager.play(SoundEvent.BUTTON_TAP)
    }

    fun selectCategory(category: Category) {
        soundManager.play(SoundEvent.BUTTON_TAP)
        _uiState.update {
            it.copy(
                selectedCategory = category,
                currentScreen = Screen.MODE_SELECT
            )
        }
    }

    fun startGame(mode: GameMode) {
        soundManager.play(SoundEvent.BUTTON_TAP)
        val category = _uiState.value.selectedCategory
        val puzzle = PuzzleGenerator.generatePuzzle(category)

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                selectedMode = mode,
                puzzle = puzzle,
                foundWords = emptySet(),
                bonusWordsFound = emptySet(),
                score = 0,
                timeElapsed = 0,
                timeRemaining = 120,
                hintsRemaining = 3,
                hintCells = emptySet(),
                isHintFlashing = false,
                selectedCells = emptyList(),
                isLevelCompleted = false,
                isGameOver = false,
                earnedCoinsThisRound = 0,
                timeBonus = 0,
                currentScreen = Screen.GAME
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (state.currentScreen != Screen.GAME || state.isLevelCompleted || state.isGameOver) {
                    break
                }

                if (state.selectedMode == GameMode.CLASSIC) {
                    _uiState.update { it.copy(timeElapsed = it.timeElapsed + 1) }
                } else {
                    val nextTime = state.timeRemaining - 1
                    if (nextTime <= 0) {
                        // Time over!
                        _uiState.update { it.copy(timeRemaining = 0, isGameOver = true) }
                        soundManager.play(SoundEvent.GAME_OVER)
                        delay(500)
                        _uiState.update { it.copy(currentScreen = Screen.RESULTS) }
                        break
                    } else {
                        if (nextTime < 30) {
                            soundManager.play(SoundEvent.COUNTDOWN_TICK)
                        }
                        _uiState.update { it.copy(timeRemaining = nextTime) }
                    }
                }
            }
        }
    }

    fun onDragStart(pos: CellPos) {
        dragStartCell = pos
        soundManager.play(SoundEvent.DRAG_SWIPE)
        _uiState.update { it.copy(selectedCells = listOf(pos)) }
    }

    fun onDragMove(currentPos: CellPos) {
        val start = dragStartCell ?: return
        val puzzle = _uiState.value.puzzle ?: return

        val dr = currentPos.row - start.row
        val dc = currentPos.col - start.col
        if (dr == 0 && dc == 0) {
            _uiState.update { it.copy(selectedCells = listOf(start)) }
            return
        }

        // Constrain to 8 directions (horizontal, vertical, or 45-degree diagonal)
        val absDr = abs(dr)
        val absDc = abs(dc)

        val (stepR, stepC, length) = when {
            absDr == 0 -> Triple(0, dc.sign, absDc)
            absDc == 0 -> Triple(dr.sign, 0, absDr)
            absDr == absDc -> Triple(dr.sign, dc.sign, absDr)
            absDr > absDc * 2 -> Triple(dr.sign, 0, absDr)
            absDc > absDr * 2 -> Triple(0, dc.sign, absDc)
            else -> {
                val len = minOf(absDr, absDc)
                Triple(dr.sign, dc.sign, len)
            }
        }

        val cells = mutableListOf<CellPos>()
        for (i in 0..length) {
            val r = start.row + stepR * i
            val c = start.col + stepC * i
            if (r in 0 until puzzle.rows && c in 0 until puzzle.cols) {
                cells.add(CellPos(r, c))
            } else {
                break
            }
        }
        _uiState.update { it.copy(selectedCells = cells) }
    }

    fun onDragEnd() {
        val state = _uiState.value
        val puzzle = state.puzzle
        val cells = state.selectedCells

        dragStartCell = null
        if (puzzle == null || cells.isEmpty()) {
            _uiState.update { it.copy(selectedCells = emptyList()) }
            return
        }

        // Build word from selected cells
        val forwardWord = cells.map { puzzle.grid[it.row][it.col] }.joinToString("")
        val backwardWord = forwardWord.reversed()

        // 1. Check if it matches any unfound target puzzle word
        val matchingPlacedWord: PlacedWord? = puzzle.placedWords.firstOrNull { placed ->
            !state.foundWords.contains(placed.word) &&
                    (placed.word.equals(forwardWord, ignoreCase = true) ||
                            placed.word.equals(backwardWord, ignoreCase = true))
        }

        if (matchingPlacedWord != null) {
            // Found a puzzle word!
            val matchedWord = matchingPlacedWord.word
            val updatedFound = state.foundWords + matchedWord
            val newScore = state.score + 10
            matchingPlacedWord.isFound = true

            soundManager.play(SoundEvent.WORD_FOUND)
            prefs.incrementWordsFound(1)

            val isComplete = updatedFound.size >= puzzle.placedWords.size

            if (isComplete) {
                timerJob?.cancel()
                val earnedCoins = 50
                val timeBonus = if (state.selectedMode == GameMode.TIME_ATTACK) state.timeRemaining * 2 else 0
                val finalScore = newScore + timeBonus

                prefs.addCoins(earnedCoins)
                prefs.markLevelCompleted(puzzle.category.id)
                prefs.updateHighScore(finalScore)
                prefs.incrementGamesPlayed()

                soundManager.play(SoundEvent.LEVEL_COMPLETE)

                _uiState.update {
                    it.copy(
                        foundWords = updatedFound,
                        score = finalScore,
                        timeBonus = timeBonus,
                        earnedCoinsThisRound = earnedCoins,
                        coins = prefs.coins,
                        completedLevels = prefs.completedLevels,
                        isLevelCompleted = true,
                        selectedCells = emptyList(),
                        currentScreen = Screen.RESULTS
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        foundWords = updatedFound,
                        score = newScore,
                        selectedCells = emptyList()
                    )
                }
            }
            return
        }

        // 2. Check if it's a bonus word
        val allPuzzleWords = puzzle.placedWords.map { it.word }
        val checkWord = forwardWord.uppercase()
        val checkReverse = backwardWord.uppercase()

        val isForwardBonus = BonusWordDictionary.isBonusWord(checkWord, allPuzzleWords)
        val isReverseBonus = BonusWordDictionary.isBonusWord(checkReverse, allPuzzleWords)

        val bonusCandidate = when {
            isForwardBonus && !state.bonusWordsFound.contains(checkWord) -> checkWord
            isReverseBonus && !state.bonusWordsFound.contains(checkReverse) -> checkReverse
            else -> null
        }

        if (bonusCandidate != null) {
            val updatedBonus = state.bonusWordsFound + bonusCandidate
            val newScore = state.score + 5
            soundManager.play(SoundEvent.BONUS_WORD)

            showToast("🌟 Bonus Word: $bonusCandidate (+5 pts)!")

            _uiState.update {
                it.copy(
                    bonusWordsFound = updatedBonus,
                    score = newScore,
                    selectedCells = emptyList()
                )
            }
            return
        }

        // 3. Selection was invalid
        if (cells.size > 1) {
            soundManager.play(SoundEvent.WRONG_SELECTION)
        }
        _uiState.update { it.copy(selectedCells = emptyList()) }
    }

    fun useHint() {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        if (state.hintsRemaining <= 0) return

        // Pick a random unfound word
        val unfoundWords = puzzle.placedWords.filter { !state.foundWords.contains(it.word) }
        if (unfoundWords.isEmpty()) return

        val targetWord = unfoundWords.random()
        soundManager.play(SoundEvent.BONUS_WORD)

        _uiState.update {
            it.copy(
                hintsRemaining = it.hintsRemaining - 1,
                hintCells = targetWord.cells.toSet(),
                isHintFlashing = true
            )
        }

        viewModelScope.launch {
            // Flash 3 times over 1.5 seconds (250ms on, 250ms off)
            for (i in 0 until 3) {
                _uiState.update { it.copy(isHintFlashing = true) }
                delay(250)
                _uiState.update { it.copy(isHintFlashing = false) }
                delay(250)
            }
            _uiState.update { it.copy(hintCells = emptySet(), isHintFlashing = false) }
        }
    }

    fun watchRewardedAdForHints(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                soundManager.play(SoundEvent.BONUS_WORD)
                _uiState.update { it.copy(hintsRemaining = it.hintsRemaining + 2) }
                showToast("🎁 +2 Hints Added!")
            },
            onAdUnavailable = {
                // Friendly offline fallback: allow spending 50 coins or grant 1 free hint if coins are low
                if (_uiState.value.coins >= 50) {
                    prefs.addCoins(-50)
                    _uiState.update {
                        it.copy(
                            coins = prefs.coins,
                            hintsRemaining = it.hintsRemaining + 2
                        )
                    }
                    soundManager.play(SoundEvent.BONUS_WORD)
                    showToast("💡 50 Coins used for +2 Hints!")
                } else {
                    _uiState.update { it.copy(hintsRemaining = it.hintsRemaining + 1) }
                    soundManager.play(SoundEvent.BONUS_WORD)
                    showToast("💡 +1 Free Offline Hint Granted!")
                }
            }
        )
    }

    fun showInterstitialOnCompletion(activity: Activity, onDone: () -> Unit = {}) {
        adManager.showInterstitialIfEligible(activity) {
            onDone()
        }
    }

    fun nextLevel() {
        val currentIdx = _uiState.value.selectedCategory.index
        val nextCategory = GameCategories.ALL.getOrNull(currentIdx + 1)
        if (nextCategory != null) {
            selectCategory(nextCategory)
        } else {
            navigateTo(Screen.CATEGORY_SELECT)
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(toastMessage = message) }
            delay(2500)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
