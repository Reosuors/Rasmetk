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

    data class PathAction(
        val path: Path,
        override val color: Color,
        override val strokeWidth: Float,
        override val isEraser: Boolean = false,
        override val layerId: String = "default",
        override val brushStyle: BrushStyle = BrushStyle.SOLID
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
        override val brushStyle: BrushStyle = BrushStyle.SOLID
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

    // Searching
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Public list or Private list in primary drawings gallery
    private val _isShowingPublicTabInGallery = MutableStateFlow(true)
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
        var list = baseList
        // Filter by tab
        list = if (isPublicTab) {
            list.filter { it.isPublic }
        } else {
            list.filter { it.isLocalOriginal }
        }
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
            val width = 1080
            val height = 1350 // High visual ratio
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
                    
                    // Draw path
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
            
            // Clear Canvas
            clearCanvas()

            onDone()
            // Navigate directly to Detail Screen of newly saved item
            navigateTo(ScreenState.DrawingDetail(newDbId))
        }
    }

    /**
     * Share drawing through system share sheets.
     */
    fun shareDrawing(drawing: DrawingEntity) {
        viewModelScope.launch {
            val file = File(drawing.imagePath)
            if (file.exists()) {
                val authority = "${context.packageName}.fileprovider"
                try {
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                    CanvasSaver.shareDrawing(context, uri, drawing.title, if (drawing.isPublic) drawing.drawingIdString else null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
