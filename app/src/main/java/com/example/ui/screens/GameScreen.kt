package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdventureLevelsCatalog
import com.example.model.GameMode
import com.example.ui.components.BlockPieceView
import com.example.ui.components.BoardView
import com.example.ui.components.FloatingScoreOverlay
import com.example.ui.components.FlowMeterBar
import com.example.ui.components.GameOverDialog
import com.example.ui.components.LevelObjectivesHud
import com.example.ui.components.LevelVictoryDialog
import com.example.ui.components.ParticleOverlay
import com.example.ui.components.PauseDialog
import com.example.ui.components.PieceTray
import com.example.ui.components.PowerUpBar
import com.example.ui.components.ShareCardDialog
import com.example.ui.components.TutorialOverlay
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val board by viewModel.board.collectAsState()
    val availablePieces by viewModel.availablePieces.collectAsState()
    val draggingPieceIndex by viewModel.draggingPieceIndex.collectAsState()
    val dragOffset by viewModel.dragOffset.collectAsState()
    val hoverPosition by viewModel.hoverGridPosition.collectAsState()
    val isPlacementValid by viewModel.isPlacementValid.collectAsState()
    val score by viewModel.score.collectAsState()
    val linesClearedThisRound by viewModel.linesClearedThisRound.collectAsState()
    val combo by viewModel.combo.collectAsState()
    val comboBannerText by viewModel.comboBannerText.collectAsState()
    val flowMeter by viewModel.flowMeter.collectAsState()
    val momentum by viewModel.momentum.collectAsState()
    val floatingScores by viewModel.floatingScores.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val isNewRecord by viewModel.isNewRecord.collectAsState()
    val activePowerUp by viewModel.activePowerUp.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val adventureLevel by viewModel.currentAdventureLevel.collectAsState()
    val maxComboInRound by viewModel.maxComboInRound.collectAsState()
    val levelStarsEarned by viewModel.levelStarsEarned.collectAsState()
    val levelCoinsEarned by viewModel.levelCoinsEarned.collectAsState()
    val levelGemsEarned by viewModel.levelGemsEarned.collectAsState()
    val isLevelCleared by viewModel.isLevelCleared.collectAsState()
    val draggingPiece = draggingPieceIndex?.let { availablePieces.getOrNull(it) }

    var isShareOpen by remember { mutableStateOf(false) }

    // Intercept Back Press during game
    BackHandler {
        if (isGameOver) {
            viewModel.navigateTo(AppScreen.HOME)
        } else {
            viewModel.pauseGame()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode / Level Badge
                Column {
                    val modeTitle = when (gameMode) {
                        GameMode.CLASSIC -> "CLASSIC"
                        GameMode.ADVENTURE -> "LEVEL $adventureLevel"
                        GameMode.DAILY -> "DAILY QUEST"
                        GameMode.TIME_RUSH -> "TIME RUSH"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x331E293B))
                            .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = modeTitle,
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Current Score & High Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "BEST ${userProfile.highScore}",
                        color = Color(0x88FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x331E293B))
                        .testTag("game_pause_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            // Flow Meter Bar
            FlowMeterBar(
                flowProgress = flowMeter,
                momentum = momentum,
                combo = combo,
                theme = theme
            )

            // Adventure Level Objectives HUD
            if (gameMode == GameMode.ADVENTURE) {
                LevelObjectivesHud(
                    level = AdventureLevelsCatalog.getLevel(adventureLevel),
                    currentScore = score,
                    currentLines = linesClearedThisRound,
                    currentMaxCombo = maxComboInRound,
                    theme = theme
                )
            }

            // Combo Banner Callout (Animated)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = comboBannerText != null,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    comboBannerText?.let { text ->
                        Text(
                            text = text,
                            color = Color(0xFFFFD700),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Center 8x8 Board
            BoardView(
                board = board,
                theme = theme,
                draggingPiece = draggingPiece,
                hoverPosition = hoverPosition,
                isPlacementValid = isPlacementValid,
                activePowerUp = activePowerUp,
                onCellClicked = { r, c -> viewModel.onCellClicked(r, c) },
                onBoundsChanged = { bounds, cellSize ->
                    viewModel.boardScreenBounds = bounds
                    viewModel.cellSizePx = cellSize
                }
            )

            // Power-Up Bar
            PowerUpBar(
                userProfile = userProfile,
                activePowerUp = activePowerUp,
                theme = theme,
                onPowerUpSelected = { type -> viewModel.selectPowerUp(type) }
            )

            // Bottom 3 Piece Tray
            PieceTray(
                availablePieces = availablePieces,
                theme = theme,
                activeDraggingIndex = draggingPieceIndex,
                onDragStart = { idx, touchPos -> viewModel.onDragStart(idx, touchPos) },
                onDrag = { delta -> viewModel.onDrag(delta) },
                onDragEnd = { viewModel.onDragEnd() },
                onDragCancel = { viewModel.onDragCancel() }
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Floating Dragged Piece Preview (follows finger)
        if (draggingPieceIndex != null && draggingPiece != null) {
            val piece = draggingPiece
            val density = LocalDensity.current
            val cellSizePx = viewModel.cellSizePx.coerceAtLeast(60f)
            val visualYOffset = cellSizePx * 1.5f

            val pieceWidthPx = piece.cols * cellSizePx
            val pieceHeightPx = piece.rows * cellSizePx

            val posX = (dragOffset.x - pieceWidthPx / 2f).toInt()
            val posY = (dragOffset.y - visualYOffset - pieceHeightPx / 2f).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(posX, posY) }
                    .scale(1.08f)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), spotColor = theme.accentColor)
            ) {
                val cellSizeDp = with(density) { cellSizePx.toDp() }
                BlockPieceView(
                    piece = piece,
                    theme = theme,
                    cellSize = cellSizeDp,
                    alpha = if (isPlacementValid) 1.0f else 0.85f
                )
            }
        }

        // Particle System Overlay (Burst & Sparks)
        ParticleOverlay(particles = particles)

        // Floating Score Overlay (+100, etc.)
        FloatingScoreOverlay(floatingScores = floatingScores)

        // Pause Dialog
        PauseDialog(
            isOpen = isPaused,
            theme = theme,
            isSfxOn = viewModel.soundManager.isSfxEnabled,
            isMusicOn = viewModel.soundManager.isMusicEnabled,
            onResume = { viewModel.resumeGame() },
            onRestart = { viewModel.startNewGame(gameMode, adventureLevel) },
            onHome = { viewModel.navigateTo(AppScreen.HOME) },
            onSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
            onToggleSfx = { viewModel.toggleSfx() },
            onToggleMusic = { viewModel.toggleMusic() }
        )

        // Level Victory Dialog
        LevelVictoryDialog(
            isOpen = isLevelCleared,
            level = AdventureLevelsCatalog.getLevel(adventureLevel),
            starsEarned = levelStarsEarned,
            score = score,
            coinsEarned = levelCoinsEarned,
            gemsEarned = levelGemsEarned,
            hasNextLevel = adventureLevel < 25,
            theme = theme,
            onNextLevel = { viewModel.startNextAdventureLevel() },
            onReplay = { viewModel.startNewGame(GameMode.ADVENTURE, adventureLevel) },
            onMap = { viewModel.navigateTo(AppScreen.ADVENTURE_MAP) }
        )

        // Game Over Dialog
        GameOverDialog(
            isOpen = isGameOver,
            score = score,
            bestScore = userProfile.highScore,
            linesCleared = linesClearedThisRound,
            combo = combo,
            isNewRecord = isNewRecord,
            coinsEarned = (score / 40) + 15,
            theme = theme,
            onReplay = { viewModel.startNewGame(gameMode, adventureLevel) },
            onHome = { viewModel.navigateTo(AppScreen.HOME) },
            onShare = { isShareOpen = true }
        )

        // Share Card Dialog
        ShareCardDialog(
            isOpen = isShareOpen,
            score = score,
            combo = combo,
            lines = linesClearedThisRound,
            theme = theme,
            onDismiss = { isShareOpen = false }
        )

        // Quick First-Launch Tutorial
        TutorialOverlay(
            isOpen = showTutorial,
            theme = theme,
            onGotIt = { viewModel.dismissTutorial() }
        )
    }
}
