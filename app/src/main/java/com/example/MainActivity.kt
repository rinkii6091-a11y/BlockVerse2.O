package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AdventureMapScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopThemesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Preload Google AdMob in background
        com.example.ads.AdMobManager.initialize(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E1A)
                ) {
                    BlockFlowApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BlockFlowApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeTheme by viewModel.activeTheme.collectAsState()

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                userProfile = userProfile,
                theme = activeTheme,
                onStartGame = { mode -> viewModel.startNewGame(mode) },
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
        AppScreen.GAME -> {
            GameScreen(viewModel = viewModel)
        }
        AppScreen.SHOP -> {
            ShopThemesScreen(viewModel = viewModel)
        }
        AppScreen.ACHIEVEMENTS -> {
            AchievementsScreen(viewModel = viewModel)
        }
        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(viewModel = viewModel)
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(viewModel = viewModel)
        }
        AppScreen.DAILY_CHALLENGE -> {
            DailyChallengeScreen(viewModel = viewModel)
        }
        AppScreen.ADVENTURE_MAP -> {
            AdventureMapScreen(viewModel = viewModel)
        }
    }
}
