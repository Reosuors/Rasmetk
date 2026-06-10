package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CommentEntity
import com.example.data.DrawingEntity
import com.example.data.DrawingRepository
import com.example.util.CanvasSaver
import com.example.util.ProfanityFilter
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Screen navigation states
sealed interface ScreenState {
    object Home : ScreenState
    object Canvas : ScreenState
    data class DrawingDetail(val drawingId: Int) : ScreenState
    data class AuthorProfile(val authorName: String) : ScreenState
}

// Drawing brush modes
enum class ShapeType {
    FREEHAND, LINE, RECTANGLE, CIRCLE, ARROW
}

// Brush style customize
enum class BrushStyle {
    SOLID, DASHED, DOTTED, AIRBRUSH, NEON
}

// Symmetry modes for artist patterns
enum class SymmetryMode {
    NONE, HORIZONTAL, VERTICAL, RADIAL
}

// Canvas grid layout styles
enum class CanvasGridStyle {
    BLANK, GRID, DOTS, ISOMETRIC
}

// Layer data state
data class CanvasLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val isVisible: Boolean = true,
    val opacity: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

// Drawing action element
sealed interface DrawingAction {
    val layerId: String
    val brushStyle: BrushStyle
    val color: Color
    val strokeWidth: Float
    val isEraser: Boolean
    val symmetryMode: SymmetryMode

    data class PathAction(
        val path: Path,
        override val color: Color,
        override val strokeWidth: Float,
        override val isEraser: Boolean = false,
        override val layerId: String = "default",
        override val brushStyle: BrushStyle = BrushStyle.SOLID,
        override val symmetryMode: SymmetryMode = SymmetryMode.NONE
    ) : DrawingAction

    data class ShapeAction(
        val shapeType: ShapeType,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        override val color: Color,
        override val strokeWidth: Float,
        override val isEraser: Boolean = false,
        override val layerId: String = "default",
        override val brushStyle: BrushStyle = BrushStyle.SOLID,
        override val symmetryMode: SymmetryMode = SymmetryMode.NONE
    ) : DrawingAction
}

class DrawingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val rootDatabase: AppDatabase by lazy {
        androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rasmatak_db"
        ).build()
    }
    
    private val repository: DrawingRepository by lazy {
        DrawingRepository(context, rootDatabase)
    }

    private val sharedPreferences = context.getSharedPreferences("rasmatak_prefs", Context.MODE_PRIVATE)

    // Current app screens navigation stack
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Home)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()
    private val _screenHistory = mutableListOf<ScreenState>(ScreenState.Home)

    // User Profile Username
    private val _username = MutableStateFlow(sharedPreferences.getString("username", "رسام مبدع") ?: "رسام مبدع")
    val username: StateFlow<String> = _username.asStateFlow()

    // Firebase Authentication & Account variables
    private val _firebaseEmail = MutableStateFlow(sharedPreferences.getString("firebase_email", "") ?: "")
    val firebaseEmail: StateFlow<String> = _firebaseEmail.asStateFlow()

    private val _isFirebaseLoggedIn = MutableStateFlow(sharedPreferences.getBoolean("firebase_logged_in", false))
    val isFirebaseLoggedIn: StateFlow<Boolean> = _isFirebaseLoggedIn.asStateFlow()

    private val _firebasePrivateDrawings = MutableStateFlow<List<com.example.data.FirebaseDrawing>>(emptyList())
    val firebasePrivateDrawings: StateFlow<List<com.example.data.FirebaseDrawing>> = _firebasePrivateDrawings.asStateFlow()

    private val _firebasePrivateLoading = MutableStateFlow(false)
    val firebasePrivateLoading: StateFlow<Boolean> = _firebasePrivateLoading.asStateFlow()

    private val _firebasePrivateLoadError = MutableStateFlow<String?>(null)
    val firebasePrivateLoadError: StateFlow<String?> = _firebasePrivateLoadError.asStateFlow()

    private val _firebaseAuthLoading = MutableStateFlow(false)
    val firebaseAuthLoading: StateFlow<Boolean> = _firebaseAuthLoading.asStateFlow()

    private val _firebaseAuthError = MutableStateFlow<String?>(null)
    val firebaseAuthError: StateFlow<String?> = _firebaseAuthError.asStateFlow()

    fun clearAuthError() {
        _firebaseAuthError.value = null
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _firebaseAuthLoading.value = true
            _firebaseAuthError.value = null
            com.example.data.FirebaseSyncManager.loginWithFirebase(email, pass).fold(
                onSuccess = { (usr, mail) ->
                    _username.value = usr
                    sharedPreferences.edit()
                        .putString("username", usr)
                        .putString("firebase_email", mail)
                        .putBoolean("firebase_logged_in", true)
                        .apply()
                    _firebaseEmail.value = mail
                    _isFirebaseLoggedIn.value = true
                    _firebaseAuthLoading.value = false
                    syncPrivateGallery()
                    onSuccess()
                },
                onFailure = { err ->
                    _firebaseAuthError.value = err.message ?: "خطأ في تسجيل الدخول"
                    _firebaseAuthLoading.value = false
                }
            )
        }
    }

    fun signUp(email: String, usr: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _firebaseAuthLoading.value = true
            _firebaseAuthError.value = null
            com.example.data.FirebaseSyncManager.signUpWithFirebase(email, usr, pass).fold(
                onSuccess = { finalizedUser ->
                    _username.value = finalizedUser
                    sharedPreferences.edit()
                        .putString("username", finalizedUser)
                        .putString("firebase_email", email.trim())
                        .putBoolean("firebase_logged_in", true)
                        .apply()
                    _firebaseEmail.value = email.trim()
                    _isFirebaseLoggedIn.value = true
                    _firebaseAuthLoading.value = false
                    syncPrivateGallery()
                    onSuccess()
                },
                onFailure = { err ->
                    _firebaseAuthError.value = err.message ?: "خطأ في التسجيل"
                    _firebaseAuthLoading.value = false
                }
            )
        }
    }

    fun signOut() {
        sharedPreferences.edit()
            .putString("firebase_email", "")
            .putBoolean("firebase_logged_in", false)
            .apply()
        _firebaseEmail.value = ""
        _isFirebaseLoggedIn.value = false
        _firebasePrivateDrawings.value = emptyList()
    }

    fun syncPrivateGallery() {
        val currEmail = _firebaseEmail.value
        if (currEmail.isBlank() || !_isFirebaseLoggedIn.value) return
        viewModelScope.launch {
            _firebasePrivateLoading.value = true
            _firebasePrivateLoadError.value = null
            com.example.data.FirebaseSyncManager.fetchPrivateDrawings(currEmail)
                .onSuccess { list ->
                    _firebasePrivateDrawings.value = list.sortedByDescending { it.timestamp }
                    _firebasePrivateLoading.value = false
                }
                .onFailure { err ->
                    _firebasePrivateLoadError.value = err.message ?: "فشل تحديث المعرض"
                    _firebasePrivateLoading.value = false
                }
        }
    }

    // App language: "ar" or "en"
    private val _appLanguage = MutableStateFlow(sharedPreferences.getString("app_language", "ar") ?: "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        sharedPreferences.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

    // Custom Canvas creation info
    val currentCanvasWidth = mutableStateOf(1000)
    val currentCanvasHeight = mutableStateOf(1000)
    val currentDrawingTitle = mutableStateOf("")

    // Searching
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Public list or Private list in primary drawings gallery
    private val _isShowingPublicTabInGallery = MutableStateFlow(false)
    val isShowingPublicTabInGallery: StateFlow<Boolean> = _isShowingPublicTabInGallery.asStateFlow()

    // State of drawings list
    val drawingsList: StateFlow<List<DrawingEntity>> = repository.allDrawingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter results dynamically using search and tabs
    val filteredDrawings: StateFlow<List<DrawingEntity>> = combine(
        drawingsList,
        _searchQuery,
        _isShowingPublicTabInGallery
    ) { baseList, query, isPublicTab ->
        // Keep only personal drawings (projects of the user only)
        var list = baseList.filter { it.isLocalOriginal }
        // Filter by Query (code or title or author)
        if (query.isNotEmpty()) {
            list = list.filter {
                it.drawingIdString.equals(query, ignoreCase = true) ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.authorName.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen specific tracking state
    private val _activeDrawingId = MutableStateFlow<Int?>(null)
    val activeDrawing: StateFlow<DrawingEntity?> = _activeDrawingId.flatMapLatest { id ->
        if (id != null) {
            repository.getDrawingByIdFlow(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeComments: StateFlow<List<CommentEntity>> = _activeDrawingId.flatMapLatest { id ->
        if (id != null) {
            repository.getCommentsForDrawingFlow(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active author profile drawings
    private val _activeAuthorName = MutableStateFlow<String?>(null)
    val activeAuthorName: StateFlow<String?> = _activeAuthorName.asStateFlow()

    val activeAuthorDrawings: StateFlow<List<DrawingEntity>> = combine(
        drawingsList,
        _activeAuthorName
    ) { allDrawings, authorName ->
        if (authorName != null) {
            allDrawings.filter { it.authorName.equals(authorName, ignoreCase = true) }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isFollowingActiveAuthor: StateFlow<Boolean> = _activeAuthorName.flatMapLatest { author ->
        if (author != null) {
            repository.isArtistFollowedFlow(author)
        } else {
            flowOf(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Complete set of followed artists to count or track
    val followedArtists: StateFlow<List<String>> = repository.followedArtistsFlow
        .map { list -> list.map { it.artistName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profanity Filter alerts
    private val _showProfanityErrorDialog = MutableStateFlow<Boolean>(false)
    val showProfanityErrorDialog: StateFlow<Boolean> = _showProfanityErrorDialog.asStateFlow()

    // Canvas drawing path lists
    val canvasStrokes = mutableStateListOf<DrawingAction>()
    val canvasRedoStrokes = mutableStateListOf<DrawingAction>()

    // Canvas styling brush states
    val brushColor = mutableStateOf(Color.Black)
    val brushSize = mutableStateOf(10f)
    val brushShapeType = mutableStateOf(ShapeType.FREEHAND)
    val brushStyle = mutableStateOf(BrushStyle.SOLID)
    val brushIsEraser = mutableStateOf(false)
    val symmetryMode = mutableStateOf(SymmetryMode.NONE)
    val canvasGridStyle = mutableStateOf(CanvasGridStyle.BLANK)

    // Layers Support
    val layers = mutableStateListOf<CanvasLayer>()
    val activeLayerId = mutableStateOf("default")

    // Color Mixer support
    val mixerColorA = mutableStateOf(Color.Red)
    val mixerColorB = mutableStateOf(Color.Blue)
    val mixerRatio = mutableStateOf(0.5f)

    // Canvas saving feedback
    private val _saveFinishedUri = MutableStateFlow<String?>(null)
    val saveFinishedUri: StateFlow<String?> = _saveFinishedUri.asStateFlow()

    init {
        // Run seed initial database setup
        viewModelScope.launch {
            repository.checkAndSeedData()
        }
        // Initialize default background layer
        if (layers.isEmpty()) {
            layers.add(CanvasLayer(id = "default", name = "الطبقة الأساسية"))
        }
        // Sync cloud private gallery if logged in
        if (_isFirebaseLoggedIn.value && _firebaseEmail.value.isNotBlank()) {
            syncPrivateGallery()
        }
    }

    // Layers Management functions
    fun addLayer() {
        val newId = UUID.randomUUID().toString()
        val num = layers.size + 1
        layers.add(CanvasLayer(id = newId, name = "طبقة $num"))
        activeLayerId.value = newId
    }

    fun removeLayer(id: String) {
        // Keep at least one layer
        if (layers.size <= 1) return
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1) {
            layers.removeAt(index)
            // Remove all strokes belongs to this layer
            canvasStrokes.removeAll { it.layerId == id }
            canvasRedoStrokes.removeAll { it.layerId == id }
            
            if (activeLayerId.value == id) {
                activeLayerId.value = layers.lastOrNull()?.id ?: "default"
            }
        }
    }

    fun toggleLayerVisibility(id: String) {
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = layers[index]
            layers[index] = current.copy(isVisible = !current.isVisible)
        }
    }

    fun setLayerOpacity(id: String, opacity: Float) {
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = layers[index]
            layers[index] = current.copy(opacity = opacity)
        }
    }

    fun updateLayerOffset(id: String, dx: Float, dy: Float) {
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = layers[index]
            layers[index] = current.copy(
                offsetX = current.offsetX + dx,
                offsetY = current.offsetY + dy
            )
        }
    }

    fun renameLayer(id: String, newName: String) {
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1 && newName.isNotBlank()) {
            val current = layers[index]
            layers[index] = current.copy(name = newName)
        }
    }

    fun moveLayerUp(id: String) {
        val index = layers.indexOfFirst { it.id == id }
        if (index > 0) {
            val temp = layers[index]
            layers.removeAt(index)
            layers.add(index - 1, temp)
        }
    }

    fun moveLayerDown(id: String) {
        val index = layers.indexOfFirst { it.id == id }
        if (index != -1 && index < layers.size - 1) {
            val temp = layers[index]
            layers.removeAt(index)
            layers.add(index + 1, temp)
        }
    }

    fun clearLayer(id: String) {
        canvasStrokes.removeAll { it.layerId == id }
        canvasRedoStrokes.removeAll { it.layerId == id }
    }

    fun selectLayer(id: String) {
        val exists = layers.any { it.id == id }
        if (exists) {
            activeLayerId.value = id
        }
    }

    fun getActiveLayer(): CanvasLayer {
        return layers.firstOrNull { it.id == activeLayerId.value } ?: CanvasLayer(id = "default", name = "الأساسية")
    }

    // Color Mixing functions
    fun mixColorsResult(): Color {
        val rA = mixerColorA.value
        val rB = mixerColorB.value
        val ratio = mixerRatio.value
        return Color(
            red = rA.red * (1f - ratio) + rB.red * ratio,
            green = rA.green * (1f - ratio) + rB.green * ratio,
            blue = rA.blue * (1f - ratio) + rB.blue * ratio,
            alpha = rA.alpha * (1f - ratio) + rB.alpha * ratio
        )
    }

    fun applyMixedColorAsBrush() {
        brushColor.value = mixColorsResult()
    }

    fun navigateTo(state: ScreenState) {
        _screenHistory.add(state)
        _screenState.value = state

        // Sync auxiliary tracking values
        when (state) {
            is ScreenState.DrawingDetail -> {
                _activeDrawingId.value = state.drawingId
            }
            is ScreenState.AuthorProfile -> {
                _activeAuthorName.value = state.authorName
            }
            else -> {}
        }
    }

    fun navigateBack() {
        if (_screenHistory.size > 1) {
            _screenHistory.removeAt(_screenHistory.size - 1)
            val prevState = _screenHistory.last()
            _screenState.value = prevState

            // Sync on backward
            when (prevState) {
                is ScreenState.DrawingDetail -> {
                    _activeDrawingId.value = prevState.drawingId
                }
                is ScreenState.AuthorProfile -> {
                    _activeAuthorName.value = prevState.authorName
                }
                else -> {}
            }
        }
    }

    fun setUsername(name: String) {
        val userNameToSet = name.trim()
        if (ProfanityFilter.hasProfanity(userNameToSet)) {
            _showProfanityErrorDialog.value = true
            return
        }
        val validatedName = if (userNameToSet.isEmpty()) "رسام مبدع" else userNameToSet
        _username.value = validatedName
        sharedPreferences.edit().putString("username", validatedName).apply()
    }

    fun dismissProfanityDialog() {
        _showProfanityErrorDialog.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGalleryTab(isPublic: Boolean) {
        _isShowingPublicTabInGallery.value = isPublic
    }

    // Toggle Favorite / Likes
    fun toggleLikeOnDrawing(drawing: DrawingEntity) {
        viewModelScope.launch {
            val myUser = _username.value
            val currentLikesStr = drawing.likedByUsersJson
            val updatedLikesJson: String
            val updatedLikesCount: Int

            if (currentLikesStr.contains(",$myUser,")) {
                // Unlike it
                updatedLikesJson = currentLikesStr.replace(",$myUser,", ",")
                updatedLikesCount = (drawing.likesCount - 1).coerceAtLeast(0)
            } else {
                // Like it
                updatedLikesJson = if (currentLikesStr.isEmpty()) {
                    ",$myUser,"
                } else {
                    "$currentLikesStr$myUser,"
                }
                updatedLikesCount = drawing.likesCount + 1
            }

            val updatedDrawing = drawing.copy(
                likesCount = updatedLikesCount,
                likedByUsersJson = updatedLikesJson
            )
            repository.updateDrawing(updatedDrawing)
        }
    }

    fun toggleFollowArtist(artistName: String) {
        viewModelScope.launch {
            val isFollowed = followedArtists.value.contains(artistName)
            if (isFollowed) {
                repository.unfollowArtist(artistName)
            } else {
                repository.followArtist(artistName)
            }
        }
    }

    /**
     * Submit a new comment. Smart Profanity checks run before inserting!
     */
    fun addCommentToDrawing(drawingId: Int, content: String) {
        val text = content.trim()
        if (text.isEmpty()) return

        // Smart profanity filter
        if (ProfanityFilter.hasProfanity(text)) {
            _showProfanityErrorDialog.value = true
            return
        }

        viewModelScope.launch {
            val author = _username.value
            val newComment = CommentEntity(
                drawingId = drawingId,
                authorName = author,
                content = text
            )
            repository.insertComment(newComment)
        }
    }

    fun deleteDrawing(drawingId: Int) {
        viewModelScope.launch {
            repository.deleteDrawing(drawingId)
            // Navigate back if on detail screen to avoid hanging details
            if (_screenState.value is ScreenState.DrawingDetail) {
                navigateBack()
            }
        }
    }

    fun editDrawingInfo(drawingId: Int, newTitle: String, newDesc: String) {
        val title = newTitle.trim()
        val desc = newDesc.trim()
        if (title.isEmpty()) return

        // Profanity Check
        if (ProfanityFilter.hasProfanity(title) || ProfanityFilter.hasProfanity(desc)) {
            _showProfanityErrorDialog.value = true
            return
        }

        viewModelScope.launch {
            val current = repository.getDrawingByIdFlow(drawingId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, null).value
            if (current != null) {
                repository.updateDrawing(
                    current.copy(
                        title = title,
                        description = desc
                    )
                )
            }
        }
    }

    // Canvas Controls
    fun undoCanvas() {
        if (canvasStrokes.isNotEmpty()) {
            val last = canvasStrokes.removeAt(canvasStrokes.size - 1)
            canvasRedoStrokes.add(last)
        }
    }

    fun redoCanvas() {
        if (canvasRedoStrokes.isNotEmpty()) {
            val last = canvasRedoStrokes.removeAt(canvasRedoStrokes.size - 1)
            canvasStrokes.add(last)
        }
    }

    fun clearCanvas() {
        canvasStrokes.clear()
        canvasRedoStrokes.clear()
        // Reset layers stack to default base
        layers.clear()
        layers.add(CanvasLayer(id = "default", name = "الطبقة الأساسية"))
        activeLayerId.value = "default"
    }

    /**
     * Renders everything on the Canvas into a high-quality offline JPG/PNG,
     * seeds it to storage, saves metadata into SQLite, and optionally saves to the internal gallery.
     */
    fun saveCanvasToApp(title: String, description: String, isPublic: Boolean, onDone: () -> Unit) {
        val validatedTitle = title.trim()
        val validatedDesc = description.trim()
        if (validatedTitle.isEmpty()) return

        // Profanity check
        if (ProfanityFilter.hasProfanity(validatedTitle) || ProfanityFilter.hasProfanity(validatedDesc)) {
            _showProfanityErrorDialog.value = true
            return
        }

        viewModelScope.launch {
            // Re-render strokes to offline Bitmap
            val width = currentCanvasWidth.value.coerceIn(100, 4000)
            val height = currentCanvasHeight.value.coerceIn(100, 4000)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Fill background white
            canvas.drawColor(android.graphics.Color.WHITE)

            // Paint configurations
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Draw layer by layer sequentially onto offscreen bitmap
            for (layer in layers) {
                if (!layer.isVisible) continue
                
                // Save canvas state and apply layer translation
                canvas.save()
                canvas.translate(layer.offsetX, layer.offsetY)
                
                // Filter strokes belonging only to this layer
                val layerActions = canvasStrokes.filter { it.layerId == layer.id }
                
                for (action in layerActions) {
                    // Helper to get paint color with layer opacity applied
                    fun getStrokeColor(baseColor: Color, isEraser: Boolean, opacityMult: Float = 1f): Int {
                        return if (isEraser) {
                            android.graphics.Color.WHITE
                        } else {
                            android.graphics.Color.argb(
                                (baseColor.alpha * opacityMult * 255).toInt().coerceIn(0, 255),
                                (baseColor.red * 255).toInt(),
                                (baseColor.green * 255).toInt(),
                                (baseColor.blue * 255).toInt()
                            )
                        }
                    }
                    
                    // Helper to set up brush styles (solid, dashed, dotted)
                    fun configurePaintStyleAndEffects(p: Paint, brushStyle: BrushStyle, strokeW: Float) {
                        p.reset()
                        p.isAntiAlias = true
                        p.strokeCap = Paint.Cap.ROUND
                        p.strokeJoin = Paint.Join.ROUND
                        p.style = Paint.Style.STROKE
                        p.strokeWidth = strokeW
                        
                        when (brushStyle) {
                            BrushStyle.DASHED -> {
                                p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(strokeW * 2f, strokeW * 2f), 0f)
                            }
                            BrushStyle.DOTTED -> {
                                p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(strokeW * 0.2f, strokeW * 2f), 0f)
                            }
                            else -> {
                                p.pathEffect = null
                            }
                        }
                    }
                    
                    fun drawCore() {
                        if (action is DrawingAction.PathAction) {
                            try {
                                val androidPath = action.path.asAndroidPath()
                                
                                when (action.brushStyle) {
                                    BrushStyle.SOLID, BrushStyle.DASHED, BrushStyle.DOTTED -> {
                                        configurePaintStyleAndEffects(paint, action.brushStyle, action.strokeWidth)
                                        paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity)
                                        canvas.drawPath(androidPath, paint)
                                    }
                                    BrushStyle.AIRBRUSH -> {
                                        // Layer 1: Core
                                        configurePaintStyleAndEffects(paint, BrushStyle.SOLID, action.strokeWidth)
                                        paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity * 0.4f)
                                        canvas.drawPath(androidPath, paint)
                                        
                                        // Layer 2: Mid
                                        configurePaintStyleAndEffects(paint, BrushStyle.SOLID, action.strokeWidth * 1.5f)
                                        paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity * 0.18f)
                                        canvas.drawPath(androidPath, paint)
                                        
                                        // Layer 3: Soft Wide
                                        configurePaintStyleAndEffects(paint, BrushStyle.SOLID, action.strokeWidth * 2.5f)
                                        paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity * 0.08f)
                                        canvas.drawPath(androidPath, paint)
                                    }
                                    BrushStyle.NEON -> {
                                        // Glow behind
                                        configurePaintStyleAndEffects(paint, BrushStyle.SOLID, action.strokeWidth * 2.2f)
                                        paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity * 0.35f)
                                        canvas.drawPath(androidPath, paint)
                                        
                                        // White core inside
                                        configurePaintStyleAndEffects(paint, BrushStyle.SOLID, action.strokeWidth * 0.6f)
                                        paint.color = if (action.isEraser) android.graphics.Color.WHITE else android.graphics.Color.WHITE
                                        canvas.drawPath(androidPath, paint)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else if (action is DrawingAction.ShapeAction) {
                            val sx = action.startX
                            val sy = action.startY
                            val ex = action.endX
                            val ey = action.endY
                            
                            configurePaintStyleAndEffects(paint, action.brushStyle, action.strokeWidth)
                            paint.color = getStrokeColor(action.color, action.isEraser, layer.opacity)
                            
                            // Draw shape details
                            when (action.shapeType) {
                                ShapeType.LINE -> canvas.drawLine(sx, sy, ex, ey, paint)
                                ShapeType.RECTANGLE -> {
                                    val left = minOf(sx, ex)
                                    val top = minOf(sy, ey)
                                    val right = maxOf(sx, ex)
                                    val bottom = maxOf(sy, ey)
                                    canvas.drawRect(left, top, right, bottom, paint)
                                }
                                ShapeType.CIRCLE -> {
                                    val dx = ex - sx
                                    val dy = ey - sy
                                    val radius = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                    canvas.drawCircle(sx, sy, radius, paint)
                                }
                                ShapeType.ARROW -> {
                                    canvas.drawLine(sx, sy, ex, ey, paint)
                                    paint.style = Paint.Style.FILL
                                    val angle = Math.atan2((ey - sy).toDouble(), (ex - sx).toDouble())
                                    val arrowSize = 25f
                                    val headPath = AndroidPath()
                                    headPath.moveTo(ex, ey)
                                    headPath.lineTo(
                                        (ex - arrowSize * Math.cos(angle - Math.PI / 6)).toFloat(),
                                        (ey - arrowSize * Math.sin(angle - Math.PI / 6)).toFloat()
                                    )
                                    headPath.lineTo(
                                        (ex - arrowSize * Math.cos(angle + Math.PI / 6)).toFloat(),
                                        (ey - arrowSize * Math.sin(angle + Math.PI / 6)).toFloat()
                                    )
                                    headPath.close()
                                    canvas.drawPath(headPath, paint)
                                }
                                else -> {}
                            }
                        }
                    }

                    // 1. Draw Primary original
                    drawCore()

                    // 2. Mirror symmetrically under matching modes
                    val cX = 1080f / 2f
                    val cY = 1350f / 2f

                    if (action.symmetryMode == SymmetryMode.VERTICAL || action.symmetryMode == SymmetryMode.RADIAL) {
                        canvas.save()
                        canvas.scale(-1f, 1f, cX, cY)
                        drawCore()
                        canvas.restore()
                    }
                    if (action.symmetryMode == SymmetryMode.HORIZONTAL || action.symmetryMode == SymmetryMode.RADIAL) {
                        canvas.save()
                        canvas.scale(1f, -1f, cX, cY)
                        drawCore()
                        canvas.restore()
                    }
                    if (action.symmetryMode == SymmetryMode.RADIAL) {
                        canvas.save()
                        canvas.scale(-1f, -1f, cX, cY)
                        drawCore()
                        canvas.restore()
                    }
                }
                
                // Restore canvas state to reset layer translate
                canvas.restore()
            }

            // Save Bitmap local files
            val dir = File(context.filesDir, "drawings")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "rasmatak_${System.currentTimeMillis()}.png"
            val file = File(dir, fileName)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Save to Gallery as requested
            val galleryUri = CanvasSaver.saveBitmapToGallery(context, bitmap, validatedTitle)

            // Generate unique user-facing public ID string (e.g. "رسم-8321")
            val randCode = (1000..9999).random()
            val uniqueIdString = "رسم-${randCode}"

            // Save state to SQL Database
            val entity = DrawingEntity(
                title = validatedTitle,
                description = validatedDesc,
                drawingIdString = uniqueIdString,
                imagePath = file.absolutePath,
                isPublic = isPublic,
                authorName = _username.value,
                likesCount = 0,
                likedByUsersJson = ",",
                isLocalOriginal = true
            )

            val newDbId = repository.insertDrawing(entity).toInt()

            // If Firebase is logged in, upload to their private gallery
            if (_isFirebaseLoggedIn.value && _firebaseEmail.value.isNotBlank()) {
                try {
                    val base64 = com.example.data.FirebaseSyncManager.encodeBitmapToBase64(bitmap)
                    com.example.data.FirebaseSyncManager.saveToPrivateCloudGallery(
                        email = _firebaseEmail.value,
                        drawing = com.example.data.FirebaseDrawing(
                            drawingIdString = uniqueIdString,
                            title = validatedTitle,
                            description = validatedDesc,
                            authorName = _username.value,
                            imageBase64 = base64,
                            timestamp = System.currentTimeMillis(),
                            isPublic = false
                        )
                    )
                    syncPrivateGallery()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Clear Canvas
            clearCanvas()

            onDone()
            // Navigate directly to Detail Screen of newly saved item
            navigateTo(ScreenState.DrawingDetail(newDbId))
        }
    }

    /**
     * Share drawing through system share sheets and dynamically publish to Firebase for links.
     */
    fun shareDrawing(drawing: DrawingEntity, onPublishDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            val file = File(drawing.imagePath)
            if (file.exists()) {
                val bmp = android.graphics.BitmapFactory.decodeFile(drawing.imagePath)
                if (bmp != null) {
                    val base64 = com.example.data.FirebaseSyncManager.encodeBitmapToBase64(bmp)
                    val fbDrawing = com.example.data.FirebaseDrawing(
                        drawingIdString = drawing.drawingIdString,
                        title = drawing.title,
                        description = drawing.description,
                        authorName = drawing.authorName,
                        imageBase64 = base64,
                        timestamp = drawing.timestamp,
                        isPublic = true
                    )
                    // Publish dynamically to public Firebase central database
                    com.example.data.FirebaseSyncManager.publishPublicShare(fbDrawing)
                }

                // Generate web-sharing deep link
                val shareLink = "https://rasmatak.web.app/view?id=${drawing.drawingIdString}"
                
                // Copy link to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("رسمتك", shareLink)
                clipboard.setPrimaryClip(clip)

                // Share text message with native Sharesheet
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "شاهد لوحتي الفنية '${drawing.title}' على تطبيق رسمتك سحابياً! 🎨☁️\nاضغط على الرابط التالي لمشاهدتها:\n$shareLink\n\nأو قم بالبحث عنها برمز اللوحة المباشر: ${drawing.drawingIdString}")
                    type = "text/plain"
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = android.content.Intent.createChooser(shareIntent, "مشاركة اللوحة الفنية عبر")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                onPublishDone(shareLink)
            }
        }
    }

    /**
     * Downloads and saves any drawing to the public system gallery
     */
    fun downloadDrawingToDevice(drawing: DrawingEntity, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val file = File(drawing.imagePath)
            if (file.exists()) {
                val bmp = android.graphics.BitmapFactory.decodeFile(drawing.imagePath)
                if (bmp != null) {
                    val savedUri = CanvasSaver.saveBitmapToGallery(context, bmp, drawing.title)
                    onDone(savedUri != null)
                } else {
                    onDone(false)
                }
            } else {
                onDone(false)
            }
        }
    }

    /**
     * Imports a cloud drawing from a pasted share link or code ID, decodes Base64, saves file, adds to DB, and opens Detail View.
     */
    fun importDrawingByCode(code: String, onResult: (String?, Boolean) -> Unit) {
        val cleanCode = code.trim()
            .replace("https://rasmatak.web.app/view?id=", "")
            .replace("rasmatak://view?id=", "")
        
        if (cleanCode.isBlank()) {
            onResult("رجاءً أدخل رمز مشاركة أو رابط صحيح", false)
            return
        }

        viewModelScope.launch {
            com.example.data.FirebaseSyncManager.fetchSharedDrawingByCode(cleanCode).fold(
                onSuccess = { fbDrawing ->
                    // First check if already exists in local DB
                    val allDrawings = drawingsList.value
                    val existing = allDrawings.firstOrNull { it.drawingIdString == fbDrawing.drawingIdString }
                    if (existing != null) {
                        navigateTo(ScreenState.DrawingDetail(existing.id))
                        onResult("اللوحة موجودة بالفعل في مرسمك!", true)
                        return@launch
                    }

                    // Decode bitmap from base64
                    val decodedBmp = com.example.data.FirebaseSyncManager.decodeBase64ToBitmap(fbDrawing.imageBase64)
                    if (decodedBmp != null) {
                        // Save bitmap local file
                        val dir = File(context.filesDir, "drawings")
                        if (!dir.exists()) dir.mkdirs()
                        val fileName = "rasmatak_imported_${System.currentTimeMillis()}.png"
                        val file = File(dir, fileName)
                        try {
                            FileOutputStream(file).use { out ->
                                decodedBmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Insert to SQLite local database
                        val entity = DrawingEntity(
                            title = fbDrawing.title,
                            description = fbDrawing.description,
                            drawingIdString = fbDrawing.drawingIdString,
                            imagePath = file.absolutePath,
                            isPublic = true,
                            authorName = fbDrawing.authorName,
                            likesCount = fbDrawing.likesCount,
                            likedByUsersJson = ",",
                            isLocalOriginal = false
                        )
                        val newId = repository.insertDrawing(entity).toInt()
                        navigateTo(ScreenState.DrawingDetail(newId))
                        onResult("تم استيراد اللوحة بنجاح ورؤيتها! 🎉", true)
                    } else {
                        onResult("فشل فك تشفير صورة اللوحة الفنية", false)
                    }
                },
                onFailure = { err ->
                    onResult(err.message ?: "فشل في العثور على رمز اللوحة السحابي", false)
                }
            )
        }
    }

    /**
     * Direct import of a FirebaseDrawing (e.g. from the private cloud gallery lists).
     */
    fun importFirebaseDrawingDirectly(fbDrawing: com.example.data.FirebaseDrawing, onResult: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            // First check if already exists in local DB
            val allDrawings = drawingsList.value
            val existing = allDrawings.firstOrNull { it.drawingIdString == fbDrawing.drawingIdString }
            if (existing != null) {
                navigateTo(ScreenState.DrawingDetail(existing.id))
                onResult("اللوحة موجودة بالفعل في مرسمك!", true)
                return@launch
            }

            // Decode bitmap from base64
            val decodedBmp = com.example.data.FirebaseSyncManager.decodeBase64ToBitmap(fbDrawing.imageBase64)
            if (decodedBmp != null) {
                // Save bitmap local file
                val dir = File(context.filesDir, "drawings")
                if (!dir.exists()) dir.mkdirs()
                val fileName = "rasmatak_imported_${System.currentTimeMillis()}.png"
                val file = File(dir, fileName)
                try {
                    val out = java.io.FileOutputStream(file)
                    decodedBmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Insert to SQLite local database
                val entity = DrawingEntity(
                    title = fbDrawing.title,
                    description = fbDrawing.description,
                    drawingIdString = fbDrawing.drawingIdString,
                    imagePath = file.absolutePath,
                    isPublic = true,
                    authorName = fbDrawing.authorName,
                    likesCount = fbDrawing.likesCount,
                    likedByUsersJson = ",",
                    isLocalOriginal = false
                )
                val newId = repository.insertDrawing(entity).toInt()
                navigateTo(ScreenState.DrawingDetail(newId))
                onResult("تم استيراد اللوحة بنجاح ورؤيتها! 🎉", true)
            } else {
                onResult("فشل فك تشفير صورة اللوحة الفنية", false)
            }
        }
    }
}
