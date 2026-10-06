package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.ui.theme.ArafBackground
import com.example.ui.theme.ArafBlue
import com.example.ui.theme.ArafDarkText
import com.example.ui.theme.ArafOrange
import com.example.ui.theme.ArafPink
import com.example.viewmodel.GameUiState

@Composable
fun ModeSelectScreen(
    uiState: GameUiState,
    onStartGame: (GameMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArafBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackIconButton(onBack = onBack)
            Text(
                text = "Game Mode",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ArafDarkText
            )
            CoinChip(coins = uiState.coins)
        }

        // Category Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = uiState.selectedCategory.emoji, fontSize = 38.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = uiState.selectedCategory.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ArafDarkText
            )

            Text(
                text = "${uiState.selectedCategory.words.size} Hidden Words to discover",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Mode Options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val pbTime = uiState.currentCategoryBestTime
            val pbLabel = if (pbTime != null) "🏆 Personal Best: %d:%02d".format(pbTime / 60, pbTime % 60) else "⚡ No PB record yet — set one now!"

            ModeCard(
                title = "Time Attack",
                subtitle = "Speedrun • Records your fastest time",
                description = "Complete the puzzle as quickly as possible. Saves your personal best for ${uiState.selectedCategory.name}.\n$pbLabel",
                icon = Icons.Default.Timer,
                iconColor = ArafOrange,
                isSelected = selectedMode == GameMode.TIME_ATTACK,
                onSelect = { selectedMode = GameMode.TIME_ATTACK }
            )

            ModeCard(
                title = "Classic Mode",
                subtitle = "Casual & Relaxed • Untimed",
                description = "No timer pressure. Explore and find every word, search for bonus words, and enjoy the puzzle.",
                icon = Icons.Default.Spa,
                iconColor = Color(0xFF4CAF50),
                isSelected = selectedMode == GameMode.CLASSIC,
                onSelect = { selectedMode = GameMode.CLASSIC }
            )

            ModeCard(
                title = "Countdown Blitz",
                subtitle = "2-Minute Countdown • Timer counts down",
                description = "Race against the clock! Red warning when <30s remaining. Earn +timeLeft × 2 bonus points!",
                icon = Icons.Default.HourglassBottom,
                iconColor = ArafPink,
                isSelected = selectedMode == GameMode.TIME_BLITZ,
                onSelect = { selectedMode = GameMode.TIME_BLITZ }
            )
        }

        // Start Button
        Button(
            onClick = { onStartGame(selectedMode) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp)
                .shadow(6.dp, RoundedCornerShape(30.dp))
                .testTag("start_puzzle_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ArafBlue),
            shape = RoundedCornerShape(30.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START PUZZLE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                2.dp,
                if (isSelected) ArafBlue else Color(0xFFE2E4E9),
                RoundedCornerShape(20.dp)
            )
            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .testTag("mode_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF3F7FF) else Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArafDarkText
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) ArafBlue else Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF71717A),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Unselected",
                tint = if (isSelected) ArafBlue else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
