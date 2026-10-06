package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassDisabled
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameCategories
import com.example.model.GameMode
import com.example.ui.theme.ArafBackground
import com.example.ui.theme.ArafBlue
import com.example.ui.theme.ArafDarkText
import com.example.ui.theme.ArafOrange
import com.example.ui.theme.ArafPink
import com.example.viewmodel.GameUiState

@Composable
fun ResultsScreen(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onNextLevel: () -> Unit,
    onHome: () -> Unit,
    onTriggerInterstitial: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        onHome()
    }

    // Trigger interstitial ad according to frequency cap (every 2nd trigger, min 60s)
    LaunchedEffect(Unit) {
        activity?.let { onTriggerInterstitial(it) }
    }

    val scaleAnim = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val isVictory = uiState.isLevelCompleted
    val hasNextLevel = uiState.selectedCategory.index < GameCategories.ALL.size - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArafBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top spacing
            Spacer(modifier = Modifier.height(12.dp))

            // Victory or Game Over Icon & Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scaleAnim.value)
            ) {
                Surface(
                    modifier = Modifier
                        .size(86.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = if (isVictory) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isVictory) Icons.Default.CheckCircle else Icons.Default.HourglassDisabled,
                            contentDescription = null,
                            tint = if (isVictory) Color(0xFF4CAF50) else Color(0xFFE53935),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isVictory) "PUZZLE COMPLETED!" else "TIME'S UP!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = ArafDarkText,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${uiState.selectedCategory.emoji} ${uiState.selectedCategory.name} • ${uiState.selectedMode.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score & Earnings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E4E9), RoundedCornerShape(22.dp))
                    .shadow(4.dp, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Total Score
                    Text(
                        text = "TOTAL SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "${uiState.score}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = ArafBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Personal Best Trophy Banner
                    if (uiState.isNewPersonalBest) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA000),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NEW PERSONAL BEST!",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFB78103)
                                    )
                                }
                                val m = uiState.timeElapsed / 60
                                val s = uiState.timeElapsed % 60
                                Text(
                                    text = "Record Time: %d:%02d".format(m, s),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF795548),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (uiState.previousBestTime != null) {
                                    val pm = uiState.previousBestTime / 60
                                    val ps = uiState.previousBestTime % 60
                                    val diff = uiState.previousBestTime - uiState.timeElapsed
                                    Text(
                                        text = "Previous: %d:%02d (-%ds faster!)".format(pm, ps, diff),
                                        fontSize = 11.sp,
                                        color = Color(0xFF8D6E63)
                                    )
                                }
                            }
                        }
                    }

                    // Coins Reward banner
                    if (isVictory) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "+50 COINS EARNED!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF795548)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Score Breakdown
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreRow(
                            label = "Time Taken",
                            pts = "%d:%02d".format(uiState.timeElapsed / 60, uiState.timeElapsed % 60)
                        )

                        if (uiState.selectedMode == GameMode.TIME_ATTACK) {
                            val best = uiState.currentCategoryBestTime ?: uiState.timeElapsed
                            ScoreRow(
                                label = "Category PB",
                                pts = "%d:%02d".format(best / 60, best % 60),
                                isBonus = uiState.isNewPersonalBest
                            )
                        }

                        ScoreRow(
                            label = "Words Found (${uiState.foundWords.size})",
                            pts = "+${uiState.foundWords.size * 10} pts"
                        )

                        if (uiState.bonusWordsFound.isNotEmpty()) {
                            ScoreRow(
                                label = "Bonus Words (${uiState.bonusWordsFound.size})",
                                pts = "+${uiState.bonusWordsFound.size * 5} pts",
                                isBonus = true
                            )
                        }

                        if (uiState.selectedMode == GameMode.TIME_BLITZ && uiState.timeBonus > 0) {
                            ScoreRow(
                                label = "Time Bonus (${uiState.timeRemaining}s left)",
                                pts = "+${uiState.timeBonus} pts",
                                isBonus = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons: Next Level, Play Again, Home
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVictory && hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp))
                            .testTag("next_level_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ArafBlue),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NEXT LEVEL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(54.dp)
                        .shadow(2.dp, RoundedCornerShape(28.dp))
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVictory && hasNextLevel) Color.White else ArafBlue
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = if (isVictory && hasNextLevel) androidx.compose.foundation.BorderStroke(1.dp, ArafBlue) else null
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = if (isVictory && hasNextLevel) ArafBlue else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAY AGAIN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVictory && hasNextLevel) ArafBlue else Color.White
                        )
                    }
                }

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp)
                        .testTag("home_button"),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BACK TO HOME",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ArafDarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScoreRow(label: String, pts: String, isBonus: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isBonus) ArafOrange else Color(0xFF4B5563),
            fontWeight = if (isBonus) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = pts,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBonus) ArafOrange else ArafDarkText
        )
    }
}
