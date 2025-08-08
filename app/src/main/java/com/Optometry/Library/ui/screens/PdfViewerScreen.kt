package com.Optometry.Library.ui.screens

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.Optometry.Library.Utils.DownloadManager
import com.Optometry.Library.navigation.Screen
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import android.content.SharedPreferences
import android.content.Context
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    navController: NavController,
    bookTitle: String? = null,
    pageNumber: Int = 0
) {
    val context = LocalContext.current
    val downloadManager = remember { DownloadManager(context) }
    val downloadedBooks by downloadManager.downloadedBooks.collectAsState()
    
    // State for floating action button
    var isFabExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fab_rotation"
    )
    
    // State for read mode
    var isReadMode by remember { mutableStateOf(false) }
    var showReadModeBanner by remember { mutableStateOf(false) }
    var originalBrightness: Float? by remember { mutableStateOf(null) }
    
    // State for fullscreen mode
    var isFullscreen by remember { mutableStateOf(false) }
    // When read mode toggles ON, show banner for 5 seconds; hide when OFF
    LaunchedEffect(isReadMode) {
        if (isReadMode) {
            showReadModeBanner = true
            delay(5000)
            showReadModeBanner = false
        } else {
            showReadModeBanner = false
        }
    }

    // Simulate "eye care mode" by warming screen and dimming brightness while read mode is active
    DisposableEffect(isReadMode) {
        val activity = context as? Activity
        if (activity != null) {
            val lp = activity.window.attributes
            if (isReadMode) {
                if (originalBrightness == null) originalBrightness = lp.screenBrightness
                lp.screenBrightness = 0.6f // slightly dim
                activity.window.attributes = lp
            } else {
                originalBrightness?.let { prev ->
                    lp.screenBrightness = prev
                    activity.window.attributes = lp
                }
            }
        }
        onDispose {
            val activityOnDispose = context as? Activity
            if (activityOnDispose != null) {
                val lp2 = activityOnDispose.window.attributes
                originalBrightness?.let { prev ->
                    lp2.screenBrightness = prev
                    activityOnDispose.window.attributes = lp2
                }
            }
        }
    }
    
    // State for PDF loading
    var isPdfLoading by remember { mutableStateOf(true) }
    var pdfError by remember { mutableStateOf<String?>(null) }
    var pdfLoaded by remember { mutableStateOf(false) }
    var useFallbackViewer by remember { mutableStateOf(false) }
    
    // State for current page
    var currentPage by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    
    // SharedPreferences for bookmarks, notes, and page persistence
    val bookmarksPrefs = remember { context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE) }
    val notesPrefs = remember { context.getSharedPreferences("notes", Context.MODE_PRIVATE) }
    val pagePrefs = remember { context.getSharedPreferences("page_persistence", Context.MODE_PRIVATE) }
    
    // Load saved page for this book
    LaunchedEffect(bookTitle) {
        bookTitle?.let { title ->
            val savedPage = pagePrefs.getInt(title, 0)
            currentPage = savedPage
        }
    }
    
    // Check if book is downloaded
    val isBookDownloaded = bookTitle?.let { downloadedBooks.contains(it) } ?: false
    
    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = { 
                        Text(
                            text = bookTitle ?: "PDF Viewer",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isFullscreen = !isFullscreen },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen, 
                                contentDescription = "Fullscreen",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expanded FAB options
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bookmark Me option
                        FloatingActionButton(
                            onClick = {
                                android.util.Log.d("PdfViewer", "Bookmark Me clicked for: $bookTitle at page $currentPage")
                                // Save bookmark with page number
                                val bookmarkKey = "${bookTitle}_page_$currentPage"
                                bookmarksPrefs.edit().putString(bookmarkKey, "$bookTitle - Page $currentPage").apply()
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.BookmarkAdd, 
                                contentDescription = "Add Bookmark",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // My Bookmarks option
                        FloatingActionButton(
                            onClick = {
                                android.util.Log.d("PdfViewer", "My Bookmarks clicked")
                                navController.navigate(Screen.Bookmarks.route)
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Bookmark, 
                                contentDescription = "My Bookmarks",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // My Notes option
                        FloatingActionButton(
                            onClick = {
                                android.util.Log.d("PdfViewer", "My Notes clicked")
                                navController.navigate(Screen.Notes.route)
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Note, 
                                contentDescription = "My Notes",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Read Mode option
                        FloatingActionButton(
                            onClick = {
                                isReadMode = !isReadMode
                                android.util.Log.d("PdfViewer", "Read Mode: ${if (isReadMode) "ON" else "OFF"}")
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = if (isReadMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isReadMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.MenuBook, 
                                contentDescription = "Read Mode",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Main FAB
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "More Options",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(rotationState)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isFullscreen) Modifier else Modifier.padding(paddingValues)
                )
                .background(
                    if (isReadMode) Color(0xFFFFF8DC) else MaterialTheme.colorScheme.background
                )
        ) {
            when {
                bookTitle == null -> {
                    // No book title provided
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "No Book Selected",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Please select a book to view its content",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ArrowBack, "Go Back")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                }
                
                !isBookDownloaded -> {
                    // Book not downloaded
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Book Not Downloaded",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Please download the book first to view its content",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ArrowBack, "Go Back")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                }
                
                else -> {
                    // Book is downloaded - show PDF viewer
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (useFallbackViewer) {
                            // Fallback PDF viewer (simple placeholder)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(500.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = "PDF",
                                                modifier = Modifier.size(100.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = bookTitle,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "PDF Content Displayed",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "This is a placeholder for the actual PDF content. In a real implementation, the PDF would be displayed here with full functionality.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = { useFallbackViewer = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Refresh, "Try PDF Viewer Again")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Try PDF Viewer Again")
                                }
                            }
                        } else {
                            // PDF Viewer
                            if (pdfError != null) {
                                // Show text-based fallback when PDF fails to load
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(600.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = "PDF",
                                                modifier = Modifier.size(80.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = bookTitle,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "PDF Content (Text Fallback)",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "This is a sample book content displayed as text since the PDF viewer encountered an error. In a real implementation, this would show the actual book content.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Error: $pdfError",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = { 
                                            pdfLoaded = false
                                            pdfError = null
                                            isPdfLoading = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, "Retry PDF")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry PDF Loading")
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AndroidView(
                                        factory = { context ->
                                            PDFView(context, null).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        update = { pdfView ->
                                            if (!pdfLoaded) {
                                                try {
                                                    isPdfLoading = true
                                                    pdfError = null
                                                    pdfLoaded = true
                                                    
                                                    android.util.Log.d("PdfViewer", "Starting PDF load for: $bookTitle")
                                                    android.util.Log.d("PdfViewer", "Loading from downloaded file")
                                                    
                                                    // Load PDF from downloaded file
                                                    try {
                                                        android.util.Log.d("PdfViewer", "Attempting to load PDF for: $bookTitle")
                                                        
                                                        val downloadedFile = downloadManager.getDownloadedBookFile(bookTitle)
                                                        if (downloadedFile != null && downloadedFile.exists()) {
                                                            android.util.Log.d("PdfViewer", "Loading from downloaded file: ${downloadedFile.absolutePath}")
                                                            pdfView.fromFile(downloadedFile)
                                                                .defaultPage(currentPage)
                                                                .enableSwipe(true)
                                                                .swipeHorizontal(false)
                                                                .enableDoubletap(true)
                                                                .scrollHandle(DefaultScrollHandle(context))
                                                                .onLoad { pageCount ->
                                                                    android.util.Log.d("PdfViewer", "PDF loaded successfully with $pageCount pages")
                                                                    totalPages = pageCount
                                                                    isPdfLoading = false
                                                                }
                                                                .onError { throwable ->
                                                                    android.util.Log.e("PdfViewer", "PDF loading error: ${throwable.message}")
                                                                    android.util.Log.e("PdfViewer", "Error stack trace: ${throwable.stackTraceToString()}")
                                                                    pdfError = "Failed to load PDF: ${throwable.message}"
                                                                    isPdfLoading = false
                                                                }
                                                                .onPageChange { page, pageCount ->
                                                                    android.util.Log.d("PdfViewer", "Page changed to $page of $pageCount")
                                                                    currentPage = page
                                                                    // Save current page for this book
                                                                    bookTitle?.let { title ->
                                                                        pagePrefs.edit().putInt(title, page).apply()
                                                                    }
                                                                }
                                                                .onRender { pages ->
                                                                    android.util.Log.d("PdfViewer", "PDF rendered with $pages pages")
                                                                }
                                                                .load()
                                                        } else {
                                                            android.util.Log.e("PdfViewer", "No downloaded file found for: $bookTitle")
                                                            pdfError = "No downloaded file found. Please download the book first."
                                                            isPdfLoading = false
                                                        }
                                                    } catch (assetException: Exception) {
                                                        android.util.Log.e("PdfViewer", "Asset loading failed: ${assetException.message}")
                                                        android.util.Log.e("PdfViewer", "Asset exception stack trace: ${assetException.stackTraceToString()}")
                                                        pdfError = "Asset loading failed: ${assetException.message}"
                                                        isPdfLoading = false
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("PdfViewer", "Error setting up PDF viewer: ${e.message}")
                                                    android.util.Log.e("PdfViewer", "Exception stack trace: ${e.stackTraceToString()}")
                                                    pdfError = "Failed to initialize PDF viewer: ${e.message}"
                                                    isPdfLoading = false
                                                }
                                            }
                                        }
                                    )
                                    
                                    // Fullscreen exit button (only in fullscreen mode)
                                    if (isFullscreen) {
                                        IconButton(
                                            onClick = { isFullscreen = false },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(16.dp)
                                                .size(48.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.FullscreenExit,
                                                contentDescription = "Exit Fullscreen",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Loading indicator
                            if (isPdfLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier.padding(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(48.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "Loading PDF...",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Please wait while the PDF loads",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Error message
                            pdfError?.let { error ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier.padding(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Error,
                                                contentDescription = "Error",
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "Failed to Load PDF",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = error,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Button(
                                                    onClick = { 
                                                        pdfLoaded = false
                                                        pdfError = null
                                                        isPdfLoading = true
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.Refresh, "Retry")
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Retry")
                                                }
                                                Button(
                                                    onClick = { useFallbackViewer = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                ) {
                                                    Icon(Icons.Default.ViewModule, "Use Simple Viewer")
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Use Simple Viewer")
                                                }
                                                Button(
                                                    onClick = { navController.popBackStack() },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                ) {
                                                    Icon(Icons.Default.ArrowBack, "Go Back")
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Go Back")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Warm color overlay to emulate blue light filter when read mode is ON
                        if (isReadMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFC107).copy(alpha = 0.08f))
                            ) {}
                        }

                        // Read mode indicator (shows only for 5 seconds on toggle)
                        if (showReadModeBanner && !isPdfLoading && pdfError == null) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = "Read Mode",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Read Mode On",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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