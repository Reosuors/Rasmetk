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

fun getTxt(key: String, lang: String): String {
    return when (lang) {
        "en" -> when (key) {
            "app_name" -> "Rasmatak"
            "welcome" -> "Welcome"
            "search_hint" -> "Search in my drawings..."
            "personal_projects" -> "My Personal Projects"
            "no_projects" -> "No drawings match your search!"
            "no_projects_sub" -> "Click + to create your first drawing and start sketching."
            "drawings_count" -> "drawings"
            "create_new" -> "Create New Drawing"
            "drawing_name" -> "Drawing Name"
            "width_px" -> "Width (pixels)"
            "height_px" -> "Height (pixels)"
            "create" -> "Create"
            "cancel" -> "Cancel"
            "settings" -> "Settings"
            "change_lang" -> "App Language"
            "edit_username" -> "Edit Painter Name"
            "save_username" -> "Save Settings"
            "close" -> "Close"
            "canvas_title" -> "Art Studio"
            "brush_tab" -> "Brush & Style"
            "layers_tab" -> "Layers & Move"
            "mixer_tab" -> "Color Mixer"
            "brush_size" -> "Brush Size"
            "eraser_size" -> "Eraser Size"
            "pencil" -> "Pencil"
            "paintbrush" -> "Paintbrush"
            "eraser" -> "Eraser"
            "rectangle" -> "Rectangle"
            "circle" -> "Circle"
            "symmetry" -> "Symmetry"
            "no_symmetry" -> "No Symmetry"
            "vert_symmetry" -> "Vertical Symmetry"
            "horiz_symmetry" -> "Horizontal Symmetry"
            "radial_symmetry" -> "Radial Symmetry"
            "grid" -> "Grid"
            "grid_blank" -> "Blank Canvas"
            "grid_squares" -> "Square Grid"
            "grid_dots" -> "Dots Grid"
            "grid_isometric" -> "Isometric Grid"
            "undo" -> "Undo"
            "redo" -> "Redo"
            "clear" -> "Clear"
            "save" -> "Save"
            "zoom_in" -> "Zoom In"
            "zoom_out" -> "Zoom Out"
            "pan_canvas" -> "Pan Canvas"
            "reset_view" -> "Reset View"
            "active_tool" -> "Active Tool"
            "panning_mode" -> "Panning Mode Active"
            "save_drawing_title" -> "Save Drawing"
            "save_drawing_desc" -> "Enter your drawing details below"
            "drawing_title_label" -> "Drawing Title *"
            "drawing_desc_label" -> "Drawing Description"
            "title_placeholder" -> "e.g. Sunset view"
            "desc_placeholder" -> "Write your feelings..."
            "privacy_label" -> "Drawing settings"
            "privacy_private" -> "Local Save"
            "privacy_private_sub" -> "Saved in your personal portfolio"
            "save_confirm" -> "Save"
            "back" -> "Back"
            "detail_title" -> "Drawing Details"
            "drawing_id" -> "Drawing ID"
            "by_author" -> "By Painter"
            "profanity_title" -> "Inappropriate word!"
            "profanity_text" -> "This word contains profanity and is blocked."
            "ok" -> "OK"
            "zoom_pan_controls" -> "Zoom & Pan controls"
            else -> key
        }
        else -> when (key) { // "ar"
            "app_name" -> "رسمتك"
            "welcome" -> "أهلاً بك"
            "search_hint" -> "البحث في مشاريعي الخاصة..."
            "personal_projects" -> "مشاريعي الشخصية"
            "no_projects" -> "لا توجد لوحات فنية تطابق البحث!"
            "no_projects_sub" -> "اضغط على زر + لبدء لوحتك الأولى!"
            "drawings_count" -> "لوحات"
            "create_new" -> "إضافة رسمة جديدة"
            "drawing_name" -> "اسم الرسمة"
            "width_px" -> "العرض بالبكسل"
            "height_px" -> "الارتفاع بالبكسل"
            "create" -> "إنشاء"
            "cancel" -> "إلغاء"
            "settings" -> "الإعدادات"
            "change_lang" -> "لغة التطبيق"
            "edit_username" -> "تعديل اسم الرسام"
            "save_username" -> "حفظ الاسم"
            "close" -> "إغلاق"
            "canvas_title" -> "مرسم الرسم الفني"
            "brush_tab" -> "الفرشاة والمظهر"
            "layers_tab" -> "الطبقات والتحريك"
            "mixer_tab" -> "دمج ومزج الألوان"
            "brush_size" -> "حجم الخط"
            "eraser_size" -> "حجم الممحاة"
            "pencil" -> "قلم رصاص"
            "paintbrush" -> "فرشاة رسم"
            "eraser" -> "ممحاة"
            "rectangle" -> "مستطيل"
            "circle" -> "دائرة"
            "symmetry" -> "تماثل"
            "no_symmetry" -> "بدون تماثل"
            "vert_symmetry" -> "تماثل عمودي"
            "horiz_symmetry" -> "تماثل أفقي"
            "radial_symmetry" -> "تماثل شعاعي"
            "grid" -> "الشبكة"
            "grid_blank" -> "لوحة فارغة"
            "grid_squares" -> "شبكة مربعات"
            "grid_dots" -> "تنقيط هندسي"
            "grid_isometric" -> "أيزوميتريك"
            "undo" -> "تراجع"
            "redo" -> "إعادة"
            "clear" -> "مسح"
            "save" -> "حفظ"
            "zoom_in" -> "تقريب"
            "zoom_out" -> "تبعيد"
            "pan_canvas" -> "تحريك اللوحة"
            "reset_view" -> "إعادة الضبط"
            "active_tool" -> "الأداة النشطة"
            "panning_mode" -> "وضع تحريك اللوحة نشط"
            "save_drawing_title" -> "حفظ الرسمة"
            "save_drawing_desc" -> "أدخل معلومات لوحتك لحفظها"
            "drawing_title_label" -> "عنوان الرسمة *"
            "drawing_desc_label" -> "وصف الرسمة"
            "title_placeholder" -> "مثال: مظهر الغروب الهادئ"
            "desc_placeholder" -> "اكتب مشاعرك وما تجسده هذه اللوحة..."
            "privacy_label" -> "إعدادات خصوصية اللوحة"
            "privacy_private" -> "حفظ محلي"
            "privacy_private_sub" -> "تظهر لك في مشاريعك الخاصة"
            "save_confirm" -> "تم، حفظ"
            "back" -> "رجوع"
            "detail_title" -> "تفاصيل اللوحة"
            "drawing_id" -> "رقم اللوحة"
            "by_author" -> "بواسطة الرسام"
            "profanity_title" -> "محتوى غير لائق!"
            "profanity_text" -> "هذه الكلمة محظورة. يرجى استخدام كلمات لائقة لتسمية لوحتك."
            "ok" -> "موافق"
            "zoom_pan_controls" -> "تحكم التقريب والتحريك"
            else -> key
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainViewsContainer(viewModel: DrawingViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.screenState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val showProfanityDialog by viewModel.showProfanityErrorDialog.collectAsStateWithLifecycle()
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()

    val layoutDirection = if (currentLang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Firebase authentication & Shared Sync variables
    var showAuthDialog by remember { mutableStateOf(false) }
    var showPrivateCloudGallerySheet by remember { mutableStateOf(false) }
    var importCodeState by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }
    var importLoading by remember { mutableStateOf(false) }

    val isFirebaseLoggedIn by viewModel.isFirebaseLoggedIn.collectAsStateWithLifecycle()
    val firebaseEmail by viewModel.firebaseEmail.collectAsStateWithLifecycle()
    val privateCloudDrawings by viewModel.firebasePrivateDrawings.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_drawing_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = getTxt("create_new", currentLang)
                )
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF0F0F0F))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
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

                        Column {
                            Text(
                                text = getTxt("welcome", currentLang),
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUsername.ifBlank { "Unregistered" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = getTxt("settings", currentLang),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = getTxt("app_name", currentLang),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFF6366F1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar integrated beautifully in the title bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { 
                        Text(
                            text = getTxt("search_hint", currentLang), 
                            color = Color(0xFF94A3B8), 
                            fontSize = 12.sp
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Filled.Search, 
                            contentDescription = null, 
                            tint = Color(0xFF94A3B8)
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear, 
                                    contentDescription = null, 
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF2A2A2A),
                        focusedContainerColor = Color(0xFF161616),
                        unfocusedContainerColor = Color(0xFF161616),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE5E5E5)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_bar"),
                    singleLine = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Firebase Cloud Accounts & Sync Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("firebase_sync_card"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131313)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252528)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Cloud,
                                contentDescription = null,
                                tint = if (isFirebaseLoggedIn) Color(0xFF4ADE80) else Color(0xFF818CF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == "ar") "حساب ومزامنة رسمتك السحابية" else "Cloud Sync Accounts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        if (isFirebaseLoggedIn) {
                            TextButton(onClick = { viewModel.signOut() }) {
                                Text(
                                    text = if (currentLang == "ar") "خروج" else "Logout",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isFirebaseLoggedIn) {
                        Text(
                            text = if (currentLang == "ar") 
                                "سجّل حسابك مجاناً الآن لتستمتع بحفظ لوحاتك في معرض سحابي خاص بالكامل واستردادها في أي وقت!" 
                                else "Sign up free to auto-save and sync drawings inside your private Firebase gallery!",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                if (currentLang == "ar") "تسجيل الدخول / إنشاء حساب سحابي 🔒" else "Login / Create Cloud Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = if (currentLang == "ar") "أهلاً بك! الحساب النشط: $firebaseEmail" else "Welcome back! Account: $firebaseEmail",
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.syncPrivateGallery()
                                    showPrivateCloudGallerySheet = true 
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF312E81),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (currentLang == "ar") "معرضي الخاص السحابي 📁" else "My Private Gallery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.syncPrivateGallery() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E1B4B),
                                    contentColor = Color(0xFF818CF8)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Filled.Sync, contentDescription = "تحديث", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Import Shared Painting Area
            Card(
                modifier = Modifier.fillMaxWidth().testTag("import_drawing_card"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131313)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252528)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (currentLang == "ar") "استيراد لوحة برمز مشاركة سحابي 🔍" else "Import Painting with Sharing Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = importCodeState,
                            onValueChange = { 
                                importCodeState = it
                                importErrorMessage = null
                                importSuccessMessage = null
                            },
                            placeholder = { 
                                Text(
                                    text = if (currentLang == "ar") "ألصق الرابط أو الرمز (مثال: رسم-5832)" else "Paste code or link...",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                ) 
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF252528),
                                focusedContainerColor = Color(0xFF0F0F0F),
                                unfocusedContainerColor = Color(0xFF0F0F0F),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE5E5E5)
                            )
                        )

                        Button(
                            onClick = {
                                if (importCodeState.isNotBlank()) {
                                    importLoading = true
                                    importErrorMessage = null
                                    importSuccessMessage = null
                                    viewModel.importDrawingByCode(importCodeState) { msg, success ->
                                        importLoading = false
                                        if (success) {
                                            importSuccessMessage = msg
                                            importCodeState = ""
                                        } else {
                                            importErrorMessage = msg
                                        }
                                    }
                                }
                            },
                            enabled = importCodeState.isNotBlank() && !importLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            if (importLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(if (currentLang == "ar") "استيراد" else "Import", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (importErrorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(importErrorMessage!!, color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (importSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(importSuccessMessage!!, color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle of Personal Drawings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTxt("personal_projects", currentLang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${drawingsList.size} " + getTxt("drawings_count", currentLang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                            text = getTxt("no_projects", currentLang),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getTxt("no_projects_sub", currentLang),
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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

    // Settings Dialog Definition
    if (showSettingsDialog) {
        var tempName by remember { mutableStateOf(currentUsername) }
        var tempLang by remember { mutableStateOf(currentLang) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { 
                Text(
                    text = getTxt("settings", currentLang), 
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Painter User Name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = getTxt("edit_username", currentLang), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.LightGray
                        )
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Application Language choice
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = getTxt("change_lang", currentLang), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.LightGray
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { tempLang = "ar" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tempLang == "ar") Color(0xFF6366F1) else Color(0xFF1F1F1F)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("العربية", color = Color.White)
                            }

                            Button(
                                onClick = { tempLang = "en" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tempLang == "en") Color(0xFF6366F1) else Color(0xFF1F1F1F)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("English", color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.setUsername(tempName)
                        }
                        viewModel.setAppLanguage(tempLang)
                        showSettingsDialog = false
                    }
                ) {
                    Text(getTxt("save_username", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(getTxt("cancel", currentLang))
                }
            }
        )
    }

    // Create New Drawing Dialog with Custom Pixel Width and Height
    if (showCreateDialog) {
        var propName by remember { mutableStateOf("") }
        var propWidth by remember { mutableStateOf("1000") }
        var propHeight by remember { mutableStateOf("1000") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { 
                Text(
                    text = getTxt("create_new", currentLang), 
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = propName,
                        onValueChange = { propName = it },
                        label = { Text(getTxt("drawing_name", currentLang)) },
                        placeholder = { Text(getTxt("title_placeholder", currentLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = propWidth,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) propWidth = newValue
                        },
                        label = { Text(getTxt("width_px", currentLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = propHeight,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) propHeight = newValue
                        },
                        label = { Text(getTxt("height_px", currentLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalWidth = propWidth.toIntOrNull() ?: 1000
                        val finalHeight = propHeight.toIntOrNull() ?: 1000
                        val finalName = propName.ifBlank { 
                            if (currentLang == "ar") "لوحتي الجديدة" else "My New Drawing" 
                        }
                        viewModel.clearCanvas()
                        viewModel.currentDrawingTitle.value = finalName
                        viewModel.currentCanvasWidth.value = finalWidth
                        viewModel.currentCanvasHeight.value = finalHeight
                        viewModel.navigateTo(ScreenState.Canvas)
                        showCreateDialog = false
                    }
                ) {
                    Text(getTxt("create", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(getTxt("cancel", currentLang))
                }
            }
        )
    }

    // 1. Firebase Authentication Dialog
    if (showAuthDialog) {
        var isSignUpMode by remember { mutableStateOf(false) }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var painterInput by remember { mutableStateOf("") }

        val authLoading by viewModel.firebaseAuthLoading.collectAsStateWithLifecycle()
        val authError by viewModel.firebaseAuthError.collectAsStateWithLifecycle()

        val onDismissAuth = {
            showAuthDialog = false
            viewModel.clearAuthError()
            emailInput = ""
            passwordInput = ""
            painterInput = ""
        }

        AlertDialog(
            onDismissRequest = onDismissAuth,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF0F0F10),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (currentLang == "ar") "بوابة رسمتك السحابية" else "Rasmatak Cloud Portal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentLang == "ar") 
                            "سجِّل الدخول أو أنشئ حساباً لحفظ لوحاتك في السحاب واستردادها من أي جهاز ومشاركتها بروابط مباشرة!"
                            else "Sign in or register to auto-save and access your drawings from any device!",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )

                    // Sign Up / Login toggle buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B1B1E))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isSignUpMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSignUpMode) Color(0xFF6366F1) else Color.Transparent,
                                contentColor = if (!isSignUpMode) Color.White else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (currentLang == "ar") "تسجيل الدخول" else "Login", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { isSignUpMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUpMode) Color(0xFF6366F1) else Color.Transparent,
                                contentColor = if (isSignUpMode) Color.White else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (currentLang == "ar") "حساب جديد" else "Sign Up", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(if (currentLang == "ar") "بريد حسابك السحابي" else "Email Address", fontSize = 11.sp) },
                        placeholder = { Text("draw@example.com", fontSize = 10.sp, color = Color(0xFF4A4A4D)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE5E5E5),
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF2E2E32),
                            focusedContainerColor = Color(0xFF121214),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )

                    // Username (only for SignUp)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = painterInput,
                            onValueChange = { painterInput = it },
                            label = { Text(if (currentLang == "ar") "اسمك الفني (الرسام)" else "Painter Username", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Picasso", fontSize = 10.sp, color = Color(0xFF4A4A4D)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE5E5E5),
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0xFF2E2E32),
                                focusedContainerColor = Color(0xFF121214),
                                unfocusedContainerColor = Color(0xFF121214)
                            )
                        )
                    }

                    // Password Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(if (currentLang == "ar") "كلمة المرور" else "Password", fontSize = 11.sp) },
                        placeholder = { Text("••••••••", fontSize = 10.sp, color = Color(0xFF4A4A4D)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE5E5E5),
                            focusedBorderColor = Color(0xFF818CF8),
                            unfocusedBorderColor = Color(0xFF2E2E32),
                            focusedContainerColor = Color(0xFF121214),
                            unfocusedContainerColor = Color(0xFF121214)
                        )
                    )

                    authError?.let { err ->
                        Text(
                            text = err,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            if (isSignUpMode) {
                                val name = painterInput.ifBlank { "رسام سحابي" }
                                viewModel.signUp(emailInput, name, passwordInput) {
                                    onDismissAuth()
                                }
                            } else {
                                viewModel.login(emailInput, passwordInput) {
                                    onDismissAuth()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !authLoading && emailInput.isNotBlank() && passwordInput.isNotBlank()
                ) {
                    if (authLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isSignUpMode) {
                                if (currentLang == "ar") "إنشاء الحساب 🚀" else "Register"
                            } else {
                                if (currentLang == "ar") "تسجيل الدخول 🔓" else "Sign In"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAuth) {
                    Text(getTxt("cancel", currentLang), color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        )
    }

    // 2. Private Cloud Gallery Dialog
    if (showPrivateCloudGallerySheet) {
        val cloudLoadError by viewModel.firebasePrivateLoadError.collectAsStateWithLifecycle()
        val privateCloudLoading by viewModel.firebasePrivateLoading.collectAsStateWithLifecycle()

        var privateImportStatusMessage by remember { mutableStateOf<String?>(null) }
        var isPrivateImportSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPrivateCloudGallerySheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(16.dp).fillMaxWidth().widthIn(max = 480.dp).fillMaxHeight(0.85f).testTag("private_gallery_dialog"),
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF0F0F10),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "ar") "معرضي الخاص السحابي 📁" else "My Private Cloud Gallery",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.syncPrivateGallery() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "مزامنة",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (currentLang == "ar") 
                            "تُحفظ لوحاتك هنا سحابياً وتلقائياً. اضغط على أي لوحة لاستردادها وتحميلها فوراً في مرسمك المحلي!"
                            else "Your cloud-saved drawings. Click to restore / download any drawing instantly into your local studio!",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    privateImportStatusMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPrivateImportSuccess) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPrivateImportSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (privateCloudLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4ADE80))
                        }
                    } else if (cloudLoadError != null) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = cloudLoadError!!,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (privateCloudDrawings.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (currentLang == "ar") "معرضك السحابي فارغ حالياً!" else "Cloud gallery is empty!",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLang == "ar") 
                                        "قم برسم لوحاتك وحفظها لتتم مزامنتها تلقائياً بالخلفية." 
                                        else "Draw and save panels to auto-sync inside your cloud storage.",
                                    color = Color(0xFF475569),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(privateCloudDrawings) { cloudDrawing ->
                                val bmp = remember(cloudDrawing.imageBase64) {
                                    com.example.data.FirebaseSyncManager.decodeBase64ToBitmap(cloudDrawing.imageBase64)
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.importFirebaseDrawingDirectly(cloudDrawing) { resMsg, success ->
                                                privateImportStatusMessage = resMsg
                                                isPrivateImportSuccess = success
                                                if (success) {
                                                    showPrivateCloudGallerySheet = false
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF161619)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E32)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (bmp != null) {
                                                androidx.compose.foundation.Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = cloudDrawing.title,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Landscape,
                                                    contentDescription = null,
                                                    tint = Color.LightGray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cloudDrawing.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            if (cloudDrawing.description.isNotEmpty()) {
                                                Text(
                                                    text = cloudDrawing.description,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "ID: ${cloudDrawing.drawingIdString}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF818CF8),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.importFirebaseDrawingDirectly(cloudDrawing) { resMsg, success ->
                                                    privateImportStatusMessage = resMsg
                                                    isPrivateImportSuccess = success
                                                    if (success) {
                                                        showPrivateCloudGallerySheet = false
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Download,
                                                contentDescription = "تحميل",
                                                tint = Color(0xFF4ADE80),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivateCloudGallerySheet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E1B4B),
                        contentColor = Color(0xFF818CF8)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (currentLang == "ar") "إغلاق المعرض" else "Close Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
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
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showSaveDetailsDialog by remember { mutableStateOf(false) }

    // Navigation back control
    BackHandler {
        viewModel.navigateBack()
    }

    var activeConfigTab by remember { mutableStateOf("brush") } // "brush", "layers", "mixer"
    var isMoveToolActive by remember { mutableStateOf(false) }

    // Zoom and Pan states
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isHandToolActive by remember { mutableStateOf(false) }

    // Visibility toggles
    var isTopBarVisible by remember { mutableStateOf(true) }
    var isBottomConfigVisible by remember { mutableStateOf(true) }
    var isBottomConfigExpanded by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (isBottomConfigVisible) {
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
                        // Collapse / Expand toggle button (Three Lines Menu icon)
                        IconButton(
                            onClick = { isBottomConfigExpanded = !isBottomConfigExpanded },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Toggle Panel Height",
                                tint = if (isBottomConfigExpanded) Color(0xFF6366F1) else Color.White
                            )
                        }

                        // Brush Tab Button
                        Button(
                            onClick = { 
                                activeConfigTab = "brush" 
                                isBottomConfigExpanded = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeConfigTab == "brush" && isBottomConfigExpanded) Color(0xFF6366F1) else Color(0xFF1F1F1F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(getTxt("brush_tab", currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        
                        // Layers Tab Button
                        Button(
                            onClick = { 
                                activeConfigTab = "layers" 
                                isBottomConfigExpanded = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeConfigTab == "layers" && isBottomConfigExpanded) Color(0xFF6366F1) else Color(0xFF1F1F1F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(getTxt("layers_tab", currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        
                        // Color Mixer Tab Button
                        Button(
                            onClick = { 
                                activeConfigTab = "mixer" 
                                isBottomConfigExpanded = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeConfigTab == "mixer" && isBottomConfigExpanded) Color(0xFF6366F1) else Color(0xFF1F1F1F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(getTxt("mixer_tab", currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        // Close bottom panel button
                        IconButton(
                            onClick = { isBottomConfigVisible = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Panel",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                    
                    if (isBottomConfigExpanded) {
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
                                text = if (currentIsEraser) getTxt("eraser_size", currentLang) else getTxt("brush_size", currentLang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(80.dp)
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
                                text = if (currentLang == "ar") "تأثير الفرشاة:" else "Brush Effect:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(80.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val styles = if (currentLang == "ar") {
                                    listOf(
                                        Pair(BrushStyle.SOLID, "قلم مصمت"),
                                        Pair(BrushStyle.DASHED, "فرشاة متقطعة"),
                                        Pair(BrushStyle.DOTTED, "تنقيط فني"),
                                        Pair(BrushStyle.AIRBRUSH, "رشاش ضبابي"),
                                        Pair(BrushStyle.NEON, "نيون مضيء")
                                    )
                                } else {
                                    listOf(
                                        Pair(BrushStyle.SOLID, "Solid Pen"),
                                        Pair(BrushStyle.DASHED, "Dashed Brush"),
                                        Pair(BrushStyle.DOTTED, "Dotted Paint"),
                                        Pair(BrushStyle.AIRBRUSH, "Airbrush"),
                                        Pair(BrushStyle.NEON, "Neon Glow")
                                    )
                                }
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
                                description = if (currentLang == "ar") "حر" else "Free",
                                selected = currentShapeType == ShapeType.FREEHAND && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.FREEHAND
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.HorizontalRule,
                                description = if (currentLang == "ar") "مستقيم" else "Line",
                                selected = currentShapeType == ShapeType.LINE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.LINE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Outlined.Circle,
                                description = if (currentLang == "ar") "دائرة" else "Circle",
                                selected = currentShapeType == ShapeType.CIRCLE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.CIRCLE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Outlined.CropSquare,
                                description = if (currentLang == "ar") "مستطيل" else "Rectangle",
                                selected = currentShapeType == ShapeType.RECTANGLE && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.RECTANGLE
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.TrendingFlat,
                                description = if (currentLang == "ar") "سهم" else "Arrow",
                                selected = currentShapeType == ShapeType.ARROW && !currentIsEraser,
                                onClick = {
                                    viewModel.brushShapeType.value = ShapeType.ARROW
                                    viewModel.brushIsEraser.value = false
                                }
                            )
                            ToolButton(
                                icon = Icons.Filled.LayersClear,
                                description = if (currentLang == "ar") "ممحاة" else "Eraser",
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
                                Text(
                                    text = if (isMoveToolActive) {
                                        if (currentLang == "ar") "تعطيل التحريك" else "Disable Move"
                                    } else {
                                        if (currentLang == "ar") "تحريك الطبقة" else "Move Layer"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Add layer
                            Button(
                                onClick = { viewModel.addLayer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == "ar") "طبقة جديدة" else "New Layer",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (isMoveToolActive) {
                            Text(
                                text = if (currentLang == "ar") {
                                    "أداة تحريك الطبقة نشطة! اسحب بإصبعك على شاشة الرسم لتحريك الطبقة المحددة."
                                } else {
                                    "Layer move tool is active! Drag on the canvas to move the selected layer."
                                },
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
                                            Text(
                                                text = if (currentLang == "ar") "الشفافية:" else "Opacity:",
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
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
                            Text(
                                text = if (currentLang == "ar") {
                                    "مزج ودمج الألوان الفنية بطريقتك:"
                                } else {
                                    "Custom Color Mixing & Palette:"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == "ar") "اللون الأول" else "Color 1",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
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
                                    Text(
                                        text = if (currentLang == "ar") {
                                            "نسبة المزج: ${(ratio * 100).toInt()}%"
                                        } else {
                                            "Mix Ratio: ${(ratio * 100).toInt()}%"
                                        },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
                                    Text(
                                        text = if (currentLang == "ar") "اللون الثاني" else "Color 2",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
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
                                        Text(
                                            text = if (currentLang == "ar") "اللون الناتج" else "Mixed Result",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
                                    Text(
                                        text = if (currentLang == "ar") "اعتماد المزيج كفرشاة الرسم" else "Apply to Brush",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } } ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar
            if (isTopBarVisible) {
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
                            Icon(Icons.Filled.ArrowBack, contentDescription = getTxt("back", currentLang), tint = Color.White)
                        }
                        
                        // Hide Top Bar button (Three Lines Menu icon)
                        IconButton(onClick = { isTopBarVisible = false }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Hide Top Bar",
                                tint = Color.LightGray
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTxt("canvas_title", currentLang),
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
                                contentDescription = getTxt("undo", currentLang),
                                tint = if (strokes.isNotEmpty()) Color.White else Color(0xFF475569)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redoCanvas() },
                            enabled = redoStrokes.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Filled.Redo,
                                contentDescription = getTxt("redo", currentLang),
                                tint = if (redoStrokes.isNotEmpty()) Color.White else Color(0xFF475569)
                            )
                        }
                        IconButton(onClick = { viewModel.clearCanvas() }) {
                            Icon(Icons.Filled.Delete, contentDescription = getTxt("clear", currentLang), tint = Color(0xFFEF4444))
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
                            Text(getTxt("save", currentLang), fontWeight = FontWeight.Bold)
                        }
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
                    .pointerInput(
                        currentShapeType,
                        currentBrushColor,
                        currentBrushSize,
                        currentIsEraser,
                        activeLayerId,
                        isMoveToolActive,
                        isHandToolActive,
                        zoomScale,
                        panOffset,
                        lOffX,
                        lOffY
                    ) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (isHandToolActive) {
                                    // Panning state
                                } else {
                                    isDraggingActive = true
                                    if (isMoveToolActive) {
                                        dragStartX = offset.x
                                        dragStartY = offset.y
                                    } else {
                                        val startXRel = (offset.x - panOffset.x) / zoomScale - lOffX
                                        val startYRel = (offset.y - panOffset.y) / zoomScale - lOffY
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
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (isHandToolActive) {
                                    panOffset = panOffset + dragAmount
                                } else if (isMoveToolActive) {
                                    viewModel.updateLayerOffset(activeLayerId, dragAmount.x / zoomScale, dragAmount.y / zoomScale)
                                } else {
                                    val currXRel = (change.position.x - panOffset.x) / zoomScale - lOffX
                                    val currYRel = (change.position.y - panOffset.y) / zoomScale - lOffY
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
                                if (isHandToolActive || isMoveToolActive) {
                                    // Move/Pan finished
                                } else {
                                    val currentStyle = viewModel.brushStyle.value
                                    val currentSymmetry = viewModel.symmetryMode.value
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
                                                    brushStyle = currentStyle,
                                                    symmetryMode = currentSymmetry
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
                                                brushStyle = currentStyle,
                                                symmetryMode = currentSymmetry
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
                    drawContext.canvas.save()
                    drawContext.canvas.translate(panOffset.x, panOffset.y)
                    drawContext.canvas.scale(zoomScale, zoomScale)

                    // 1. Draw custom background grid pattern (GRID, DOTS, ISOMETRIC)
                    val gridStyle = viewModel.canvasGridStyle.value
                    if (gridStyle != CanvasGridStyle.BLANK) {
                        val gridColor = Color(0x2894A3B8) // soft slate grid color
                        val dotRadius = 1.5.dp.toPx()
                        val spacing = 40.dp.toPx()
                        
                        when (gridStyle) {
                            CanvasGridStyle.GRID -> {
                                // Draw horizontal lines
                                var y = 0f
                                while (y < size.height) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1f
                                    )
                                    y += spacing
                                }
                                // Draw vertical lines
                                var x = 0f
                                while (x < size.width) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 1f
                                    )
                                    x += spacing
                                }
                            }
                            CanvasGridStyle.DOTS -> {
                                var x = spacing / 2f
                                while (x < size.width) {
                                    var y = spacing / 2f
                                    while (y < size.height) {
                                        drawCircle(
                                            color = gridColor,
                                            radius = dotRadius,
                                            center = Offset(x, y)
                                        )
                                        y += spacing
                                    }
                                    x += spacing
                                }
                            }
                            CanvasGridStyle.ISOMETRIC -> {
                                val hSpacing = spacing * 1.5f
                                var x = 0f
                                while (x < size.width) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 1f
                                    )
                                    x += hSpacing
                                }
                                val diagonalSpacing = hSpacing
                                var y = -size.width
                                while (y < size.height + size.width) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y + size.width * 0.577f),
                                        strokeWidth = 1f
                                    )
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y - size.width * 0.577f),
                                        strokeWidth = 1f
                                    )
                                    y += diagonalSpacing
                                }
                            }
                            else -> {}
                        }
                    }

                    val layersList = viewModel.layers
                    layersList.forEach { layer ->
                        if (layer.isVisible) {
                            drawContext.canvas.save()
                            drawContext.canvas.translate(layer.offsetX, layer.offsetY)
                            
                            val layerStrokes = strokes.filter { it.layerId == layer.id }
                            layerStrokes.forEach { action ->
                                val opMult = layer.opacity
                                val baseColor = if (action.isEraser) Color.White else action.color
                                
                                fun drawActionCore() {
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
                                
                                drawActionCore()
                                
                                val cX = size.width / 2f
                                val cY = size.height / 2f
                                if (action.symmetryMode == SymmetryMode.VERTICAL || action.symmetryMode == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(-1f, 1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActionCore()
                                    drawContext.canvas.restore()
                                }
                                if (action.symmetryMode == SymmetryMode.HORIZONTAL || action.symmetryMode == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(1f, -1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActionCore()
                                    drawContext.canvas.restore()
                                }
                                if (action.symmetryMode == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(-1f, -1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActionCore()
                                    drawContext.canvas.restore()
                                }
                            }
                            
                            if (isDraggingActive && !isMoveToolActive && activeLayerId == layer.id) {
                                val activeBrushColor = if (currentIsEraser) Color.White else currentBrushColor
                                val currentStyle = viewModel.brushStyle.value
                                val previewSymmetry = viewModel.symmetryMode.value
                                val previewEffect = when (currentStyle) {
                                    BrushStyle.DASHED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(currentBrushSize * 2f, currentBrushSize * 2f), 0f)
                                    BrushStyle.DOTTED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(currentBrushSize * 0.2f, currentBrushSize * 2f), 0f)
                                    else -> null
                                }
                                
                                fun drawActivePreviewCore() {
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
                                
                                drawActivePreviewCore()
                                
                                val cX = size.width / 2f
                                val cY = size.height / 2f
                                if (previewSymmetry == SymmetryMode.VERTICAL || previewSymmetry == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(-1f, 1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActivePreviewCore()
                                    drawContext.canvas.restore()
                                }
                                if (previewSymmetry == SymmetryMode.HORIZONTAL || previewSymmetry == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(1f, -1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActivePreviewCore()
                                    drawContext.canvas.restore()
                                }
                                if (previewSymmetry == SymmetryMode.RADIAL) {
                                    drawContext.canvas.save()
                                    drawContext.canvas.translate(cX, cY)
                                    drawContext.canvas.scale(-1f, -1f)
                                    drawContext.canvas.translate(-cX, -cY)
                                    drawActivePreviewCore()
                                    drawContext.canvas.restore()
                                }
                            }
                            
                            drawContext.canvas.restore()
                        }
                    }

                    // 4. Draw Symmetry Axis guideline indicators as overlay
                    val actSymmetry = viewModel.symmetryMode.value
                    if (actSymmetry != SymmetryMode.NONE) {
                        val symLineColor = Color(0x6A6366F1) // Indigo primary with nice premium opacity
                        val cX = size.width / 2f
                        val cY = size.height / 2f
                        val dashEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        
                        if (actSymmetry == SymmetryMode.VERTICAL || actSymmetry == SymmetryMode.RADIAL) {
                            drawLine(
                                color = symLineColor,
                                start = Offset(cX, 0f),
                                end = Offset(cX, size.height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                        if (actSymmetry == SymmetryMode.HORIZONTAL || actSymmetry == SymmetryMode.RADIAL) {
                            drawLine(
                                color = symLineColor,
                                start = Offset(0f, cY),
                                end = Offset(size.width, cY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                    }
                    drawContext.canvas.restore()
                }

                // Interactive Floating Toolbar Box overlay
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
                        // 1. قلم رصاص (Pencil / Solid)
                        val isPencilActive = !currentIsEraser && 
                                currentShapeType == ShapeType.FREEHAND && 
                                viewModel.brushStyle.value == BrushStyle.SOLID
                        FloatingToolButton(
                            icon = Icons.Filled.Create,
                            label = if (currentLang == "ar") "قلم رصاص" else "Pencil",
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
                            label = if (currentLang == "ar") "فرشاة رسم" else "Brush",
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
                            label = if (currentLang == "ar") "ممحاة دقيقة" else "Precision Eraser",
                            isActive = isEraserActive,
                            onClick = {
                                viewModel.brushIsEraser.value = true
                            }
                        )

                        // Visual separator
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
                            label = if (currentLang == "ar") "مستطيل" else "Rectangle",
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
                            label = if (currentLang == "ar") "دائرة" else "Circle",
                            isActive = isCircleActive,
                            onClick = {
                                viewModel.brushShapeType.value = ShapeType.CIRCLE
                                viewModel.brushIsEraser.value = false
                            }
                        )

                        // Visual separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(Color(0xFF333333))
                        )

                        // 6. تماثل ذكي
                        var showSymmetryMenu by remember { mutableStateOf(false) }
                        val activeSymmetry = viewModel.symmetryMode.value
                        Box {
                            FloatingToolButton(
                                icon = Icons.Filled.Flip,
                                label = if (currentLang == "ar") "تماثل" else "Symmetry",
                                isActive = activeSymmetry != SymmetryMode.NONE,
                                onClick = { showSymmetryMenu = true }
                            )
                            DropdownMenu(
                                expanded = showSymmetryMenu,
                                onDismissRequest = { showSymmetryMenu = false },
                                modifier = Modifier
                                    .background(Color(0xE8121214))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "بدون تماثل" else "No Symmetry", color = if (activeSymmetry == SymmetryMode.NONE) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.symmetryMode.value = SymmetryMode.NONE
                                        showSymmetryMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "تماثل عمودي" else "Vertical Symmetry", color = if (activeSymmetry == SymmetryMode.VERTICAL) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.symmetryMode.value = SymmetryMode.VERTICAL
                                        showSymmetryMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "تماثل أفقي" else "Horizontal Symmetry", color = if (activeSymmetry == SymmetryMode.HORIZONTAL) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.symmetryMode.value = SymmetryMode.HORIZONTAL
                                        showSymmetryMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "تماثل شعاعي" else "Radial Symmetry", color = if (activeSymmetry == SymmetryMode.RADIAL) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.symmetryMode.value = SymmetryMode.RADIAL
                                        showSymmetryMenu = false
                                    }
                                )
                            }
                        }

                        // 7. شبكة فنية
                        var showGridMenu by remember { mutableStateOf(false) }
                        val activeGrid = viewModel.canvasGridStyle.value
                        Box {
                            FloatingToolButton(
                                icon = Icons.Filled.GridOn,
                                label = if (currentLang == "ar") "الشبكة" else "Grid",
                                isActive = activeGrid != CanvasGridStyle.BLANK,
                                onClick = { showGridMenu = true }
                            )
                            DropdownMenu(
                                expanded = showGridMenu,
                                onDismissRequest = { showGridMenu = false },
                                modifier = Modifier
                                    .background(Color(0xE8121214))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "لوحة فارغة" else "Blank Board", color = if (activeGrid == CanvasGridStyle.BLANK) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.canvasGridStyle.value = CanvasGridStyle.BLANK
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "شبكة مربعات" else "Square Grid", color = if (activeGrid == CanvasGridStyle.GRID) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.canvasGridStyle.value = CanvasGridStyle.GRID
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "تنقيط هندسي" else "Dotted Grid", color = if (activeGrid == CanvasGridStyle.DOTS) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.canvasGridStyle.value = CanvasGridStyle.DOTS
                                        showGridMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "ar") "أيزوميتريك" else "Isometric Geometric", color = if (activeGrid == CanvasGridStyle.ISOMETRIC) Color(0xFF6366F1) else Color.White) },
                                    onClick = {
                                        viewModel.canvasGridStyle.value = CanvasGridStyle.ISOMETRIC
                                        showGridMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Floating Zoom & View Controls Overlay Column (Right-Centered)
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 80.dp) // clear of the floating horizontal toolbar
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xE8121214))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zoom In button
                    IconButton(
                        onClick = { zoomScale = minOf(6f, zoomScale * 1.2f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }

                    // Zoom indication text
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Zoom Out button
                    IconButton(
                        onClick = { zoomScale = maxOf(0.4f, zoomScale / 1.2f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }

                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color(0xFF333333)))

                    // Hand Tool (Pan/Draw Toggle)
                    IconButton(
                        onClick = { isHandToolActive = !isHandToolActive },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isHandToolActive) Color(0xFF6366F1) else Color(0xFF222222), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isHandToolActive) Icons.Filled.PanTool else Icons.Filled.Gesture,
                            contentDescription = "Toggle Hand Pan",
                            tint = Color.White
                        )
                    }

                    // Reset Pan Zoom view
                    IconButton(
                        onClick = { 
                            zoomScale = 1f
                            panOffset = Offset.Zero
                            isHandToolActive = false
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset View", tint = Color.White)
                    }

                    // Toggle All UI / Restore bottom config & top bar
                    if (!isTopBarVisible || !isBottomConfigVisible) {
                        Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color(0xFF333333)))
                        IconButton(
                            onClick = { 
                                isTopBarVisible = true
                                isBottomConfigVisible = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        ) {
                            Icon(Icons.Filled.Visibility, contentDescription = "Show Menus", tint = Color.White)
                        }
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
            title = { Text(if (currentLang == "ar") "حفظ الرسمة" else "Save Drawing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (currentLang == "ar") "أدخل معلومات لوحتك لتسجيلها وحفظها في مرسمك الخاص!" else "Enter your drawing details to record and save it to your studio!")
                    
                    OutlinedTextField(
                        value = saveTitleState,
                        onValueChange = { saveTitleState = it },
                        label = { Text(if (currentLang == "ar") "عنوان الرسمة *" else "Drawing Title *") },
                        singleLine = true,
                        placeholder = { Text(if (currentLang == "ar") "مثال: منظر الغروب الهادئ" else "e.g., Calm Sunset Scene") }
                    )

                    OutlinedTextField(
                        value = saveDescState,
                        onValueChange = { saveDescState = it },
                        label = { Text(if (currentLang == "ar") "وصف الرسمة" else "Description") },
                        maxLines = 3,
                        placeholder = { Text(if (currentLang == "ar") "اكتب مشاعرك وما تجسده هذه اللوحة..." else "What does this drawing express...") }
                    )

                    // Public vs Private
                    Text(if (currentLang == "ar") "خصوصية اللوحة:" else "Drawing Privacy:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                Text(if (currentLang == "ar") "رسمة عامة" else "Public", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(if (currentLang == "ar") "يصدر لها كود ID ويبحث عنها الجميع" else "Discoverable with a unique share code", fontSize = 9.sp, textAlign = TextAlign.Center)
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
                                Text(if (currentLang == "ar") "رسمة خاصة" else "Private", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(if (currentLang == "ar") "تظهر لك وحدك في مرسمك الخاص" else "Only visible in your personal studio", fontSize = 9.sp, textAlign = TextAlign.Center)
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
                        Text(if (currentLang == "ar") "تم، حفظ الرسم" else "Done, Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDetailsDialog = false },
                    enabled = !isFinalSavingLoading
                ) {
                    Text(if (currentLang == "ar") "إلغاء" else "Cancel")
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
                        IconButton(onClick = {
                            viewModel.downloadDrawingToDevice(draw) { success ->
                                if (success) {
                                    Toast.makeText(context, "تم حفظ اللوحة الفنية بنجاح في معرض الصور! 🌠🎨", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "فشل حفظ اللوحة الفنية. تأكد من الأذونات.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "تحميل الرسمة للجهاز", tint = Color.White)
                        }
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
