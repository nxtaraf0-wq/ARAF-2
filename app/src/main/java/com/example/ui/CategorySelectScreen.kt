package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.GameCategories
import com.example.ui.theme.ArafBackground
import com.example.ui.theme.ArafBlue
import com.example.ui.theme.ArafDarkText
import com.example.ui.theme.ArafOrange
import com.example.viewmodel.GameUiState

@Composable
fun CategorySelectScreen(
    uiState: GameUiState,
    onCategorySelected: (Category) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArafBackground)
            .padding(top = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackIconButton(onBack = onBack)
            Text(
                text = "Choose Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ArafDarkText
            )
            CoinChip(coins = uiState.coins)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a level to play (${uiState.completedLevels.size}/15 unlocked)",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        // 15 Categories Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(GameCategories.ALL) { category ->
                val isUnlocked = isCategoryUnlocked(category.index, uiState.completedLevels)
                val isCompleted = uiState.completedLevels.contains(category.id)
                val bestTime = uiState.categoryBestTimes[category.id]

                CategoryCard(
                    category = category,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    bestTime = bestTime,
                    onClick = {
                        if (isUnlocked) {
                            onCategorySelected(category)
                        }
                    }
                )
            }
        }
    }
}

private fun isCategoryUnlocked(index: Int, completedLevels: Set<String>): Boolean {
    // Level 0 (Animals) is always unlocked
    if (index == 0) return true
    val prevCategory = GameCategories.ALL.getOrNull(index - 1) ?: return false
    return completedLevels.contains(prevCategory.id)
}

@Composable
private fun CategoryCard(
    category: Category,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    bestTime: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFE2E4E9),
                RoundedCornerShape(18.dp)
            )
            .shadow(if (isUnlocked) 3.dp else 0.dp, RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("category_card_${category.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color(0xFFE9EBEF)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isUnlocked) 1f else 0.5f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Emoji bubble
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = if (isUnlocked) Color(0xFFF0F5FF) else Color(0xFFDFE1E6)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = category.emoji, fontSize = 22.sp)
                        }
                    }

                    if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = category.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) ArafDarkText else Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${category.words.size} words",
                            fontSize = 11.sp,
                            color = if (isUnlocked) Color(0xFF6B7280) else Color.LightGray
                        )
                        if (bestTime != null && isUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF3E0),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Text(
                                    text = "⚡ %d:%02d".format(bestTime / 60, bestTime % 60),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
