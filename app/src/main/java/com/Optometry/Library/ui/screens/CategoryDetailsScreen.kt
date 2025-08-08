package com.Optometry.Library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.Optometry.Library.Utils.MyResponses
import com.Optometry.Library.ViewModels.SupabaseViewModel
import com.Optometry.Library.ViewModels.SupabaseViewModelFactory
import com.Optometry.Library.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(
    navController: NavController,
    categoryId: String?,
    categoryTitle: String?
) {
    val context = LocalContext.current
    val viewModel: SupabaseViewModel = viewModel(factory = SupabaseViewModelFactory(context))
    val booksState by viewModel.booksState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getAllBooks()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = categoryTitle ?: "Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        when (booksState) {
            is MyResponses.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MyResponses.Success -> {
                val books = booksState.data
                val categoryBooks = books?.filter { book -> 
                    book.category_id == categoryId
                } ?: emptyList()
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (categoryBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No books found in this category",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        items(categoryBooks) { book ->
                            CategoryBookCard(
                                book = book,
                                onClick = { 
                                    navController.navigate("${Screen.Details.route}/${book.id}")
                                }
                            )
                        }
                    }
                }
            }
            is MyResponses.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = booksState.errorMessage ?: "Failed to load books",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBookCard(
    book: com.Optometry.Library.Models.SupabaseBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp), // Increased from 120dp
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Increased elevation
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Increased from 12dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover image
            Box(
                modifier = Modifier
                    .size(80.dp, 120.dp) // Increased from 80dp x 100dp
                    .clip(RoundedCornerShape(10.dp)) // Increased corner radius
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!book.image.isNullOrEmpty()) {
                    CachedBookImage(
                        imageUrl = book.image,
                        title = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Text(
                        text = book.title.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp)) // Increased from 16dp
            
            // Book details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    fontSize = 18.sp, // Increased from 16sp
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(6.dp)) // Increased from 4dp
                
                if (!book.author.isNullOrEmpty()) {
                    Text(
                        text = "by ${book.author}",
                        fontSize = 15.sp, // Increased from 14sp
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp)) // Increased from 8dp
                
                if (!book.description.isNullOrEmpty()) {
                    Text(
                        text = book.description,
                        fontSize = 13.sp, // Increased from 12sp
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
            }
        }
    }
} 