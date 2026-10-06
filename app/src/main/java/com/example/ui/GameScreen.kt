package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellPos
import com.example.model.GameMode
import com.example.model.PlacedWord
import com.example.model.WordSearchPuzzle
import com.example.ui.theme.ArafBackground
import com.example.ui.theme.ArafBlue
import com.example.ui.theme.ArafDarkText
import com.example.ui.theme.ArafGrayFound
import com.example.ui.theme.ArafOrange
import com.example.ui.theme.ArafPink
import com.example.ui.theme.WordHighlightColors
import com.example.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onDragStart: (CellPos) -> Unit,
    onDragMove: (CellPos) -> Unit,
    onDragEnd: () -> Unit,
    onUseHint: () -> Unit,
    onWatchAdForHints: (Activity) -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArafBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation & Stats Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackIconButton(onBack = onBack)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.selectedCategory.emoji,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = uiState.selectedCategory.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArafDarkText
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SoundToggleButton(
                            soundEnabled = uiState.soundEnabled,
                            onToggle = onToggleSound
                        )
                    }
                }

                // 2. Score, Timer, and Coins Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E4E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score: ",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.score}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArafBlue
                            )
                        }
                    }

                    // Timer Pill (turns red if < 30s in Time Mode)
                    TimerPill(
                        mode = uiState.selectedMode,
                        timeElapsed = uiState.timeElapsed,
                        timeRemaining = uiState.timeRemaining
                    )

                    CoinChip(coins = uiState.coins)
                }
            }

            // 3. Word List Panel (Found words crossed out in their assigned highlight color)
            uiState.puzzle?.let { puzzle ->
                WordListPanel(
                    puzzle = puzzle,
                    foundWords = uiState.foundWords,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Letter Grid with Touch Pan Drag Gesture
            uiState.puzzle?.let { puzzle ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LetterGrid(
                        puzzle = puzzle,
                        foundWords = uiState.foundWords,
                        selectedCells = uiState.selectedCells,
                        hintCells = uiState.hintCells,
                        isHintFlashing = uiState.isHintFlashing,
                        onDragStart = onDragStart,
                        onDragMove = onDragMove,
                        onDragEnd = onDragEnd
                    )
                }
            }

            // 5. Bottom Action Bar: Hints, Rewarded Ads, Bonus Words
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint button with red badge
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = if (uiState.hintsRemaining > 0) Color(0xFFE53935) else Color.Gray,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "${uiState.hintsRemaining}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .shadow(3.dp, CircleShape)
                            .clickable(enabled = uiState.hintsRemaining > 0) { onUseHint() }
                            .testTag("hint_button"),
                        color = if (uiState.hintsRemaining > 0) Color(0xFFFFF9C4) else Color(0xFFEEEEEE),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.hintsRemaining > 0) Color(0xFFFBC02D) else Color.LightGray
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Use Hint",
                                tint = if (uiState.hintsRemaining > 0) Color(0xFFF57F17) else Color.Gray,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Bonus Words Counter Chip
                if (uiState.bonusWordsFound.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3E0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArafOrange)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = ArafOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.bonusWordsFound.size} Bonus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                // Rewarded Ad / Extra Hints Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clickable { activity?.let { onWatchAdForHints(it) } }
                        .testTag("rewarded_hints_button"),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArafBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Watch Ad",
                            tint = ArafBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+2 Hints",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArafBlue
                        )
                    }
                }
            }
        }

        // Floating Toast Banner for Bonus Words / Hints
        GameToastBanner(
            message = uiState.toastMessage,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        )
    }
}

