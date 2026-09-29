package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.AchievementEntity
import com.example.data.AppDatabase
import com.example.data.DailyChallengeEntity
import com.example.data.GameRepository
import com.example.data.LevelProgressEntity
import com.example.data.UserProfileEntity
import com.example.haptics.HapticsManager
import com.example.logic.GameInputHandler
import com.example.logic.GameLogic
import com.example.logic.InputHandlerState
import com.example.logic.PlacementResult
import com.example.model.AdventureLevel
import com.example.model.AdventureLevelsCatalog
import com.example.model.BlockPiece
import com.example.model.CellState
import com.example.model.CollectibleType
import com.example.model.FloatingScore
import com.example.model.GameMode
import com.example.model.MomentumLevel
import com.example.model.Particle
import com.example.model.ParticleType
import com.example.model.PieceTemplates
import com.example.model.PowerUpType
import com.example.model.ScoreHeaderState
import com.example.model.SpecialType
import com.example.theme.GameThemes
import com.example.theme.ThemeId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class AppScreen {
    HOME,
    GAME,
    SHOP,
    ACHIEVEMENTS,
    LEADERBOARD,
    SETTINGS,
    DAILY_CHALLENGE,
    ADVENTURE_MAP
}

data class BoardSnapshot(
    val board: List<List<CellState>>,
    val pieces: List<BlockPiece?>,
    val score: Int,
    val combo: Int,
    val flowMeter: Float
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val gameLogic: GameLogic = GameLogic.instance
    val inputHandler: GameInputHandler = GameInputHandler(gameLogic)
    val inputHandlerState: StateFlow<InputHandlerState> = inputHandler.state
    private val repository: GameRepository
    val soundManager = SoundManager()
    val hapticsManager = HapticsManager(application.applicationContext)

    init {
        val db = AppDatabase.getInstance(application)
        repository = GameRepository(db.gameDao())
        viewModelScope.launch {
            repository.initDefaultAchievementsIfNeeded()
            repository.initDailyChallengeIfNeeded()
            loadUserProfile()
            startParticlesLoop()
        }
        viewModelScope.launch {
            repository.allLevelProgress.collect { list ->
                _allLevelProgress.value = list.associateBy { it.levelNumber }
            }
        }
    }

    // App Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Game Mode
    private val _gameMode = MutableStateFlow(GameMode.CLASSIC)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _currentAdventureLevel = MutableStateFlow(1)
    val currentAdventureLevel: StateFlow<Int> = _currentAdventureLevel.asStateFlow()

    private val _allLevelProgress = MutableStateFlow<Map<Int, LevelProgressEntity>>(emptyMap())
    val allLevelProgress: StateFlow<Map<Int, LevelProgressEntity>> = _allLevelProgress.asStateFlow()

    private val _maxComboInRound = MutableStateFlow(0)
    val maxComboInRound: StateFlow<Int> = _maxComboInRound.asStateFlow()

    private val _levelStarsEarned = MutableStateFlow(1)
    val levelStarsEarned: StateFlow<Int> = _levelStarsEarned.asStateFlow()

    private val _levelCoinsEarned = MutableStateFlow(100)
    val levelCoinsEarned: StateFlow<Int> = _levelCoinsEarned.asStateFlow()

    private val _levelGemsEarned = MutableStateFlow(1)
    val levelGemsEarned: StateFlow<Int> = _levelGemsEarned.asStateFlow()

    // User Profile & Stats from Room
    private val _userProfile = MutableStateFlow(UserProfileEntity())
    val userProfile: StateFlow<UserProfileEntity> = _userProfile.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements.asStateFlow()

    private val _dailyChallenge = MutableStateFlow<DailyChallengeEntity?>(null)
    val dailyChallenge: StateFlow<DailyChallengeEntity?> = _dailyChallenge.asStateFlow()

    // Active Theme
    private val _activeTheme = MutableStateFlow(GameThemes.NeonCity)
    val activeTheme = _activeTheme.asStateFlow()

    // Board: 8x8
    private val _board = MutableStateFlow(createEmptyBoard())
    val board: StateFlow<List<List<CellState>>> = _board.asStateFlow()

    // 3 Available Pieces
    private val _availablePieces = MutableStateFlow<List<BlockPiece?>>(listOf(null, null, null))
    val availablePieces: StateFlow<List<BlockPiece?>> = _availablePieces.asStateFlow()

    // Drag & Placement State
    private val _draggingPieceIndex = MutableStateFlow<Int?>(null)
    val draggingPieceIndex: StateFlow<Int?> = _draggingPieceIndex.asStateFlow()

    private val _dragOffset = MutableStateFlow(Offset.Zero)
    val dragOffset: StateFlow<Offset> = _dragOffset.asStateFlow()

    private val _hoverGridPosition = MutableStateFlow<Pair<Int, Int>?>(null)
    val hoverGridPosition: StateFlow<Pair<Int, Int>?> = _hoverGridPosition.asStateFlow()

    private val _isPlacementValid = MutableStateFlow(false)
    val isPlacementValid: StateFlow<Boolean> = _isPlacementValid.asStateFlow()

    // Game Metrics
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _linesClearedThisRound = MutableStateFlow(0)
    val linesClearedThisRound: StateFlow<Int> = _linesClearedThisRound.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _comboBannerText = MutableStateFlow<String?>(null)
    val comboBannerText: StateFlow<String?> = _comboBannerText.asStateFlow()

    private val _flowMeter = MutableStateFlow(0f) // 0.0 to 1.0
    val flowMeter: StateFlow<Float> = _flowMeter.asStateFlow()

    private val _isFlowModeActive = MutableStateFlow(false)
    val isFlowModeActive: StateFlow<Boolean> = _isFlowModeActive.asStateFlow()

    private val _momentum = MutableStateFlow(MomentumLevel.CALM)
    val momentum: StateFlow<MomentumLevel> = _momentum.asStateFlow()

    // Overlays & VFX
    private val _floatingScores = MutableStateFlow<List<FloatingScore>>(emptyList())
    val floatingScores: StateFlow<List<FloatingScore>> = _floatingScores.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    // Dialogs / States
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    /**
     * ViewModel-backed state stream driving the Game Score Header UI component.
     * Reactively emits updated current score, high score, new record indicators,
     * combo counts, and multipliers.
     */
    val scoreHeaderState: StateFlow<ScoreHeaderState> = combine(
        _score,
        _userProfile,
        _isNewRecord,
        _combo,
        _momentum
    ) { score, profile, isNewRecord, combo, momentum ->
        ScoreHeaderState(
            currentScore = score,
            bestScore = maxOf(profile.highScore, score),
            isNewRecord = isNewRecord || (profile.highScore > 0 && score > profile.highScore),
            combo = combo,
            multiplier = momentum.multiplier,
            momentum = momentum,
            gameMode = _gameMode.value,
            adventureLevel = _currentAdventureLevel.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ScoreHeaderState()
    )

    fun addPoints(points: Int, label: String = "POINTS") {
        addScore(points, label)
    }

    private val _isLevelCleared = MutableStateFlow(false)
    val isLevelCleared: StateFlow<Boolean> = _isLevelCleared.asStateFlow()

    private val _activePowerUp = MutableStateFlow<PowerUpType?>(null)
    val activePowerUp: StateFlow<PowerUpType?> = _activePowerUp.asStateFlow()

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    // Undo History
    private var lastSnapshot: BoardSnapshot? = null

    // Touch/Board Measurement
    var boardScreenBounds = androidx.compose.ui.geometry.Rect.Zero
    var cellSizePx = 0f

    private var comboDecayJob: Job? = null

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = repository.checkDailyStreak()
            _userProfile.value = profile
            soundManager.isSfxEnabled = profile.soundEnabled
            soundManager.isMusicEnabled = profile.musicEnabled
            hapticsManager.isHapticsEnabled = profile.hapticsEnabled
            _activeTheme.value = GameThemes.getById(
                try { ThemeId.valueOf(profile.activeThemeId) } catch (_: Exception) { ThemeId.NEON_CITY }
            )
            _showTutorial.value = !profile.isTutorialCompleted

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.getDailyChallenge(today).collect { dc ->
                _dailyChallenge.value = dc
            }
        }
        viewModelScope.launch {
            repository.achievements.collect { list ->
                _achievements.value = list
            }
        }
    }

    private fun createEmptyBoard(): List<List<CellState>> {
        return gameLogic.createEmptyBoard()
    }

    // ==========================================
    // GAME START & RESET
    // ==========================================

    fun startNewGame(mode: GameMode = GameMode.CLASSIC, levelNum: Int = 1) {
        _gameMode.value = mode
        _currentAdventureLevel.value = levelNum
        _score.value = 0
        _linesClearedThisRound.value = 0
        _combo.value = 0
        _maxComboInRound.value = 0
        _flowMeter.value = 0f
        _momentum.value = MomentumLevel.CALM
        _isFlowModeActive.value = false
        _isGameOver.value = false
        _isNewRecord.value = false
        _isLevelCleared.value = false
        _isPaused.value = false
        _activePowerUp.value = null
        lastSnapshot = null

        // Initial board state based on mode
        val newBoard = List(8) { MutableList(8) { CellState() } }

        if (mode == GameMode.ADVENTURE) {
            val lvl = AdventureLevelsCatalog.getLevel(levelNum)
            when (lvl.obstaclePattern) {
                "CORNERS" -> {
                    listOf(0 to 0, 0 to 7, 7 to 0, 7 to 7).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.NONE, colorIndex = 5)
                    }
                }
                "QUADRANTS" -> {
                    listOf(2 to 2, 2 to 5, 5 to 2, 5 to 5).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.NONE, colorIndex = 4)
                    }
                }
                "CENTER_ICE" -> {
                    listOf(3 to 3, 3 to 4, 4 to 3, 4 to 4).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.ICE, iceHitsLeft = 2, colorIndex = 0)
                    }
                }
                "CROSS_ICE" -> {
                    listOf(2 to 3, 3 to 2, 4 to 5, 5 to 4, 3 to 3, 4 to 4).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.ICE, iceHitsLeft = 2, colorIndex = 0)
                    }
                }
                "PERIMETER" -> {
                    listOf(0 to 3, 0 to 4, 7 to 3, 7 to 4, 3 to 0, 4 to 0, 3 to 7, 4 to 7).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.NONE, colorIndex = 6)
                    }
                }
                "STAGGERED_ICE" -> {
                    listOf(1 to 2, 1 to 5, 2 to 1, 2 to 6, 5 to 1, 5 to 6, 6 to 2, 6 to 5).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.ICE, iceHitsLeft = 2, colorIndex = 0)
                    }
                }
                "DIAMOND_ICE" -> {
                    listOf(1 to 3, 1 to 4, 3 to 1, 4 to 1, 3 to 6, 4 to 6, 6 to 3, 6 to 4).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.ICE, iceHitsLeft = 2, colorIndex = 0)
                    }
                    listOf(3 to 3, 4 to 4).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.NONE, colorIndex = 3)
                    }
                }
                "DENSE" -> {
                    listOf(1 to 1, 1 to 6, 6 to 1, 6 to 6, 3 to 3, 3 to 4, 4 to 3, 4 to 4).forEach { (r, c) ->
                        newBoard[r][c] = CellState(isFilled = true, specialType = SpecialType.NONE, colorIndex = 2)
                    }
                }
            }
        }
        _board.value = newBoard

        dealNewTrio()
        navigateTo(AppScreen.GAME)
    }

    fun resumeGame() {
        _isPaused.value = false
    }

    fun pauseGame() {
        _isPaused.value = true
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun dismissTutorial() {
        _showTutorial.value = false
        viewModelScope.launch {
            val updated = _userProfile.value.copy(isTutorialCompleted = true)
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    private fun dealNewTrio() {
        val trio = gameLogic.generatePieceTrio(_board.value, _userProfile.value.currentLevel)
        _availablePieces.value = trio
        checkGameOverCondition()
    }

    // ==========================================
    // INPUT HANDLING (DRAG-AND-DROP & TAP-TO-PLACE)
    // ==========================================

    fun onDragStart(pieceIndex: Int, initialTouchOffset: Offset) {
        val piece = _availablePieces.value.getOrNull(pieceIndex) ?: return
        soundManager.playPickup()
        val newState = inputHandler.onDragStart(
            pieceIndex = pieceIndex,
            piece = piece,
            screenTouch = initialTouchOffset,
            boardBounds = boardScreenBounds,
            cellSizePx = cellSizePx,
            board = _board.value
        )
        _draggingPieceIndex.value = newState.activePieceIndex
        _dragOffset.value = newState.dragPosition
        _hoverGridPosition.value = newState.hoverPosition
        _isPlacementValid.value = newState.isPlacementValid
    }

    fun onDrag(dragDelta: Offset) {
        val newState = inputHandler.onDrag(
            dragDelta = dragDelta,
            boardBounds = boardScreenBounds,
            cellSizePx = cellSizePx,
            board = _board.value
        )
        _dragOffset.value = newState.dragPosition
        _hoverGridPosition.value = newState.hoverPosition
        _isPlacementValid.value = newState.isPlacementValid
    }

    fun onDragEnd() {
        when (val result = inputHandler.onDragEnd(_board.value)) {
            is PlacementResult.Success -> {
                placePiece(result.pieceIndex, result.piece, result.targetRow, result.targetCol)
            }
            is PlacementResult.Invalid -> {
                soundManager.playInvalid()
                hapticsManager.vibrateInvalid()
            }
            else -> {}
        }

        _draggingPieceIndex.value = null
        _hoverGridPosition.value = null
        _isPlacementValid.value = false
    }

    fun onDragCancel() {
        inputHandler.onDragCancel()
        _draggingPieceIndex.value = null
        _hoverGridPosition.value = null
        _isPlacementValid.value = false
    }

    fun onPieceTrayTapped(pieceIndex: Int) {
        val piece = _availablePieces.value.getOrNull(pieceIndex) ?: return
        soundManager.playPickup()
        val newState = inputHandler.onPieceTrayTapped(pieceIndex, piece)
        _draggingPieceIndex.value = if (newState.isTapSelected) pieceIndex else null
    }

    // ==========================================
    // PLACEMENT & LINE CLEARING
    // ==========================================

    private fun placePiece(index: Int, piece: BlockPiece, startR: Int, startC: Int) {
        // Save snapshot for Undo
        lastSnapshot = BoardSnapshot(
            board = _board.value,
            pieces = _availablePieces.value,
            score = _score.value,
            combo = _combo.value,
            flowMeter = _flowMeter.value
        )

        // 1. Stamp piece onto board using GameLogic
        val currentBoard = gameLogic.stampPiece(_board.value, piece, startR, startC)

        // Mark piece consumed
        val updatedPieces = _availablePieces.value.toMutableList()
        updatedPieces[index] = null
        _availablePieces.value = updatedPieces

        soundManager.playSnap()
        hapticsManager.vibrateSnap()

        // Points for placing using GameLogic
        val placePoints = gameLogic.calculatePlacementScore(piece, _momentum.value.multiplier)
        addScore(placePoints, "PLACED")

        // 2. Detect lines to clear using GameLogic
        val fullRows = gameLogic.detectFullRows(currentBoard)
        val fullCols = gameLogic.detectFullCols(currentBoard)
        val linesCleared = fullRows.size + fullCols.size

        if (linesCleared > 0) {
            processLineClears(currentBoard, fullRows, fullCols)
        } else {
            _board.value = currentBoard
            handleNoLineClear()
        }

        // 3. If all pieces placed, generate new trio
        if (_availablePieces.value.all { it == null }) {
            dealNewTrio()
        } else {
            checkGameOverCondition()
        }
    }

    private fun processLineClears(
        boardState: List<List<CellState>>,
        fullRows: List<Int>,
        fullCols: List<Int>
    ) {
        val totalLines = fullRows.size + fullCols.size
        _linesClearedThisRound.value += totalLines

        // Combo increment
        val currentCombo = _combo.value + 1
        _combo.value = currentCombo

        // Calculate score & combo banner through GameLogic
        val scoreBreakdown = gameLogic.calculateLineClearScore(
            linesCleared = totalLines,
            combo = currentCombo,
            momentumMultiplier = _momentum.value.multiplier
        )
        val banner = scoreBreakdown.bannerText
        _comboBannerText.value = banner
        viewModelScope.launch {
            delay(1200)
            if (_comboBannerText.value == banner) {
                _comboBannerText.value = null
            }
        }

        // Audio & Haptics
        soundManager.playLineClear(totalLines, currentCombo)
        if (currentCombo >= 2) soundManager.playCombo(currentCombo)
        hapticsManager.vibrateLineClear()
        if (currentCombo >= 3) hapticsManager.vibrateCombo()

        addScore(scoreBreakdown.totalScoreEarned, banner)

        // Increase Flow Meter
        val flowGain = 0.20f * totalLines + (currentCombo * 0.05f)
        val newFlow = (_flowMeter.value + flowGain).coerceIn(0f, 1f)
        _flowMeter.value = newFlow

        // Update Momentum
        _momentum.value = when {
            newFlow >= 0.85f -> MomentumLevel.OVERDRIVE
            newFlow >= 0.55f -> MomentumLevel.HYPE
            newFlow >= 0.25f -> MomentumLevel.FLOW
            else -> MomentumLevel.CALM
        }

        // Process line clear and special block reactions through GameLogic
        val clearResult = gameLogic.processLineClears(boardState, fullRows, fullCols)

        // Trigger special effects sounds
        for (effect in clearResult.specialEffectsTriggered) {
            when (effect.specialType) {
                SpecialType.BOMB -> {
                    soundManager.playBomb()
                    hapticsManager.vibrateSpecial()
                }
                else -> {}
            }
        }

        // Collect gathered items
        for (item in clearResult.collectiblesGathered) {
            collectItem(item.collectibleType, item.row, item.col)
        }

        // Trigger high-impact Neon Line Clear particle effects for each cleared row & column
        for (r in fullRows) {
            spawnRowClearNeonEffect(r, isRow = true)
        }
        for (c in fullCols) {
            spawnRowClearNeonEffect(c, isRow = false)
        }

        // Spawn Burst Particles at remaining cleared cells
        val palette = _activeTheme.value.particleColors
        for ((r, c) in clearResult.clearedCells) {
            val cx = boardScreenBounds.left + (c + 0.5f) * cellSizePx
            val cy = boardScreenBounds.top + (r + 0.5f) * cellSizePx
            spawnParticleBurst(cx, cy, palette, count = 4)
        }

        // Apply clears to board
        _board.value = clearResult.updatedBoard

        // Update round combo record
        _maxComboInRound.value = maxOf(_maxComboInRound.value, currentCombo)

        // Check Adventure Level condition
        if (_gameMode.value == GameMode.ADVENTURE && !_isLevelCleared.value) {
            val lvl = AdventureLevelsCatalog.getLevel(_currentAdventureLevel.value)
            val meetsLines = _linesClearedThisRound.value >= lvl.targetLines
            val meetsScore = _score.value >= lvl.targetScore
            val meetsCombo = lvl.targetCombos == 0 || _maxComboInRound.value >= lvl.targetCombos
            if (meetsLines && meetsScore && meetsCombo) {
                triggerLevelVictory(lvl)
            }
        }

        // Reset combo decay timer
        resetComboTimer()
    }

    private fun handleNoLineClear() {
        // Slow combo decay
        resetComboTimer()
        _flowMeter.value = (_flowMeter.value - 0.05f).coerceAtLeast(0f)
        if (_flowMeter.value < 0.25f && _momentum.value != MomentumLevel.CALM) {
            _momentum.value = MomentumLevel.CALM
        }
    }

    private fun resetComboTimer() {
        comboDecayJob?.cancel()
        comboDecayJob = viewModelScope.launch {
            delay(5000)
            _combo.value = 0
            _momentum.value = MomentumLevel.CALM
            _flowMeter.value = (_flowMeter.value - 0.2f).coerceAtLeast(0f)
        }
    }

    private fun collectItem(item: CollectibleType, r: Int, c: Int) {
        soundManager.playCollectible()
        val (coinAdd, gemAdd) = when (item) {
            CollectibleType.COIN -> 10 to 0
            CollectibleType.GEM -> 0 to 1
            CollectibleType.STAR -> 25 to 0
            CollectibleType.ENERGY_SHARD -> 15 to 1
            else -> 5 to 0
        }
        viewModelScope.launch {
            val current = _userProfile.value
            val updated = current.copy(
                coins = current.coins + coinAdd,
                gems = current.gems + gemAdd
            )
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    private fun addScore(points: Int, label: String) {
        val newScore = _score.value + points
        _score.value = newScore

        // Floating score popup in center
        val screenX = boardScreenBounds.center.x
        val screenY = boardScreenBounds.center.y
        _floatingScores.value = _floatingScores.value + FloatingScore(
            text = "+$points $label",
            startX = screenX,
            startY = screenY
        )

        // Check if High Score beaten
        if (newScore > _userProfile.value.highScore) {
            if (!_isNewRecord.value) {
                _isNewRecord.value = true
                soundManager.playLevelUp()
            }
        }
    }

    // ==========================================
    // GAME OVER & ACHIEVEMENTS
    // ==========================================

    private fun checkGameOverCondition() {
        val pieces = _availablePieces.value.filterNotNull()
        if (pieces.isEmpty()) return

        val canFitAny = gameLogic.canAnyPieceFit(_board.value, pieces)
        if (!canFitAny) {
            // Check if player has power-ups to save themselves (Shuffle or Hammer)
            triggerGameOver()
        }
    }

    private fun triggerGameOver() {
        _isGameOver.value = true
        soundManager.playGameOver()

        viewModelScope.launch {
            val current = _userProfile.value
            val isBest = _score.value > current.highScore
            val earnedCoins = (_score.value / 40) + 15
            val earnedGems = if (isBest) 5 else 1

            val updated = current.copy(
                highScore = maxOf(current.highScore, _score.value),
                coins = current.coins + earnedCoins,
                gems = current.gems + earnedGems,
                totalGamesPlayed = current.totalGamesPlayed + 1,
                totalLinesCleared = current.totalLinesCleared + _linesClearedThisRound.value,
                bestCombo = maxOf(current.bestCombo, _combo.value)
            )
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    private fun triggerLevelVictory(lvl: AdventureLevel) {
        val stars = when {
            _score.value >= lvl.starRequirements[2] -> 3
            _score.value >= lvl.starRequirements[1] -> 2
            else -> 1
        }
        _levelStarsEarned.value = stars
        val coins = 100 + (stars * 50) + (lvl.levelNumber * 20)
        val gems = stars + (if (lvl.levelNumber % 5 == 0) 5 else 0)
        _levelCoinsEarned.value = coins
        _levelGemsEarned.value = gems
        _isLevelCleared.value = true

        soundManager.playLevelUp()
        hapticsManager.vibrateCombo()

        viewModelScope.launch {
            repository.saveLevelProgress(lvl.levelNumber, stars, _score.value)
            val cur = _userProfile.value
            val nextLevel = maxOf(cur.currentLevel, minOf(25, lvl.levelNumber + 1))
            val updated = cur.copy(
                currentLevel = nextLevel,
                coins = cur.coins + coins,
                gems = cur.gems + gems,
                levelStars = cur.levelStars + stars
            )
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    fun startNextAdventureLevel() {
        val next = _currentAdventureLevel.value + 1
        if (next <= 25) {
            startNewGame(GameMode.ADVENTURE, next)
        } else {
            navigateTo(AppScreen.ADVENTURE_MAP)
        }
    }

    /**
     * Rewarded Ad Bonus: Clears the center 4x4 matrix and deals fresh pieces,
     * allowing the player to revive and continue playing without losing their combo!
     */
    fun reviveGame() {
        val currentBoard = _board.value.map { it.toMutableList() }
        for (r in 2..5) {
            for (c in 2..5) {
                currentBoard[r][c] = CellState()
            }
        }
        _board.value = currentBoard
        _isGameOver.value = false
        dealNewTrio()
        soundManager.playLevelUp()
        hapticsManager.vibrateCombo()
        spawnParticleBurst(boardScreenBounds.center.x, boardScreenBounds.center.y, _activeTheme.value.particleColors, count = 24)
    }

    /**
     * Rewarded Ad Bonus: Doubles the coins earned in the game session.
     */
    fun doubleGameOverCoins(bonusCoins: Int) {
        viewModelScope.launch {
            val cur = _userProfile.value
            val updated = cur.copy(coins = cur.coins + bonusCoins)
            _userProfile.value = updated
            repository.saveProfile(updated)
            soundManager.playCollectible()
        }
    }

    // ==========================================
    // POWER-UPS
    // ==========================================

    fun selectPowerUp(type: PowerUpType) {
        val profile = _userProfile.value
        val hasStock = when (type) {
            PowerUpType.HAMMER -> profile.hammerCount > 0
            PowerUpType.SHUFFLE -> profile.shuffleCount > 0
            PowerUpType.UNDO -> profile.undoCount > 0
            PowerUpType.BOMB -> profile.bombCount > 0
            PowerUpType.COLOR_BLAST -> profile.colorBlastCount > 0
        }

        if (!hasStock) {
            soundManager.playInvalid()
            return
        }

        soundManager.playPowerUp()

        when (type) {
            PowerUpType.SHUFFLE -> {
                // Instantly re-rolls pieces
                consumePowerUp(PowerUpType.SHUFFLE)
                dealNewTrio()
                spawnParticleBurst(boardScreenBounds.center.x, boardScreenBounds.bottom + 100f, _activeTheme.value.particleColors, 16)
            }
            PowerUpType.UNDO -> {
                if (lastSnapshot != null) {
                    consumePowerUp(PowerUpType.UNDO)
                    val snap = lastSnapshot!!
                    _board.value = snap.board
                    _availablePieces.value = snap.pieces
                    _score.value = snap.score
                    _combo.value = snap.combo
                    _flowMeter.value = snap.flowMeter
                    lastSnapshot = null
                } else {
                    soundManager.playInvalid()
                }
            }
            else -> {
                // Toggle targeting mode on board
                _activePowerUp.value = if (_activePowerUp.value == type) null else type
            }
        }
    }

    fun onCellClicked(row: Int, col: Int) {
        val active = _activePowerUp.value
        if (active != null) {
            handlePowerUpOnCell(active, row, col)
            return
        }

        // Tap-to-Place execution via GameInputHandler
        when (val result = inputHandler.onBoardCellTapped(row, col, _board.value)) {
            is PlacementResult.Success -> {
                placePiece(result.pieceIndex, result.piece, result.targetRow, result.targetCol)
                _draggingPieceIndex.value = null
            }
            is PlacementResult.Invalid -> {
                soundManager.playInvalid()
                hapticsManager.vibrateInvalid()
            }
            PlacementResult.None -> {
                // No piece selected for tap-to-place
            }
            else -> {}
        }
    }

    private fun handlePowerUpOnCell(active: PowerUpType, row: Int, col: Int) {
        val currentBoard = _board.value.map { it.toMutableList() }

        when (active) {
            PowerUpType.HAMMER -> {
                if (currentBoard[row][col].isFilled) {
                    consumePowerUp(PowerUpType.HAMMER)
                    currentBoard[row][col] = CellState()
                    _board.value = currentBoard
                    soundManager.playSnap()
                    hapticsManager.vibrateSnap()
                    val cx = boardScreenBounds.left + (col + 0.5f) * cellSizePx
                    val cy = boardScreenBounds.top + (row + 0.5f) * cellSizePx
                    spawnParticleBurst(cx, cy, _activeTheme.value.particleColors, 14)
                }
            }
            PowerUpType.BOMB -> {
                consumePowerUp(PowerUpType.BOMB)
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = row + dr
                        val nc = col + dc
                        if (nr in 0..7 && nc in 0..7) {
                            currentBoard[nr][nc] = CellState()
                        }
                    }
                }
                _board.value = currentBoard
                soundManager.playBomb()
                hapticsManager.vibrateSpecial()
                val cx = boardScreenBounds.left + (col + 0.5f) * cellSizePx
                val cy = boardScreenBounds.top + (row + 0.5f) * cellSizePx
                spawnParticleBurst(cx, cy, _activeTheme.value.particleColors, 28)
            }
            PowerUpType.COLOR_BLAST -> {
                val targetColor = currentBoard[row][col].colorIndex
                if (currentBoard[row][col].isFilled) {
                    consumePowerUp(PowerUpType.COLOR_BLAST)
                    for (r in 0 until 8) {
                        for (c in 0 until 8) {
                            if (currentBoard[r][c].isFilled && currentBoard[r][c].colorIndex == targetColor) {
                                currentBoard[r][c] = CellState()
                            }
                        }
                    }
                    _board.value = currentBoard
                    soundManager.playLineClear(2, 2)
                    hapticsManager.vibrateLineClear()
                }
            }
            else -> {}
        }
        _activePowerUp.value = null
        checkGameOverCondition()
    }

    private fun consumePowerUp(type: PowerUpType) {
        viewModelScope.launch {
            val p = _userProfile.value
            val updated = when (type) {
                PowerUpType.HAMMER -> p.copy(hammerCount = maxOf(0, p.hammerCount - 1))
                PowerUpType.SHUFFLE -> p.copy(shuffleCount = maxOf(0, p.shuffleCount - 1))
                PowerUpType.UNDO -> p.copy(undoCount = maxOf(0, p.undoCount - 1))
                PowerUpType.BOMB -> p.copy(bombCount = maxOf(0, p.bombCount - 1))
                PowerUpType.COLOR_BLAST -> p.copy(colorBlastCount = maxOf(0, p.colorBlastCount - 1))
            }
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    // ==========================================
    // THEME SHOP & COSMETICS
    // ==========================================

    fun selectTheme(themeId: ThemeId) {
        val theme = GameThemes.getById(themeId)
        val profile = _userProfile.value
        val unlocked = profile.unlockedThemes.split(",").toSet()

        if (unlocked.contains(themeId.name)) {
            _activeTheme.value = theme
            viewModelScope.launch {
                val updated = profile.copy(activeThemeId = themeId.name)
                _userProfile.value = updated
                repository.saveProfile(updated)
            }
        } else if (profile.coins >= theme.costCoins && profile.gems >= theme.costGems) {
            // Purchase theme
            viewModelScope.launch {
                val updated = profile.copy(
                    coins = profile.coins - theme.costCoins,
                    gems = profile.gems - theme.costGems,
                    unlockedThemes = profile.unlockedThemes + ",${themeId.name}",
                    activeThemeId = themeId.name
                )
                _userProfile.value = updated
                _activeTheme.value = theme
                repository.saveProfile(updated)
                soundManager.playLevelUp()
            }
        } else {
            soundManager.playInvalid()
        }
    }

    fun buyPowerUp(type: PowerUpType, costCoins: Int) {
        val profile = _userProfile.value
        if (profile.coins >= costCoins) {
            viewModelScope.launch {
                val updated = when (type) {
                    PowerUpType.HAMMER -> profile.copy(coins = profile.coins - costCoins, hammerCount = profile.hammerCount + 1)
                    PowerUpType.SHUFFLE -> profile.copy(coins = profile.coins - costCoins, shuffleCount = profile.shuffleCount + 1)
                    PowerUpType.UNDO -> profile.copy(coins = profile.coins - costCoins, undoCount = profile.undoCount + 1)
                    PowerUpType.BOMB -> profile.copy(coins = profile.coins - costCoins, bombCount = profile.bombCount + 1)
                    PowerUpType.COLOR_BLAST -> profile.copy(coins = profile.coins - costCoins, colorBlastCount = profile.colorBlastCount + 1)
                }
                _userProfile.value = updated
                repository.saveProfile(updated)
                soundManager.playSnap()
            }
        } else {
            soundManager.playInvalid()
        }
    }

    // ==========================================
    // SETTINGS
    // ==========================================

    fun toggleSfx() {
        val next = !soundManager.isSfxEnabled
        soundManager.isSfxEnabled = next
        updateProfileSettings { it.copy(soundEnabled = next) }
    }

    fun toggleMusic() {
        val next = !soundManager.isMusicEnabled
        soundManager.isMusicEnabled = next
        updateProfileSettings { it.copy(musicEnabled = next) }
    }

    fun toggleHaptics() {
        val next = !hapticsManager.isHapticsEnabled
        hapticsManager.isHapticsEnabled = next
        updateProfileSettings { it.copy(hapticsEnabled = next) }
    }

    fun toggleReducedMotion() {
        val next = !_userProfile.value.reducedMotion
        updateProfileSettings { it.copy(reducedMotion = next) }
    }

    fun toggleColorblindMode() {
        val next = !_userProfile.value.colorblindMode
        updateProfileSettings { it.copy(colorblindMode = next) }
    }

    private fun updateProfileSettings(block: (UserProfileEntity) -> UserProfileEntity) {
        viewModelScope.launch {
            val updated = block(_userProfile.value)
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            val reset = UserProfileEntity()
            _userProfile.value = reset
            repository.saveProfile(reset)
            _activeTheme.value = GameThemes.NeonCity
            startNewGame(GameMode.CLASSIC)
        }
    }

    // ==========================================
    // PARTICLE ENGINE (HIGH 60FPS EFFICIENCY)
    // ==========================================

    fun spawnParticleBurst(x: Float, y: Float, palette: List<Color>, count: Int = 10) {
        if (_userProfile.value.reducedMotion) return
        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Math.random() * 2.0 * Math.PI
            val speed = (100f + Math.random() * 350f).toFloat()
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            newParticles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = palette.random(),
                    size = (4f + Math.random() * 8f).toFloat(),
                    maxLife = (0.4f + Math.random() * 0.4f).toFloat()
                )
            )
        }
        _particles.value = (_particles.value + newParticles).takeLast(160)
    }

    /**
     * Spawns an energetic neon particle burst along an entire cleared row or column.
     * Incorporates neon shockwave rings, spinning diamond sparkles, laser streaks, and glowing sparks.
     */
    fun spawnRowClearNeonEffect(index: Int, isRow: Boolean) {
        if (_userProfile.value.reducedMotion) return
        if (boardScreenBounds.width <= 0 || cellSizePx <= 0) return

        val palette = _activeTheme.value.particleColors
        val newParticles = mutableListOf<Particle>()
        val primaryColor = palette.firstOrNull() ?: Color(0xFF00E5FF)
        val secondaryColor = palette.getOrNull(1) ?: Color(0xFFFF007A)
        val accentGold = Color(0xFFFFD700)

        if (isRow) {
            val centerY = boardScreenBounds.top + (index + 0.5f) * cellSizePx

            // High-speed horizontal laser streaks across the row
            for (i in 0 until 4) {
                val startX = boardScreenBounds.left + (Math.random() * 0.4f * boardScreenBounds.width).toFloat()
                val speed = (420f + Math.random() * 520f).toFloat()
                newParticles.add(
                    Particle(
                        x = startX,
                        y = centerY + ((Math.random() - 0.5f) * cellSizePx * 0.35f).toFloat(),
                        vx = speed,
                        vy = ((Math.random() - 0.5f) * 40f).toFloat(),
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        size = (3.5f + Math.random() * 3f).toFloat(),
                        maxLife = 0.55f,
                        type = ParticleType.LIGHT_STREAK,
                        length = (45f + Math.random() * 65f).toFloat()
                    )
                )
            }

            // Cell explosions with expanding neon rings and sparkle stars
            for (c in 0 until 8) {
                val cx = boardScreenBounds.left + (c + 0.5f) * cellSizePx
                val cy = centerY

                // Neon Shockwave Ring
                newParticles.add(
                    Particle(
                        x = cx,
                        y = cy,
                        vx = 0f,
                        vy = 0f,
                        color = palette[c % palette.size],
                        size = cellSizePx * 0.45f,
                        maxLife = 0.45f,
                        type = ParticleType.NEON_RING
                    )
                )

                // Spinning Diamond Star Sparkle
                newParticles.add(
                    Particle(
                        x = cx,
                        y = cy,
                        vx = ((Math.random() - 0.5f) * 160f).toFloat(),
                        vy = ((Math.random() - 0.5f) * 180f - 50f).toFloat(),
                        color = accentGold,
                        size = (6f + Math.random() * 6f).toFloat(),
                        maxLife = (0.5f + Math.random() * 0.3f).toFloat(),
                        type = ParticleType.STAR_SPARKLE,
                        rotation = (Math.random() * 360f).toFloat(),
                        vRot = ((Math.random() - 0.5f) * 400f).toFloat()
                    )
                )

                // Outward neon sparks (top & bottom vertical burst)
                for (s in 0 until 4) {
                    val angle = if (s % 2 == 0) (-Math.PI / 2.0 + (Math.random() - 0.5)) else (Math.PI / 2.0 + (Math.random() - 0.5))
                    val speed = (120f + Math.random() * 260f).toFloat()
                    newParticles.add(
                        Particle(
                            x = cx,
                            y = cy,
                            vx = (cos(angle) * speed).toFloat(),
                            vy = (sin(angle) * speed).toFloat(),
                            color = palette.random(),
                            size = (4.5f + Math.random() * 5.5f).toFloat(),
                            maxLife = (0.4f + Math.random() * 0.35f).toFloat(),
                            type = ParticleType.NEON_CIRCLE
                        )
                    )
                }
            }
        } else {
            // Column clear
            val centerX = boardScreenBounds.left + (index + 0.5f) * cellSizePx

            // High-speed vertical laser streaks
            for (i in 0 until 4) {
                val startY = boardScreenBounds.top + (Math.random() * 0.4f * boardScreenBounds.height).toFloat()
                val speed = (420f + Math.random() * 520f).toFloat()
                newParticles.add(
                    Particle(
                        x = centerX + ((Math.random() - 0.5f) * cellSizePx * 0.35f).toFloat(),
                        y = startY,
                        vx = ((Math.random() - 0.5f) * 40f).toFloat(),
                        vy = speed,
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        size = (3.5f + Math.random() * 3f).toFloat(),
                        maxLife = 0.55f,
                        type = ParticleType.LIGHT_STREAK,
                        length = (45f + Math.random() * 65f).toFloat()
                    )
                )
            }

            // Cell explosions with expanding neon rings and sparkle stars
            for (r in 0 until 8) {
                val cx = centerX
                val cy = boardScreenBounds.top + (r + 0.5f) * cellSizePx

                // Neon Shockwave Ring
                newParticles.add(
                    Particle(
                        x = cx,
                        y = cy,
                        vx = 0f,
                        vy = 0f,
                        color = palette[r % palette.size],
                        size = cellSizePx * 0.45f,
                        maxLife = 0.45f,
                        type = ParticleType.NEON_RING
                    )
                )

                // Spinning Diamond Star Sparkle
                newParticles.add(
                    Particle(
                        x = cx,
                        y = cy,
                        vx = ((Math.random() - 0.5f) * 180f - 50f).toFloat(),
                        vy = ((Math.random() - 0.5f) * 160f).toFloat(),
                        color = accentGold,
                        size = (6f + Math.random() * 6f).toFloat(),
                        maxLife = (0.5f + Math.random() * 0.3f).toFloat(),
                        type = ParticleType.STAR_SPARKLE,
                        rotation = (Math.random() * 360f).toFloat(),
                        vRot = ((Math.random() - 0.5f) * 400f).toFloat()
                    )
                )

                // Outward neon sparks (left & right horizontal burst)
                for (s in 0 until 4) {
                    val angle = if (s % 2 == 0) (0.0 + (Math.random() - 0.5)) else (Math.PI + (Math.random() - 0.5))
                    val speed = (120f + Math.random() * 260f).toFloat()
                    newParticles.add(
                        Particle(
                            x = cx,
                            y = cy,
                            vx = (cos(angle) * speed).toFloat(),
                            vy = (sin(angle) * speed).toFloat(),
                            color = palette.random(),
                            size = (4.5f + Math.random() * 5.5f).toFloat(),
                            maxLife = (0.4f + Math.random() * 0.35f).toFloat(),
                            type = ParticleType.NEON_CIRCLE
                        )
                    )
                }
            }
        }

        _particles.value = (_particles.value + newParticles).takeLast(180)
    }

    private fun startParticlesLoop() {
        viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                delay(16) // ~60fps
                val currentTime = System.currentTimeMillis()
                val dt = ((currentTime - lastTime) / 1000f).coerceIn(0.001f, 0.05f)
                lastTime = currentTime

                // Update particles
                val currentP = _particles.value
                if (currentP.isNotEmpty()) {
                    val updated = mutableListOf<Particle>()
                    for (p in currentP) {
                        p.life -= dt
                        if (p.life > 0) {
                            p.x += p.vx * dt
                            p.y += p.vy * dt
                            p.vy += 380f * dt // gentle gravity
                            p.rotation += p.vRot * dt
                            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                            updated.add(p)
                        }
                    }
                    _particles.value = updated
                }

                // Clean floating scores older than 1.4s
                val currentFloating = _floatingScores.value
                if (currentFloating.isNotEmpty()) {
                    _floatingScores.value = currentFloating.filter { currentTime - it.creationTime < 1400 }
                }
            }
        }
    }
}
