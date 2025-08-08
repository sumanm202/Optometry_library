package com.Optometry.Library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.Optometry.Library.Utils.CachedBookImage
import com.Optometry.Library.Repository.SupabaseRepo
import com.Optometry.Library.Utils.DownloadManager
import com.Optometry.Library.Utils.MyResponses
import com.Optometry.Library.ViewModels.SupabaseViewModel
import com.Optometry.Library.ViewModels.SupabaseViewModelFactory
import com.Optometry.Library.navigation.Screen
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavController,
    bookId: String? = null
) {
    // Debug logging
    LaunchedEffect(bookId) {
        android.util.Log.d("DetailsScreen", "Received bookId: $bookId")
    }
    val context = LocalContext.current
    val viewModel: SupabaseViewModel = viewModel(
        factory = SupabaseViewModelFactory(context)
    )
    val downloadManager = remember { DownloadManager(context) }
    val scope = rememberCoroutineScope()
    
    // States
    val booksState by viewModel.booksState.collectAsState()
    val homeLayoutState by viewModel.homeLayoutState.collectAsState()
    val downloadedBooks by downloadManager.downloadedBooks.collectAsState()
    val downloadingBooks by downloadManager.downloadingBooks.collectAsState()
    val downloadProgress by downloadManager.downloadProgress.collectAsState()
    
    // Load books and home layout if not loaded
    LaunchedEffect(Unit) {
        viewModel.getAllBooks()
        viewModel.getHomeLayout()
    }
    
    // Debug logging for states
    LaunchedEffect(booksState, homeLayoutState) {
        android.util.Log.d("DetailsScreen", "Books state: ${booksState.javaClass.simpleName}")
        android.util.Log.d("DetailsScreen", "Home layout state: ${homeLayoutState.javaClass.simpleName}")
        
        if (booksState is MyResponses.Success) {
            android.util.Log.d("DetailsScreen", "Books count: ${booksState.data?.size}")
            booksState.data?.take(3)?.forEach { book ->
                android.util.Log.d("DetailsScreen", "Book: id=${book.id}, title=${book.title}")
            }
        }
        
        if (homeLayoutState is MyResponses.Success) {
            android.util.Log.d("DetailsScreen", "Home layouts count: ${homeLayoutState.data?.size}")
            homeLayoutState.data?.take(3)?.forEach { layout ->
                android.util.Log.d("DetailsScreen", "Home layout: id=${layout.id}, title=${layout.title}")
            }
        }
    }
    
    // Find the specific book from either books or home layout
    val finalBook = when {
        // First try to find in home layout (for BOD books)
        homeLayoutState is MyResponses.Success -> {
            homeLayoutState.data?.firstOrNull { it.id == bookId }
        }
        // Then try to find in books
        booksState is MyResponses.Success -> {
            booksState.data?.firstOrNull { it.id == bookId }
        }
        // If both states are not ready, return null
        else -> null
    } ?: when {
        // Fallback: if not found in home layout, try books
        homeLayoutState is MyResponses.Success && booksState is MyResponses.Success -> {
            booksState.data?.firstOrNull { it.id == bookId }
        }
        // If only books state is ready
        booksState is MyResponses.Success -> {
            booksState.data?.firstOrNull { it.id == bookId }
        }
        // If only home layout state is ready
        homeLayoutState is MyResponses.Success -> {
            homeLayoutState.data?.firstOrNull { it.id == bookId }
        }
        else -> null
    }
    
    // Debug logging for book finding
    LaunchedEffect(finalBook, bookId) {
        android.util.Log.d("DetailsScreen", "Looking for bookId: $bookId")
        android.util.Log.d("DetailsScreen", "Found book: ${finalBook != null}")
        if (finalBook != null) {
            when (finalBook) {
                is com.Optometry.Library.Models.SupabaseBook -> {
                    android.util.Log.d("DetailsScreen", "Found SupabaseBook title: ${finalBook.title}")
                }
                is com.Optometry.Library.Models.SupabaseHomeLayout -> {
                    android.util.Log.d("DetailsScreen", "Found SupabaseHomeLayout title: ${finalBook.title}")
                }
                else -> {
                    android.util.Log.d("DetailsScreen", "Found unknown book type: ${finalBook.javaClass.simpleName}")
                }
            }
        }
    }
    
    // Create a simple wrapper to access common properties
    data class BookInfo(
        val title: String,
        val author: String?,
        val description: String?,
        val image: String?,
        val bookPdf: String,
        val category: String?
    )
    
    val bookInfo = finalBook?.let { book ->
        when (book) {
            is com.Optometry.Library.Models.SupabaseBook -> BookInfo(
                title = book.title,
                author = book.author,
                description = book.description,
                image = book.image,
                bookPdf = book.book_pdf,
                category = book.category
            )
            is com.Optometry.Library.Models.SupabaseHomeLayout -> BookInfo(
                title = book.title,
                author = book.author,
                description = book.description,
                image = book.image,
                bookPdf = book.book_pdf,
                category = book.category
            )
            else -> null
        }
    }
    
    val isDownloaded = bookInfo?.let { downloadedBooks.contains(it.title) } ?: false
    val isDownloading = bookInfo?.let { downloadingBooks.contains(it.title) } ?: false
    val currentProgress = bookInfo?.let { downloadProgress[it.title] ?: 0 } ?: 0
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            booksState is MyResponses.Loading || homeLayoutState is MyResponses.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            booksState is MyResponses.Error && homeLayoutState is MyResponses.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = booksState.errorMessage ?: homeLayoutState.errorMessage ?: "Failed to load book details",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            bookInfo != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Book Cover and Basic Info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Book Image
                            CachedBookImage(
                                imageUrl = bookInfo?.image,
                                title = bookInfo?.title ?: "Book",
                                modifier = Modifier
                                    .size(200.dp, 280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentScale = ContentScale.Crop,
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Book Title
                            Text(
                                text = bookInfo?.title ?: "Unknown Title",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Author Name
                            Text(
                                text = "by ${bookInfo?.author ?: "Unknown Author"}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Download/Read Button
                            Button(
                                onClick = {
                                    val bookTitle = bookInfo?.title ?: "Unknown Book"
                                    if (isDownloaded) {
                                        // Navigate to PDF viewer
                                        navController.navigate("${Screen.PdfViewer.route}/${bookTitle}")
                                    } else if (!isDownloading) {
                                        // Start download
                                        scope.launch {
                                            val success = downloadManager.downloadBook(
                                                bookTitle,
                                                bookInfo?.bookPdf ?: ""
                                            )
                                            if (success) {
                                                // Navigate to PDF viewer after download
                                                navController.navigate("${Screen.PdfViewer.route}/${bookTitle}")
                                            } else {
                                                // Show error message if download fails
                                                android.util.Log.e("DetailsScreen", "Download failed for: $bookTitle")
                                            }
                                        }
                                    }
                                },
                                enabled = !isDownloading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        progress = { currentProgress / 100f },
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Downloading... $currentProgress%")
                                } else {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Default.MenuBook else Icons.Default.Download,
                                        contentDescription = if (isDownloaded) "Read" else "Download",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isDownloaded) "Read Book" else "Download Book",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            
                            // Download status message
                            if (isDownloading) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Downloading book... $currentProgress% complete",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Description Section
                    if (!bookInfo?.description.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = bookInfo?.description ?: "",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Book Details Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Book Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            BookDetailRow(label = "Title", value = bookInfo?.title ?: "Unknown")
                            BookDetailRow(label = "Author", value = bookInfo?.author ?: "Unknown")
                            BookDetailRow(label = "Category", value = bookInfo?.category ?: "General")
                            BookDetailRow(label = "Status", value = if (isDownloaded) "Downloaded" else "Available")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            else -> {
                // No book found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = "No Book",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Book not found",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 