package com.Optometry.Library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.Optometry.Library.Utils.CachedBookImage
import com.Optometry.Library.Repository.SupabaseRepo
import com.Optometry.Library.Utils.MyResponses
import com.Optometry.Library.ViewModels.SupabaseViewModel
import com.Optometry.Library.ViewModels.SupabaseViewModelFactory
import com.Optometry.Library.navigation.Screen
import com.Optometry.Library.ui.components.*
import com.Optometry.Library.ui.theme.*
import com.Optometry.Library.Utils.BackButtonHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    showExitDialog: Boolean = false,
    onExitDialogDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SupabaseViewModel = viewModel(factory = SupabaseViewModelFactory(context))
    val categoriesState by viewModel.categoriesState.collectAsState()
    val booksState by viewModel.booksState.collectAsState()
    val homeLayoutState by viewModel.homeLayoutState.collectAsState()
    
    LaunchedEffect(Unit) { 
        viewModel.getCategories()
        viewModel.getAllBooks()
        viewModel.getHomeLayout()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Optometry Library",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    // Search button
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Black
                        )
                    }
                    
                    // Profile button
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp), // Increased from 16dp
            verticalArrangement = Arrangement.spacedBy(20.dp) // Increased from 16dp
        ) {
            // BOD Section - Yellow card with "New Added Book"
            item {
                when (homeLayoutState) {
                    is MyResponses.Loading -> { BookOfTheDayLoading() }
                    is MyResponses.Success -> {
                        val homeLayouts = homeLayoutState.data
                        if (!homeLayouts.isNullOrEmpty()) {
                            val bodBook = homeLayouts.first()
                            // Debug logging
                            android.util.Log.d("MainScreen", "BOD Book: id=${bodBook.id}, title=${bodBook.title}")
                            BodSection(
                                book = bodBook,
                                onReadNowClick = { 
                                    // Navigate to details with the home layout book ID
                                    android.util.Log.d("MainScreen", "Navigating to details with bookId: ${bodBook.id}")
                                    navController.navigate("${Screen.Details.route}/${bodBook.id}") 
                                }
                            )
                        } else {
                            android.util.Log.d("MainScreen", "No home layouts found")
                            BodSection(book = null, onReadNowClick = { })
                        }
                    }
                    is MyResponses.Error -> { 
                        android.util.Log.e("MainScreen", "Home layout error: ${homeLayoutState.errorMessage}")
                        BookOfTheDayError(homeLayoutState.errorMessage ?: "Failed to load book of the day") 
                    }
                }
            }
            
            // Categories Section with Books - Show ALL categories
            when {
                categoriesState is MyResponses.Loading || booksState is MyResponses.Loading -> { 
                    item { CategorySectionLoading() } 
                }
                categoriesState is MyResponses.Success && booksState is MyResponses.Success -> {
                    val categories = categoriesState.data
                    val books = booksState.data
                    
                    if (!categories.isNullOrEmpty() && !books.isNullOrEmpty()) {
                        items(categories) { category -> // Show ALL categories, not just 3
                            // Filter books for this category using category_id foreign key
                            val categoryBooks = books.filter { book -> 
                                book.category_id == category.id
                            }
                            
                            CategorySection(
                                title = category.title,
                                books = categoryBooks,
                                onBookClick = { bookId -> navController.navigate("${Screen.Details.route}/${bookId}") },
                                onSeeAllClick = { 
                                    // Navigate to category details screen with category info
                                    navController.navigate("${Screen.Category.route}/${category.id}/${category.title}")
                                }
                            )
                        }
                    }
                }
                categoriesState is MyResponses.Error -> { 
                    item { CategorySectionError(message = categoriesState.errorMessage ?: "Failed to load categories") } 
                }
                booksState is MyResponses.Error -> { 
                    item { CategorySectionError(message = booksState.errorMessage ?: "Failed to load books") } 
                }
            }
        }
    }
    
    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = onExitDialogDismiss,
            title = {
                Text(
                    "Exit App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Do you want to exit?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExitDialogDismiss()
                        BackButtonHandler.exitApp()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onExitDialogDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("No", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Handle back press
    LaunchedEffect(Unit) {
        // This will be handled by the activity's onBackPressed
    }
}

@Composable
private fun BodSection(
    book: com.Optometry.Library.Models.SupabaseHomeLayout?,
    onReadNowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp), // Increased height
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700) // Golden yellow color
        ),
        shape = RoundedCornerShape(16.dp), // Increased corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp), // Increased padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "New Added Book",
                    fontSize = 20.sp, // Increased font size
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
                
                // Read book button
                Button(
                    onClick = onReadNowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp), // Increased corner radius
                    modifier = Modifier.height(40.dp) // Increased height
                ) {
                    Text(
                        text = "read book >",
                        color = Color.White,
                        fontSize = 14.sp, // Increased font size
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Right side - Book image (if available)
            if (book?.image != null) {
                Spacer(modifier = Modifier.width(20.dp)) // Increased spacing
                CachedBookImage(
                    imageUrl = book.image,
                    title = book.title,
                    modifier = Modifier
                        .size(100.dp, 160.dp) // Increased size significantly
                        .clip(RoundedCornerShape(12.dp)), // Increased corner radius
                    contentScale = ContentScale.Crop,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    books: List<com.Optometry.Library.Models.SupabaseBook>,
    onBookClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column {
        // Section header with title and "See All" link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp), // Increased from 4dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp, // Increased from 16sp
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            TextButton(
                onClick = onSeeAllClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = "See All",
                    fontSize = 15.sp, // Increased from 14sp
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp)) // Increased from 8dp
        
        // Horizontal scrollable book list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased from 12dp
            contentPadding = PaddingValues(horizontal = 6.dp) // Increased from 4dp
        ) {
            items(books) { book -> // Show ALL books, not just 5
                BookCard(
                    title = book.title,
                    author = book.author,
                    imageUrl = book.image,
                    onClick = { onBookClick(book.id) }
                )
            }
        }
    }
}

@Composable
private fun BookCard(
    title: String,
    author: String?,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp) // Increased from 120dp
            .height(210.dp), // Increased from 160dp
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Increased elevation
        shape = RoundedCornerShape(12.dp) // Increased corner radius
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp) // Increased padding
        ) {
            // Book cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)) // Increased corner radius
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    CachedBookImage(
                        imageUrl = imageUrl,
                        title = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Text(
                        text = title.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp)) // Increased spacing
            
            // Book title
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium, // Increased from bodySmall
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            
            // Author (if available)
            if (!author.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 