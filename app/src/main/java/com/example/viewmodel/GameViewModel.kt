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
import com.example.model.AdventureLevel
import com.example.model.AdventureLevelsCatalog
import com.example.model.BlockPiece
import com.example.model.CellState
import com.example.model.CollectibleType
import com.example.model.FloatingScore
import com.example.model.GameMode
import com.example.model.MomentumLevel
import com.example.model.Particle
import com.example.model.PieceTemplates
import com.example.model.PowerUpType
import com.example.model.SpecialType
import com.example.theme.GameThemes
import com.example.theme.ThemeId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        return List(8) { List(8) { CellState() } }
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
        val trio = PieceTemplates.generateTrio(_board.value, _userProfile.value.currentLevel)
        _availablePieces.value = trio
        checkGameOverCondition()
    }

    // ==========================================
    // DRAG AND DROP
    // ==========================================

    fun onDragStart(pieceIndex: Int, initialTouchOffset: Offset) {
        val piece = _availablePieces.value.getOrNull(pieceIndex) ?: return
        _draggingPieceIndex.value = pieceIndex
        _dragOffset.value = initialTouchOffset
        soundManager.playPickup()
        updateHoverAndValidity(piece, initialTouchOffset)
    }

    fun onDrag(dragDelta: Offset) {
        val idx = _draggingPieceIndex.value ?: return
        val piece = _availablePieces.value.getOrNull(idx) ?: return
        val newOffset = _dragOffset.value + dragDelta
        _dragOffset.value = newOffset
        updateHoverAndValidity(piece, newOffset)
    }

    fun onDragEnd() {
        val idx = _draggingPieceIndex.value
        val piece = idx?.let { _availablePieces.value.getOrNull(it) }

        if (idx != null && piece != null && _isPlacementValid.value) {
            val (startR, startC) = _hoverGridPosition.value ?: (0 to 0)
            placePiece(idx, piece, startR, startC)
        } else if (idx != null) {
            soundManager.playInvalid()
        }

        _draggingPieceIndex.value = null
        _hoverGridPosition.value = null
        _isPlacementValid.value = false
    }

    fun onDragCancel() {
        _draggingPieceIndex.value = null
        _hoverGridPosition.value = null
        _isPlacementValid.value = false
    }

    private fun updateHoverAndValidity(piece: BlockPiece, touchOffset: Offset) {
        if (boardScreenBounds.width <= 0 || cellSizePx <= 0) {
            _isPlacementValid.value = false
            return
        }

        // Lift piece slightly above finger so thumb does not occlude the view!
        val visualYOffset = cellSizePx * 1.5f
        val adjustedX = touchOffset.x - (piece.cols * cellSizePx / 2f)
        val adjustedY = (touchOffset.y - visualYOffset) - (piece.rows * cellSizePx / 2f)

        val localX = adjustedX - boardScreenBounds.left
        val localY = adjustedY - boardScreenBounds.top

        val col = Math.round(localX / cellSizePx).coerceIn(0, 8 - piece.cols)
        val row = Math.round(localY / cellSizePx).coerceIn(0, 8 - piece.rows)

        val canPlace = piece.canPlaceAt(_board.value, row, col)
        _hoverGridPosition.value = row to col
        _isPlacementValid.value = canPlace
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

        // 1. Stamp piece onto board
        val currentBoard = _board.value.map { it.toMutableList() }
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shapeMatrix[r][c]) {
                    val br = startR + r
                    val bc = startC + c
                    currentBoard[br][bc] = CellState(
                        isFilled = true,
                        colorIndex = piece.colorIndex,
                        specialType = piece.specialType,
                        collectible = piece.collectible
                    )
                }
            }
        }

        // Mark piece consumed
        val updatedPieces = _availablePieces.value.toMutableList()
        updatedPieces[index] = null
        _availablePieces.value = updatedPieces

        soundManager.playSnap()
        hapticsManager.vibrateSnap()

        // Points for placing
        val placePoints = (piece.blockCount * 10 * _momentum.value.multiplier).toInt()
        addScore(placePoints, "PLACED")

        // 2. Detect lines to clear
        val fullRows = (0 until 8).filter { r -> currentBoard[r].all { it.isFilled } }
        val fullCols = (0 until 8).filter { c -> (0 until 8).all { r -> currentBoard[r][c].isFilled } }

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
        boardState: List<MutableList<CellState>>,
        fullRows: List<Int>,
        fullCols: List<Int>
    ) {
        val totalLines = fullRows.size + fullCols.size
        _linesClearedThisRound.value += totalLines

        // Combo increment
        val currentCombo = _combo.value + 1
        _combo.value = currentCombo

        val banner = when (currentCombo) {
            1 -> if (totalLines > 1) "${totalLines}X LINE CLEAR!" else "LINE CLEAR!"
            2 -> "2X FLOW!"
            3 -> "PERFECT COMBO!"
            4 -> "INSANE FLOW!"
            5 -> "ULTRA COMBO!"
            else -> "BLOCK GOD ${currentCombo}X!"
        }
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

        // Calculate score with exponential bonuses for multi-lines and combos
        val lineBase = when (totalLines) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            else -> 2200
        }
        val comboBonus = (currentCombo - 1) * 150
        val earnedScore = ((lineBase + comboBonus) * _momentum.value.multiplier).toInt()
        addScore(earnedScore, banner)

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

        // Find cells to clear & check special blocks
        val cellsToClear = mutableSetOf<Pair<Int, Int>>()
        for (r in fullRows) {
            for (c in 0 until 8) cellsToClear.add(r to c)
        }
        for (c in fullCols) {
            for (r in 0 until 8) cellsToClear.add(r to c)
        }

        // Process special blocks inside cleared lines
        val secondaryClears = mutableSetOf<Pair<Int, Int>>()
        for ((r, c) in cellsToClear) {
            val cell = boardState[r][c]
            when (cell.specialType) {
                SpecialType.BOMB -> {
                    soundManager.playBomb()
                    hapticsManager.vibrateSpecial()
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0..7 && nc in 0..7) secondaryClears.add(nr to nc)
                        }
                    }
                }
                SpecialType.LIGHTNING_H -> {
                    for (col in 0 until 8) secondaryClears.add(r to col)
                }
                SpecialType.LIGHTNING_V -> {
                    for (row in 0 until 8) secondaryClears.add(row to c)
                }
                SpecialType.RAINBOW -> {
                    // Clears all adjacent blocks
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0..7 && nc in 0..7) secondaryClears.add(nr to nc)
                        }
                    }
                }
                SpecialType.ICE -> {
                    // Handled in ice fracture phase
                }
                else -> {}
            }

            // Check Collectibles
            if (cell.collectible != CollectibleType.NONE) {
                collectItem(cell.collectible, r, c)
            }
        }
        cellsToClear.addAll(secondaryClears)

        // Fracture / Shatter nearby Ice Blocks
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val cell = boardState[r][c]
                if (cell.specialType == SpecialType.ICE && cell.isFilled) {
                    val inClearedLine = r in fullRows || c in fullCols
                    val isNearBlast = secondaryClears.any { (sr, sc) -> kotlin.math.abs(sr - r) <= 1 && kotlin.math.abs(sc - c) <= 1 }
                    if (inClearedLine || isNearBlast) {
                        val hits = cell.iceHitsLeft - 1
                        if (hits <= 0) {
                            cellsToClear.add(r to c)
                            val cx = boardScreenBounds.left + (c + 0.5f) * cellSizePx
                            val cy = boardScreenBounds.top + (r + 0.5f) * cellSizePx
                            spawnParticleBurst(cx, cy, listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color.White), count = 8)
                        } else {
                            boardState[r][c] = cell.copy(iceHitsLeft = hits)
                        }
                    }
                }
            }
        }

        // Spawn Burst Particles at cleared cells
        val palette = _activeTheme.value.particleColors
        for ((r, c) in cellsToClear) {
            val cx = boardScreenBounds.left + (c + 0.5f) * cellSizePx
            val cy = boardScreenBounds.top + (r + 0.5f) * cellSizePx
            spawnParticleBurst(cx, cy, palette, count = 5)
        }

        // Apply clears to board
        for ((r, c) in cellsToClear) {
            boardState[r][c] = CellState()
        }
        _board.value = boardState

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

        val canFitAny = pieces.any { it.canFitAnywhere(_board.value) }
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
        val active = _activePowerUp.value ?: return
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
        _particles.value = (_particles.value + newParticles).takeLast(120)
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
                            p.vy += 450f * dt // gentle gravity
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
