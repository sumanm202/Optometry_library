package com.Optometry.Library.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Optometry.Library.navigation.Screen
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(navController: NavController) {
    val context = LocalContext.current
    val bookmarksPrefs = remember { context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE) }
    
    // Get all bookmarks
    var bookmarks by remember {
        mutableStateOf(
            bookmarksPrefs.all.mapNotNull { (key, value) ->
                if (value is String) {
                    BookmarkItem(key, value)
                } else null
            }
        )
    }
    
    // Refresh bookmarks when preferences change
    LaunchedEffect(Unit) {
        bookmarks = bookmarksPrefs.all.mapNotNull { (key, value) ->
            if (value is String) {
                BookmarkItem(key, value)
            } else null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search bookmarks */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = { /* Sort bookmarks */ }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (bookmarks.isEmpty()) {
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
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmarks",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Bookmarks Yet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your bookmarked books will appear here",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarks) { bookmark ->
                    BookmarkCard(
                        bookmark = bookmark,
                        onBookmarkClick = { 
                            // Extract book title and page number from bookmark
                            val parts = bookmark.title.split(" - ")
                            val bookTitle = parts.firstOrNull()
                            val pageInfo = parts.getOrNull(1) // "Page X"
                            val pageNumber = pageInfo?.replace("Page ", "")?.toIntOrNull() ?: 0
                            
                            if (bookTitle != null) {
                                // Navigate to PDF viewer with page number
                                navController.navigate("${Screen.PdfViewer.route}/$bookTitle/$pageNumber")
                            }
                        },
                        onDeleteClick = {
                            bookmarksPrefs.edit().remove(bookmark.key).apply()
                            // Update the bookmarks list immediately
                            bookmarks = bookmarksPrefs.all.mapNotNull { (key, value) ->
                                if (value is String) {
                                    BookmarkItem(key, value)
                                } else null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: BookmarkItem,
    onBookmarkClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onBookmarkClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Bookmark",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = bookmark.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Bookmark",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class BookmarkItem(
    val key: String,
    val title: String,
    val description: String = ""
) 