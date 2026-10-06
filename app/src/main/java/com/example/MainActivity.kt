package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CategorySelectScreen
import com.example.ui.GameScreen
import com.example.ui.HomeScreen
import com.example.ui.ModeSelectScreen
import com.example.ui.ResultsScreen
import com.example.ui.theme.ArafTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArafTheme {
                val viewModel: GameViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (uiState.currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onPlayClicked = { viewModel.navigateTo(Screen.CATEGORY_SELECT) },
                                onSoundToggled = { viewModel.toggleSound() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.CATEGORY_SELECT -> {
                            CategorySelectScreen(
                                uiState = uiState,
                                onCategorySelected = { category ->
                                    viewModel.selectCategory(category)
                                },
                                onBack = { viewModel.navigateTo(Screen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.MODE_SELECT -> {
                            ModeSelectScreen(
                                uiState = uiState,
                                onStartGame = { mode ->
                                    viewModel.startGame(mode)
                                },
                                onBack = { viewModel.navigateTo(Screen.CATEGORY_SELECT) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.GAME -> {
                            GameScreen(
                                uiState = uiState,
                                onBack = { viewModel.navigateTo(Screen.CATEGORY_SELECT) },
                                onDragStart = { pos -> viewModel.onDragStart(pos) },
                                onDragMove = { pos -> viewModel.onDragMove(pos) },
                                onDragEnd = { viewModel.onDragEnd() },
                                onUseHint = { viewModel.useHint() },
                                onWatchAdForHints = { act -> viewModel.watchRewardedAdForHints(act) },
                                onToggleSound = { viewModel.toggleSound() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.RESULTS -> {
                            ResultsScreen(
                                uiState = uiState,
                                onPlayAgain = {
                                    viewModel.startGame(uiState.selectedMode)
                                },
                                onNextLevel = {
                                    viewModel.nextLevel()
                                },
                                onHome = {
                                    viewModel.navigateTo(Screen.HOME)
                                },
                                onTriggerInterstitial = { act ->
                                    viewModel.showInterstitialOnCompletion(act)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