@Composable
private fun TimerPill(
    mode: GameMode,
    timeElapsed: Int,
    timeRemaining: Int
) {
    val isTimeMode = mode == GameMode.TIME_ATTACK
    val isCritical = isTimeMode && timeRemaining < 30

    // Specified colors: When <30s remaining, red pill bg-red-500/30, text-red-300
    val bgColor = if (isCritical) Color(0xFFFFCDD2) else Color.White
    val textColor = if (isCritical) Color(0xFFD32F2F) else ArafDarkText
    val borderColor = if (isCritical) Color(0xFFE53935) else Color(0xFFE2E4E9)

    val minutes = if (isTimeMode) timeRemaining / 60 else timeElapsed / 60
    val seconds = if (isTimeMode) timeRemaining % 60 else timeElapsed % 60
    val timeStr = "%d:%02d".format(minutes, seconds)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = if (isCritical) 3.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = "Timer",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordListPanel(
    puzzle: WordSearchPuzzle,
    foundWords: Set<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color(0xFFE2E4E9), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET WORDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${foundWords.size} / ${puzzle.placedWords.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArafBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Display words horizontally wrapped
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                puzzle.placedWords.forEach { placed ->
                    val isFound = foundWords.contains(placed.word)
                    val color = WordHighlightColors[placed.colorIndex % WordHighlightColors.size]

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFound) color.copy(alpha = 0.2f) else Color(0xFFF4F4F6),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFound) color else Color.Transparent
                        )
                    ) {
                        Text(
                            text = placed.word,
                            fontSize = 11.sp,
                            fontWeight = if (isFound) FontWeight.Normal else FontWeight.Bold,
                            color = if (isFound) ArafGrayFound else ArafDarkText,
                            textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterGrid(
    puzzle: WordSearchPuzzle,
    foundWords: Set<String>,
    selectedCells: List<CellPos>,
    hintCells: Set<CellPos>,
    isHintFlashing: Boolean,
    onDragStart: (CellPos) -> Unit,
    onDragMove: (CellPos) -> Unit,
    onDragEnd: () -> Unit
) {
    // Map each cell coordinate to its found color
    val cellColorMap = remember(foundWords, puzzle) {
        val map = mutableMapOf<CellPos, Color>()
        puzzle.placedWords.forEach { placed ->
            if (foundWords.contains(placed.word)) {
                val color = WordHighlightColors[placed.colorIndex % WordHighlightColors.size]
                placed.cells.forEach { pos ->
                    map[pos] = color
                }
            }
        }
        map
    }

    val selectedSet = remember(selectedCells) { selectedCells.toSet() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E4E9), RoundedCornerShape(18.dp))
            .testTag("word_search_grid")
    ) {
        val gridSize = puzzle.rows
        val cellWidth = maxWidth / gridSize
        val cellHeight = maxHeight / gridSize

        // Gesture detector for drag and selection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gridSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val c = (offset.x / (size.width.toFloat() / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            val r = (offset.y / (size.height.toFloat() / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            onDragStart(CellPos(r, c))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val c = (change.position.x / (size.width.toFloat() / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            val r = (change.position.y / (size.height.toFloat() / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            onDragMove(CellPos(r, c))
                        },
                        onDragEnd = {
                            onDragEnd()
                        },
                        onDragCancel = {
                            onDragEnd()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until gridSize) {
                            val pos = CellPos(r, c)
                            val char = puzzle.grid[r][c]
                            val isSelected = selectedSet.contains(pos)
                            val isHinted = hintCells.contains(pos) && isHintFlashing
                            val foundColor = cellColorMap[pos]

                            val tileBg = when {
                                isHinted -> Color(0xFFFFEB3B) // Flash yellow for hint
                                isSelected -> ArafBlue.copy(alpha = 0.45f)
                                foundColor != null -> foundColor.copy(alpha = 0.35f) // 35% opacity
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                isHinted -> Color(0xFFE65100)
                                isSelected -> Color.White
                                foundColor != null -> ArafDarkText
                                else -> ArafDarkText
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tileBg)
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, ArafBlue, RoundedCornerShape(6.dp))
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$char",
                                    fontSize = when {
                                        gridSize <= 8 -> 20.sp
                                        gridSize <= 10 -> 17.sp
                                        else -> 14.sp
                                    },
                                    fontWeight = if (isSelected || isHinted || foundColor != null) FontWeight.Black else FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
