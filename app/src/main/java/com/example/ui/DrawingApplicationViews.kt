package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CommentEntity
import com.example.data.DrawingEntity
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainViewsContainer(viewModel: DrawingViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.screenState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val showProfanityDialog by viewModel.showProfanityErrorDialog.collectAsStateWithLifecycle()

    // Force RTL local layout direction for Arabic visual flow
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main Switcher
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        is ScreenState.Home -> {
                            HomeScreen(viewModel = viewModel, currentUsername = username)
                        }
                        is ScreenState.Canvas -> {
                            CanvasScreen(viewModel = viewModel)
                        }
                        is ScreenState.DrawingDetail -> {
                            DrawingDetailScreen(
                                viewModel = viewModel,
                                drawingId = screen.drawingId
                            )
                        }
                        is ScreenState.AuthorProfile -> {
                            AuthorProfileScreen(
                                viewModel = viewModel,
                                authorName = screen.authorName
                            )
                        }
                    }
                }

                // Global dialog for profanity warning
                if (showProfanityDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissProfanityDialog() },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "تنبيه",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "محتوى غير لائق!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Text(
                                text = "هذه كلمة سيئة، ممنوع ترسلها أو تستخدمها! يرجى الالتزام بالكلام الطيب والذوق العام لجعل مجتمعنا جميلاً.",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.dismissProfanityDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("حسنًا، أعتذر")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: DrawingViewModel, currentUsername: String) {
    val drawingsList by viewModel.filteredDrawings.collectAsStateWithLifecycle()
    val isPublicTab by viewModel.isShowingPublicTabInGallery.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val followedArtists by viewModel.followedArtists.collectAsStateWithLifecycle()

    var editingUsernameState by remember(currentUsername) { mutableStateOf(currentUsername) }
    var isEditingNameActive by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF0F0F0F))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Avatar circle
                        val firstLetter = if (currentUsername.isNotEmpty()) currentUsername.take(1).uppercase() else "A"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "أهلاً بك",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (isEditingNameActive) {
                                OutlinedTextField(
                                    value = editingUsernameState,
                                    onValueChange = { editingUsernameState = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6366F1),
                                        unfocusedBorderColor = Color(0xFF2A2A2A),
                                        focusedContainerColor = Color(0xFF1A1A1A),
                                        unfocusedContainerColor = Color(0xFF1A1A1A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color(0xFFE5E5E5)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("username_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (editingUsernameState.isNotBlank()) {
                                            viewModel.setUsername(editingUsernameState)
                                        }
                                        isEditingNameActive = false
                                        focusManager.clearFocus()
                                    })
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isEditingNameActive = true }
                                ) {
                                    Text(
                                        text = currentUsername.ifBlank { "أحمد الرسام" },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "تعديل الاسم",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    if (isEditingNameActive) {
                        IconButton(
                            onClick = {
                                if (editingUsernameState.isNotBlank()) {
                                    viewModel.setUsername(editingUsernameState)
                                }
                                isEditingNameActive = false
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "حفظ الاسم",
                                tint = Color(0xFF6366F1)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "رَسْمَتَك",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF6366F1)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
 
            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ابحث برقم الرسمة (ID) أو العنوان أو اسم الرسام...", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "مسح", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF2A2A2A),
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE5E5E5)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar"),
                singleLine = true
            )
 
            Spacer(modifier = Modifier.height(16.dp))
 
            // Two Action Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "ابدأ الرسم" (Start Drawing) with Gradient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                            )
                        )
                        .clickable { viewModel.navigateTo(ScreenState.Canvas) }
                        .testTag("sketch_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Brush,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ابدأ الرسم",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Gallery Toggler
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                        .clickable { viewModel.setGalleryTab(!isPublicTab) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (!isPublicTab) Color(0xFF6366F1).copy(alpha = 0.2f) else Color(0xFF2D2D2D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (!isPublicTab) Icons.Filled.Lock else Icons.Filled.Public,
                                contentDescription = null,
                                tint = if (!isPublicTab) Color(0xFF6366F1) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!isPublicTab) "رسوماتي الخاصة" else "المعرض العام",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
 
            // Tab toggles of Public Hub vs Client Drawings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabButton(
                    text = "رسمات عامة للمجتمع 🌍",
                    selected = isPublicTab,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setGalleryTab(true) }
                )
                TabButton(
                    text = "مرسمي الخاص 🔒",
                    selected = !isPublicTab,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setGalleryTab(false) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPublicTab) "معرض الإلهام العام" else "مجموعتي الفنية الخاصة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${drawingsList.size} رسمات معروضة",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Drawing Feed list
            if (drawingsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Landscape,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد لوحات فنية تطابق البحث!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPublicTab) "كن أول من يرسم لوحة وينشرها عامة لكل العالم!" else "لوحتك تظهر هنا عند حفظها وحمايتها كخاصة.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawings_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(items = drawingsList, key = { it.id }) { drawing ->
                        DrawingCard(
                            drawing = drawing,
                            viewModel = viewModel,
                            onClick = { viewModel.navigateTo(ScreenState.DrawingDetail(drawing.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) Color(0xFF6366F1) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF94A3B8),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun DrawingCard(drawing: DrawingEntity, viewModel: DrawingViewModel, onClick: () -> Unit) {
    val bitmap = rememberBitmap(drawing.imagePath)
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("drawing_item_${drawing.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Canvas View
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = drawing.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Brush, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = drawing.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // ID Badge
                    if (drawing.isPublic) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "معرف: ${drawing.drawingIdString}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E5E5),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Artist
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.navigateTo(ScreenState.AuthorProfile(drawing.authorName))
                    }
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "الرسام",
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFF6366F1)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "الرسام: ${drawing.authorName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6366F1),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }

                if (drawing.description.isNotEmpty()) {
                    Text(
                        text = drawing.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action bars like/comments
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "لايك",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${drawing.likesCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            contentDescription = "تعليق",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تفاعل",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Options menu (Three points)
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "خيارات الرسمة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    if (drawing.isPublic) {
                        DropdownMenuItem(
                            text = { Text("نسخ معرّف اللوحة (ID)") },
                            leadingIcon = { Icon(Icons.Filled.CopyAll, contentDescription = null) },
                            onClick = {
                                isMenuExpanded = false
                                copyToClipboard(context, drawing.drawingIdString)
                            }
                        )
                    }
                    if (drawing.isLocalOriginal) {
                        DropdownMenuItem(
                            text = { Text("تعديل معلومات اللوحة") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                isMenuExpanded = false
                                showEditDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف اللوحة نهائيًا") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            onClick = {
                                isMenuExpanded = false
                                viewModel.deleteDrawing(drawing.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue to Edit title and description
    if (showEditDialog) {
        var editTitleState by remember { mutableStateOf(drawing.title) }
        var editDescState by remember { mutableStateOf(drawing.description) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل معلومات اللوحة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitleState,
                        onValueChange = { editTitleState = it },
                        label = { Text("عنوان اللوحة *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDescState,
                        onValueChange = { editDescState = it },
                        label = { Text("تفاصيل ووصف") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editTitleState.isNotBlank()) {
                        viewModel.editDrawingInfo(drawing.id, editTitleState, editDescState)
                        showEditDialog = false
                    }
                }) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * 2. CANVAS SCREEN - Comprehensive Responsive Board
 */
@Composable
fun CanvasScreen(viewModel: DrawingViewModel) {
    val strokes = viewModel.canvasStrokes
    val redoStrokes = viewModel.canvasRedoStrokes
    
    val currentBrushColor by viewModel.brushColor
    val currentBrushSize by viewModel.brushSize
    val currentShapeType by viewModel.brushShapeType
    val currentIsEraser by viewModel.brushIsEraser

    var showSaveDetailsDialog by remember { mutableStateOf(false) }

    // Navigation back control
    BackHandler {
        viewModel.navigateBack()
    }

    var activeConfigTab by remember { mutableStateOf("brush") } // "brush", "layers", "mixer"
    var isMoveToolActive by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF131313))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222)))
                    .padding(12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brush Tab Button
                    Button(
                        onClick = { activeConfigTab = "brush" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeConfigTab == "brush") Color(0xFF6366F1) else Color(0xFF1F1F1F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🖌️ الفرشاة والمظهر", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    
                    // Layers Tab Button
                    Button(
                        onClick = { activeConfigTab = "layers" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeConfigTab == "layers") Color(0xFF6366F1) else Color(0xFF1F1F1F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🥞 الطبقات والتحريك", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    
                    // Color Mixer Tab Button
                    Button(
                        onClick = { activeConfigTab = "mixer" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeConfigTab == "mixer") Color(0xFF6366F1) else Color(0xFF1F1F1F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🧪 دمج ومزج الألوان", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                
                HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)
                
                // Render Panel based on activeConfigTab
                when (activeConfigTab) {
                    "brush" -> {
                        // 1. Brush Size Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentIsEraser) "حجم الممحاة" else "حجم الخط",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(70.dp)
                            )
                            Slider(
                                value = currentBrushSize,
                                onValueChange = { viewModel.brushSize.value = it },
                                valueRange = 2f..80f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF6366F1),
                                    activeTrackColor = Color(0xFF6366F1),
                                    inactiveTrackColor = Color(0xFF2A2A2A)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${currentBrushSize.toInt()}px",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // 1.5 Brush Styles Carousel Selection
                        val currentBrushStyle by viewModel.brushStyle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تأثير الفرشاة:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(70.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val styles = listOf(
                                    Pair(BrushStyle.SOLID, "قلم مصمت"),
                                    Pair(BrushStyle.DASHED, "فرشاة متقطعة"),
                                    Pair(BrushStyle.DOTTED, "تنقيط فني"),
                                    Pair(BrushStyle.AIRBRUSH, "رشاش ضبابي"),
                                    Pair(BrushStyle.NEON, "نيون مضيء")
                                )
                                items(styles.size) { index ->
                                    val (styleVal, styleName) = styles[index]
                                    val selected = currentBrushStyle == styleVal && !currentIsEraser
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) Color(0xFF6366F1) else Color(0xFF1E1E1E))
                                            .border(1.dp, if (selected) Color.White else Color(0xFF333333), RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.brushStyle.value = styleVal
                                                viewModel.brushIsEraser.value = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = styleName, color = if (selected) Color.White else Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2. Palette Colors Selection Row
                        if (!currentIsEraser) {
                            val colors = listOf(
                                Color.Black, Color.DarkGray, Color.Red,
                                Color(0xFFE65100), Color(0xFFFFEB3B), Color(0xFF4CAF50),
                                Color(0xFF2196F3), Color(0xFF3F51B5), Color(0xFF9C27B0),
                                Color(0xFFE91E63)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                colors.forEach { col ->
                                    val isSelected = currentBrushColor == col
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 30.dp else 24.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF6366F1) else Color(0xFF2A2A2A),
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.brushColor.value = col }
                                    )
                                }
                            }
                        }

                        // 3. Brush Shapes Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ToolButton(
                                icon = Icons.Filled.Gesture,
                                description = "حر",
                                selected = currentShapeType == ShapeType.FREEHAND && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.FREEHAND
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.HorizontalRule,
                                description = "مستقيم",
                                selected = currentShapeType == ShapeType.LINE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.LINE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Outlined.Circle,
                                description = "دائرة",
                                selected = currentShapeType == ShapeType.CIRCLE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.CIRCLE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Outlined.CropSquare,
                                description = "مستطيل",
                                selected = currentShapeType == ShapeType.RECTANGLE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.RECTANGLE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.TrendingFlat,
                                description = "سهم",
                                selected = currentShapeType == ShapeType.ARROW && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.ARROW
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.LayersClear,
                                description = "ممحاة",
                                selected = currentIsEraser,
                                onClick = {
                                    viewModel.brushIsEraser.value = true
                                }
                            )
                        }
                    }
                    
                    "layers" -> {
                        val layersList = viewModel.layers
                        val activeLayerIdVal by viewModel.activeLayerId
                        
                        // Header layer tools
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Translation tool toggle
                            Button(
                                onClick = { isMoveToolActive = !isMoveToolActive },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMoveToolActive) Color(0xFFEF4444) else Color(0xFF1E1E1E),
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (isMoveToolActive) Icons.Filled.OpenWith else Icons.Filled.PanTool,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isMoveToolActive) "تعطيل التحريك ❌" else "أداة تحريك الطبقة 🖐️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            // Add layer
                            Button(
                                onClick = { viewModel.addLayer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طبقة جديدة +", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (isMoveToolActive) {
                            Text(
                                text = "👉 أداة التحريك نشطة! اسحب بإصبعك على شاشة الرسم لتحريك الطبقة المحددة الآن.",
                                fontSize = 10.sp,
                                color = Color(0xFF818CF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        HorizontalDivider(color = Color(0xFF222222))
                        
                        // Layers List Column
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(layersList.size) { index ->
                                val layer = layersList[index]
                                val isSelected = layer.id == activeLayerIdVal
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1A1F36) else Color(0xFF181818))
                                        .border(1.dp, if (isSelected) Color(0xFF6366F1) else Color(0xFF2E2E2E), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectLayer(layer.id) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Eye hide
                                    IconButton(
                                        onClick = { viewModel.toggleLayerVisibility(layer.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null,
                                            tint = if (layer.isVisible) Color.White else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        var isRenaming by remember { mutableStateOf(false) }
                                        var rnText by remember { mutableStateOf(layer.name) }
                                        
                                        if (isRenaming) {
                                            TextField(
                                                value = rnText,
                                                onValueChange = { rnText = it },
                                                singleLine = true,
                                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    focusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    viewModel.renameLayer(layer.id, rnText)
                                                    isRenaming = false
                                                })
                                            )
                                        } else {
                                            Text(
                                                text = layer.name,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.clickable { isRenaming = true }
                                            )
                                        }
                                        
                                        // Opacity slider
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("شفافية:", color = Color.Gray, fontSize = 9.sp)
                                            Slider(
                                                value = layer.opacity,
                                                onValueChange = { viewModel.setLayerOpacity(layer.id, it) },
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Color(0xFF6366F1),
                                                    activeTrackColor = Color(0xFF6366F1),
                                                    inactiveTrackColor = Color(0xFF333333)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(15.dp)
                                            )
                                            Text("${(layer.opacity * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                        }
                                    }
                                    
                                    // Order controls and Delete
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { viewModel.moveLayerUp(layer.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                        
                                        IconButton(
                                            onClick = { viewModel.moveLayerDown(layer.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                        
                                        IconButton(
                                            onClick = { viewModel.removeLayer(layer.id) },
                                            enabled = layersList.size > 1,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    "mixer" -> {
                        var clrA by viewModel.mixerColorA
                        var clrB by viewModel.mixerColorB
                        var ratio by viewModel.mixerRatio
                        val mixedResult = viewModel.mixColorsResult()
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧪 دمج ومزج الألوان الفنية بطريقتك:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("اللون الأول", color = Color.Gray, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(clrA)
                                            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val slots = listOf(Color.Red, Color.Yellow, Color.Blue, Color.White, Color.Black)
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        slots.forEach { sClr ->
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(sClr)
                                                    .clickable { clrA = sClr }
                                            )
                                        }
                                    }
                                }
                                
                                Column(modifier = Modifier.weight(2f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("نسبة المزج: ${(ratio * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = ratio,
                                        onValueChange = { ratio = it },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF6366F1),
                                            activeTrackColor = Color(0xFF6366F1),
                                            inactiveTrackColor = Color(0xFF2A2A2A)
                                        )
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("اللون الثاني", color = Color.Gray, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(clrB)
                                            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val slots = listOf(Color.Green, Color.Magenta, Color.White, Color.Black, Color.Blue)
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        slots.forEach { sClr ->
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(sClr)
                                                    .clickable { clrB = sClr }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(mixedResult)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Text("اللون المزيج الناتج", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Button(
                                    onClick = {
                                        viewModel.applyMixedColorAsBrush()
                                        viewModel.brushIsEraser.value = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("اعتماد المزيج كفرشاة الرسم 🎨", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131313))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222)))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "تراجع", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مرسم الألوان الفني 🎨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Undo - Redo - Clear - Save
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.undoCanvas() },
                        enabled = strokes.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Filled.Undo,
                            contentDescription = "خطوة للخلف",
                            tint = if (strokes.isNotEmpty()) Color.White else Color(0xFF475569)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redoCanvas() },
                        enabled = redoStrokes.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Filled.Redo,
                            contentDescription = "خطوة للأمام",
                            tint = if (redoStrokes.isNotEmpty()) Color.White else Color(0xFF475569)
                        )
                    }
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "مسح اللوحة كاملة", tint = Color(0xFFEF4444))
                    }
                    Button(
                        onClick = { showSaveDetailsDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MAIN DRAWING CORE CANVAS
            var activePath by remember { mutableStateOf<Path?>(null) }
            var dragStartX by remember { mutableStateOf(0f) }
            var dragStartY by remember { mutableStateOf(0f) }
            var dragCurrentX by remember { mutableStateOf(0f) }
            var dragCurrentY by remember { mutableStateOf(0f) }
            var isDraggingActive by remember { mutableStateOf(false) }

            val activeLayerId by viewModel.activeLayerId
            val activeLayer = viewModel.getActiveLayer()
            val lOffX = activeLayer.offsetX
            val lOffY = activeLayer.offsetY

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .pointerInput(currentShapeType, currentBrushColor, currentBrushSize, currentIsEraser, activeLayerId, isMoveToolActive, lOffX, lOffY) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDraggingActive = true
                                if (isMoveToolActive) {
                                    dragStartX = offset.x
                                    dragStartY = offset.y
                                } else {
                                    val startXRel = offset.x - lOffX
                                    val startYRel = offset.y - lOffY
                                    dragStartX = startXRel
                                    dragStartY = startYRel
                                    dragCurrentX = startXRel
                                    dragCurrentY = startYRel

                                    if (currentShapeType == ShapeType.FREEHAND) {
                                        val newP = Path()
                                        newP.moveTo(startXRel, startYRel)
                                        activePath = newP
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (isMoveToolActive) {
                                    viewModel.updateLayerOffset(activeLayerId, dragAmount.x, dragAmount.y)
                                } else {
                                    val currXRel = change.position.x - lOffX
                                    val currYRel = change.position.y - lOffY
                                    dragCurrentX = currXRel
                                    dragCurrentY = currYRel

                                    if (currentShapeType == ShapeType.FREEHAND) {
                                        activePath?.lineTo(currXRel, currYRel)
                                        val pathState = activePath
                                        if (pathState != null) {
                                            activePath = null
                                            activePath = pathState
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                isDraggingActive = false
                                if (isMoveToolActive) {
                                    // Move finished
                                } else {
                                    val currentStyle = viewModel.brushStyle.value
                                    if (currentShapeType == ShapeType.FREEHAND) {
                                        val pathToSave = activePath
                                        if (pathToSave != null) {
                                            strokes.add(
                                                DrawingAction.PathAction(
                                                    path = pathToSave,
                                                    color = currentBrushColor,
                                                    strokeWidth = currentBrushSize,
                                                    isEraser = currentIsEraser,
                                                    layerId = activeLayerId,
                                                    brushStyle = currentStyle
                                                )
                                            )
                                            redoStrokes.clear()
                                        }
                                        activePath = null
                                    } else {
                                        strokes.add(
                                            DrawingAction.ShapeAction(
                                                shapeType = currentShapeType,
                                                startX = dragStartX,
                                                startY = dragStartY,
                                                endX = dragCurrentX,
                                                endY = dragCurrentY,
                                                color = currentBrushColor,
                                                strokeWidth = currentBrushSize,
                                                isEraser = currentIsEraser,
                                                layerId = activeLayerId,
                                                brushStyle = currentStyle
                                            )
                                        )
                                        redoStrokes.clear()
                                    }
                                }
                            }
                        )
                    }
                    .testTag("drawing_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val layersList = viewModel.layers
                    layersList.forEach { layer ->
                        if (layer.isVisible) {
                            drawContext.canvas.save()
                            drawContext.canvas.translate(layer.offsetX, layer.offsetY)
                            
                            val layerStrokes = strokes.filter { it.layerId == layer.id }
                            layerStrokes.forEach { action ->
                                val opMult = layer.opacity
                                val baseColor = if (action.isEraser) Color.White else action.color
                                
                                when (action) {
                                    is DrawingAction.PathAction -> {
                                        val effect = when (action.brushStyle) {
                                            BrushStyle.DASHED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(action.strokeWidth * 2f, action.strokeWidth * 2f), 0f)
                                            BrushStyle.DOTTED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(action.strokeWidth * 0.2f, action.strokeWidth * 2f), 0f)
                                            else -> null
                                        }
                                        
                                        when (action.brushStyle) {
                                            BrushStyle.SOLID, BrushStyle.DASHED, BrushStyle.DOTTED -> {
                                                drawPath(
                                                    path = action.path,
                                                    color = baseColor.copy(alpha = baseColor.alpha * opMult),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                                        pathEffect = effect
                                                    )
                                                )
                                            }
                                            BrushStyle.AIRBRUSH -> {
                                                drawPath(
                                                    path = action.path,
                                                    color = baseColor.copy(alpha = baseColor.alpha * opMult * 0.4f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                                drawPath(
                                                    path = action.path,
                                                    color = baseColor.copy(alpha = baseColor.alpha * opMult * 0.18f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth * 1.5f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                                drawPath(
                                                    path = action.path,
                                                    color = baseColor.copy(alpha = baseColor.alpha * opMult * 0.08f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth * 2.5f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                            BrushStyle.NEON -> {
                                                drawPath(
                                                    path = action.path,
                                                    color = baseColor.copy(alpha = baseColor.alpha * opMult * 0.35f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth * 2.2f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                                drawPath(
                                                    path = action.path,
                                                    color = Color.White,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = action.strokeWidth * 0.6f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    is DrawingAction.ShapeAction -> {
                                        val effect = when (action.brushStyle) {
                                            BrushStyle.DASHED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(action.strokeWidth * 2f, action.strokeWidth * 2f), 0f)
                                            BrushStyle.DOTTED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(action.strokeWidth * 0.2f, action.strokeWidth * 2f), 0f)
                                            else -> null
                                        }
                                        val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = action.strokeWidth,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                            pathEffect = effect
                                        )
                                        val strokeCol = baseColor.copy(alpha = baseColor.alpha * opMult)
                                        val sx = action.startX
                                        val sy = action.startY
                                        val ex = action.endX
                                        val ey = action.endY

                                        when (action.shapeType) {
                                            ShapeType.LINE -> {
                                                drawLine(
                                                    color = strokeCol,
                                                    start = Offset(sx, sy),
                                                    end = Offset(ex, ey),
                                                    strokeWidth = action.strokeWidth,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                    pathEffect = effect
                                                )
                                            }
                                            ShapeType.RECTANGLE -> {
                                                val left = minOf(sx, ex)
                                                val top = minOf(sy, ey)
                                                val right = maxOf(sx, ex)
                                                val bottom = maxOf(sy, ey)
                                                drawRect(
                                                    color = strokeCol,
                                                    topLeft = Offset(left, top),
                                                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                                    style = strokeStyle
                                                )
                                            }
                                            ShapeType.CIRCLE -> {
                                                val dx = ex - sx
                                                val dy = ey - sy
                                                val radius = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                                drawCircle(
                                                    color = strokeCol,
                                                    center = Offset(sx, sy),
                                                    radius = radius,
                                                    style = strokeStyle
                                                )
                                            }
                                            ShapeType.ARROW -> {
                                                drawLine(
                                                    color = strokeCol,
                                                    start = Offset(sx, sy),
                                                    end = Offset(ex, ey),
                                                    strokeWidth = action.strokeWidth,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                    pathEffect = effect
                                                )
                                                val angle = Math.atan2((ey - sy).toDouble(), (ex - sx).toDouble())
                                                val arrowSize = 25f
                                                val headPath = Path().apply {
                                                    moveTo(ex, ey)
                                                    lineTo(
                                                        (ex - arrowSize * Math.cos(angle - Math.PI / 6)).toFloat(),
                                                        (ey - arrowSize * Math.sin(angle - Math.PI / 6)).toFloat()
                                                    )
                                                    lineTo(
                                                        (ex - arrowSize * Math.cos(angle + Math.PI / 6)).toFloat(),
                                                        (ey - arrowSize * Math.sin(angle + Math.PI / 6)).toFloat()
                                                    )
                                                    close()
                                                }
                                                drawPath(
                                                    path = headPath,
                                                    color = strokeCol
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                            
                            if (isDraggingActive && !isMoveToolActive && activeLayerId == layer.id) {
                                val activeBrushColor = if (currentIsEraser) Color.White else currentBrushColor
                                val currentStyle = viewModel.brushStyle.value
                                val previewEffect = when (currentStyle) {
                                    BrushStyle.DASHED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(currentBrushSize * 2f, currentBrushSize * 2f), 0f)
                                    BrushStyle.DOTTED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(currentBrushSize * 0.2f, currentBrushSize * 2f), 0f)
                                    else -> null
                                }
                                
                                if (currentShapeType == ShapeType.FREEHAND) {
                                    activePath?.let { p ->
                                        when (currentStyle) {
                                            BrushStyle.SOLID, BrushStyle.DASHED, BrushStyle.DOTTED -> {
                                                drawPath(
                                                    path = p,
                                                    color = activeBrushColor,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = currentBrushSize,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                                        pathEffect = previewEffect
                                                    )
                                                )
                                            }
                                            BrushStyle.AIRBRUSH -> {
                                                drawPath(
                                                    path = p,
                                                    color = activeBrushColor.copy(alpha = 0.40f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = currentBrushSize,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                                drawPath(
                                                    path = p,
                                                    color = activeBrushColor.copy(alpha = 0.18f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = currentBrushSize * 1.5f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                            BrushStyle.NEON -> {
                                                drawPath(
                                                    path = p,
                                                    color = activeBrushColor.copy(alpha = 0.35f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = currentBrushSize * 2.2f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                                drawPath(
                                                    path = p,
                                                    color = Color.White,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = currentBrushSize * 0.6f,
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val previewStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = currentBrushSize,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                        pathEffect = previewEffect
                                    )
                                    when (currentShapeType) {
                                        ShapeType.LINE -> {
                                            drawLine(
                                                color = activeBrushColor,
                                                start = Offset(dragStartX, dragStartY),
                                                end = Offset(dragCurrentX, dragCurrentY),
                                                strokeWidth = currentBrushSize,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                pathEffect = previewEffect
                                            )
                                        }
                                        ShapeType.RECTANGLE -> {
                                            val left = minOf(dragStartX, dragCurrentX)
                                            val top = minOf(dragStartY, dragCurrentY)
                                            val right = maxOf(dragStartX, dragCurrentX)
                                            val bottom = maxOf(dragStartY, dragCurrentY)
                                            drawRect(
                                                color = activeBrushColor,
                                                topLeft = Offset(left, top),
                                                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                                style = previewStroke
                                            )
                                        }
                                        ShapeType.CIRCLE -> {
                                            val dx = dragCurrentX - dragStartX
                                            val dy = dragCurrentY - dragStartY
                                            val radius = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                            drawCircle(
                                                color = activeBrushColor,
                                                center = Offset(dragStartX, dragStartY),
                                                radius = radius,
                                                style = previewStroke
                                            )
                                        }
                                        ShapeType.ARROW -> {
                                            drawLine(
                                                color = activeBrushColor,
                                                start = Offset(dragStartX, dragStartY),
                                                end = Offset(dragCurrentX, dragCurrentY),
                                                strokeWidth = currentBrushSize,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                pathEffect = previewEffect
                                            )
                                            val angle = Math.atan2((dragCurrentY - dragStartY).toDouble(), (dragCurrentX - dragStartX).toDouble())
                                            val arrowSize = 25f
                                            val headPath = Path().apply {
                                                moveTo(dragCurrentX, dragCurrentY)
                                                lineTo(
                                                    (dragCurrentX - arrowSize * Math.cos(angle - Math.PI / 6)).toFloat(),
                                                    (dragCurrentY - arrowSize * Math.sin(angle - Math.PI / 6)).toFloat()
                                                )
                                                lineTo(
                                                    (dragCurrentX - arrowSize * Math.cos(angle + Math.PI / 6)).toFloat(),
                                                    (dragCurrentY - arrowSize * Math.sin(angle + Math.PI / 6)).toFloat()
                                                )
                                                close()
                                            }
                                            drawPath(path = headPath, color = activeBrushColor)
                                        }
                                        else -> {}
                                    }
                                }
                            }
                            
                            drawContext.canvas.restore()
                        }
                    }
                }

                // 🚀 شريط الأدوات العائم الذكي والحديث والمميز (Floating Glassmorphic Toolbar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .align(Alignment.TopCenter)
                        .pointerInput(Unit) {}
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xE8121214))
                            .border(
                                width = 1.5.dp,
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFFC084FC), Color(0xFF3B82F6))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. قلم رصاص (Pencil)
                        val isPencilActive = !currentIsEraser && 
                                currentShapeType == ShapeType.FREEHAND && 
                                viewModel.brushStyle.value == BrushStyle.SOLID
                        FloatingToolButton(
                            icon = Icons.Filled.Create,
                            label = "قلم رصاص",
                            isActive = isPencilActive,
                            onClick = {
                                viewModel.brushShapeType.value = ShapeType.FREEHAND
                                viewModel.brushStyle.value = BrushStyle.SOLID
                                viewModel.brushIsEraser.value = false
                            }
                        )

                        // 2. فرشاة رسم (Paintbrush)
                        val isBrushActive = !currentIsEraser && 
                                currentShapeType == ShapeType.FREEHAND && 
                                (viewModel.brushStyle.value == BrushStyle.AIRBRUSH || viewModel.brushStyle.value == BrushStyle.NEON)
                        FloatingToolButton(
                            icon = Icons.Filled.Brush,
                            label = "فرشاة رسم",
                            isActive = isBrushActive,
                            onClick = {
                                viewModel.brushShapeType.value = ShapeType.FREEHAND
                                if (viewModel.brushStyle.value != BrushStyle.AIRBRUSH && viewModel.brushStyle.value != BrushStyle.NEON) {
                                    viewModel.brushStyle.value = BrushStyle.AIRBRUSH
                                }
                                viewModel.brushIsEraser.value = false
                            }
                        )

                        // 3. ممحاة (Eraser)
                        val isEraserActive = currentIsEraser
                        FloatingToolButton(
                            icon = Icons.Filled.LayersClear,
                            label = "ممحاة دقيقة",
                            isActive = isEraserActive,
                            onClick = {
                                viewModel.brushIsEraser.value = true
                            }
                        )

                        // فاصل مرئي عمودي أنيق
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(Color(0xFF333333))
                        )

                        // 4. مستطيل (Rectangle)
                        val isRectActive = !currentIsEraser && currentShapeType == ShapeType.RECTANGLE
                        FloatingToolButton(
                            icon = Icons.Outlined.CropSquare,
                            label = "مستطيل",
                            isActive = isRectActive,
                            onClick = {
                                viewModel.brushShapeType.value = ShapeType.RECTANGLE
                                viewModel.brushIsEraser.value = false
                            }
                        )

                        // 5. دائرة (Circle)
                        val isCircleActive = !currentIsEraser && currentShapeType == ShapeType.CIRCLE
                        FloatingToolButton(
                            icon = Icons.Outlined.Circle,
                            label = "دائرة",
                            isActive = isCircleActive,
                            onClick = {
                                viewModel.brushShapeType.value = ShapeType.CIRCLE
                                viewModel.brushIsEraser.value = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Saving Dialog popup
    if (showSaveDetailsDialog) {
        var saveTitleState by remember { mutableStateOf("") }
        var saveDescState by remember { mutableStateOf("") }
        var isPublicState by remember { mutableStateOf(true) }

        var isFinalSavingLoading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isFinalSavingLoading) showSaveDetailsDialog = false
            },
            title = { Text("حفظ الرسمة 🎨", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل معلومات لوحتك لتسجيلها وحفظها في المعرض والكل يعرفها!")
                    
                    OutlinedTextField(
                        value = saveTitleState,
                        onValueChange = { saveTitleState = it },
                        label = { Text("عنوان الرسمة *") },
                        singleLine = true,
                        placeholder = { Text("مثال: منظر الغروب الهادئ") }
                    )

                    OutlinedTextField(
                        value = saveDescState,
                        onValueChange = { saveDescState = it },
                        label = { Text("وصف الرسمة") },
                        maxLines = 3,
                        placeholder = { Text("اكتب مشاعرك وما تجسده هذه اللوحة...") }
                    )

                    // Public vs Private
                    Text("خصوصية اللوحة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            onClick = { isPublicState = true },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPublicState) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isPublicState) 2.dp else 1.dp,
                                    color = if (isPublicState) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null)
                                Text("رسمة عامة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("يصدر لها كود ID ويبحث عنها الجميع", fontSize = 9.sp, textAlign = TextAlign.Center)
                            }
                        }

                        Card(
                            onClick = { isPublicState = false },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!isPublicState) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (!isPublicState) 2.dp else 1.dp,
                                    color = if (!isPublicState) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null)
                                Text("رسمة خاصة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("تظهر لك وحدك في مرسمك الخاص", fontSize = 9.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveTitleState.isNotBlank()) {
                            isFinalSavingLoading = true
                            viewModel.saveCanvasToApp(
                                title = saveTitleState,
                                description = saveDescState,
                                isPublic = isPublicState,
                                onDone = {
                                    isFinalSavingLoading = false
                                    showSaveDetailsDialog = false
                                }
                            )
                        }
                    },
                    enabled = saveTitleState.isNotBlank() && !isFinalSavingLoading
                ) {
                    if (isFinalSavingLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("تم، حفظ ونشر")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDetailsDialog = false },
                    enabled = !isFinalSavingLoading
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFF6366F1) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (selected) Color.White else Color(0xFF94A3B8)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

/**
 * 3. DETAIL VIEW SCREEN - Drawing details, likes, comments, and painter linkage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingDetailScreen(viewModel: DrawingViewModel, drawingId: Int) {
    val drawing by viewModel.activeDrawing.collectAsStateWithLifecycle()
    val comments by viewModel.activeComments.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val isFollowingAuthor by viewModel.isFollowingActiveAuthor.collectAsStateWithLifecycle()

    var newCommentState by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text(drawing?.title ?: "تفاصيل اللوحة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "تراجع", tint = Color.White)
                    }
                },
                actions = {
                    drawing?.let { draw ->
                        IconButton(onClick = { viewModel.shareDrawing(draw) }) {
                            Icon(Icons.Filled.Share, contentDescription = "مشاركة الرسمة", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        drawing?.let { draw ->
            val bitmap = rememberBitmap(draw.imagePath)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Expanded High Quality Image Container
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = draw.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Filled.Landscape,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.LightGray
                            )
                        }
                    }
                }

                // Stats and Action Rows (ID, Likes, Comments, Share)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = draw.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                            if (draw.isPublic) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { copyToClipboard(context, draw.drawingIdString) }
                                ) {
                                    Text(
                                        text = "رمز اللوحة: ${draw.drawingIdString}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "نسخ",
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "🔒 لوحة خاصة بفلتر حماية",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Likes Counter button
                        val myUser = username
                        val alreadyLikedByMe = draw.likedByUsersJson.contains(",$myUser,")
                        Button(
                            onClick = { viewModel.toggleLikeOnDrawing(draw) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (alreadyLikedByMe) Color(0xFFEF4444) else Color(0xFF1A1A1A),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (alreadyLikedByMe) Color.Transparent else Color(0xFF2A2A2A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (alreadyLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "لايك",
                                tint = if (alreadyLikedByMe) Color.White else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${draw.likesCount} إعجاب", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Description Block
                if (draw.description.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1A1A1A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "قصة اللوحة الفنية:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = draw.description, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Author Linkage Widget with dynamic Follow
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(ScreenState.AuthorProfile(draw.authorName)) }
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = draw.authorName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الرسام المبدع",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = draw.authorName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1),
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                    )
                                }
                            }

                            // Follow toggle
                            if (draw.authorName != username) {
                                Button(
                                    onClick = { viewModel.toggleFollowArtist(draw.authorName) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowingAuthor) Color(0xFF2D2D2D) else Color(0xFF6366F1),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFollowingAuthor) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isFollowingAuthor) "إلغاء المتابعة" else "متابعة الرسام", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Community Comments Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعليقات مجتمع المعجبين 💬",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF6366F1)
                        )
                        Text(
                            text = "${comments.size} تعليق",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Sticky Add comment block
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCommentState,
                            onValueChange = { newCommentState = it },
                            placeholder = { Text("أضف تعليقًا لطيفًا بأسلوبك الفني...", color = Color(0xFF64748B), fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF2A2A2A),
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE5E5E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newCommentState.isNotBlank()) {
                                    viewModel.addCommentToDrawing(draw.id, newCommentState)
                                    newCommentState = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("أرسل", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Render comments bubble lists
                if (comments.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF131313)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد تعليقات بعد! كن أول من يكتب إعجابه ويعلق للرسام.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(comments) { comment ->
                        CommentBubble(
                            comment = comment,
                            isMe = comment.authorName == username,
                            onAuthorClick = {
                                viewModel.navigateTo(ScreenState.AuthorProfile(comment.authorName))
                            }
                        )
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("جاري تحميل تفاصيل اللوحة...", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CommentBubble(comment: CommentEntity, isMe: Boolean, onAuthorClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) Color(0xFF6366F1) else Color(0xFF1A1A1A)
            ),
            border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isMe) "أنت (${comment.authorName})" else comment.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isMe) Color.White else Color(0xFF6366F1),
                        modifier = Modifier.clickable { onAuthorClick() }
                    )
                    Text(
                        text = "منذ قليل",
                        fontSize = 9.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color(0xFF94A3B8)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    fontSize = 13.sp,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 4. AUTHOR PROFILE SCREEN - View drawings and follows of a specific painter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(viewModel: DrawingViewModel, authorName: String) {
    val drawings by viewModel.activeAuthorDrawings.collectAsStateWithLifecycle()
    val isFollowingArtist by viewModel.isFollowingActiveAuthor.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()

    val context = LocalContext.current

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("ملف الرسام المبدع", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "تراجع", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Illustration avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = authorName.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = authorName,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color.White
            )

            // Dynamic badges
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A1A1A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                ) {
                    Text(
                        text = "${drawings.size} لوحة مرسومة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (isFollowingArtist) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF6366F1)
                    ) {
                        Text(
                            text = "تتابعه حاليًا",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Follow button at Profile level
            if (authorName != username) {
                Button(
                    onClick = { viewModel.toggleFollowArtist(authorName) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowingArtist) Color(0xFF2D2D2D) else Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isFollowingArtist) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFollowingArtist) "إلغاء المتابعة" else "متابعة أعماله",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFF222222))

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "استكشف لوحات الفن الخاصة بالرسام:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF6366F1)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Author's personal grid gallery
            if (drawings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد رسومات عامة مسجلة للرسام حاليًا.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(drawings) { draw ->
                        val thumbBitmap = rememberBitmap(draw.imagePath)
                        Card(
                            onClick = { viewModel.navigateTo(ScreenState.DrawingDetail(draw.id)) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(Color.White), // Keep clean white back of user drawings
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (thumbBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = thumbBitmap,
                                            contentDescription = draw.title,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Filled.Brush, contentDescription = null, tint = Color.LightGray)
                                    }
                                }
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = draw.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * HELPER UTILS
 */
@Composable
fun rememberBitmap(path: String): ImageBitmap? {
    return remember(path) {
        try {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.asImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("معرف الرسمة", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ معرّف اللوحة: $text", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 🎨 زر عائم مخصص يدعم الحركة والتأثيرات الحديثة وتمدد الحجم عند التنشيط (Pill Expansion)
 */
@Composable
fun FloatingToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val contentColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color(0xFF94A3B8),
        animationSpec = tween(150)
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isActive) {
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            
            AnimatedVisibility(
                visible = isActive,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
