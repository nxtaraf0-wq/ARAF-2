package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArafBlue
import com.example.ui.theme.ArafDarkText
import com.example.ui.theme.ArafOrange

@Composable
fun CoinChip(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("coin_balance_chip"),
        color = Color(0xFFFFF8E7),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "Coins",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$coins",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF795548)
            )
        }
    }
}

@Composable
fun SoundToggleButton(
    soundEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E4E9), CircleShape)
            .testTag("sound_toggle_button")
    ) {
        Icon(
            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = if (soundEnabled) "Mute sound" else "Unmute sound",
            tint = if (soundEnabled) ArafBlue else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun BackIconButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onBack,
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E4E9), CircleShape)
            .testTag("back_button")
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            tint = ArafDarkText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun GameToastBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
