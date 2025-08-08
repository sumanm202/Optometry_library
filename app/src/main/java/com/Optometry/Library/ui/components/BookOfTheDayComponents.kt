package com.Optometry.Library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.Optometry.Library.Utils.CachedBookImage
import com.Optometry.Library.Models.SupabaseHomeLayout

@Composable
fun BookOfTheDaySection(
    book: SupabaseHomeLayout?,
    onReadNowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (book != null) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), // Increased from 16dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book Image
                CachedBookImage(
                    imageUrl = book.image,
                    title = book.title,
                    modifier = Modifier
                        .width(120.dp) // Increased from 100dp
                        .height(160.dp) // Increased from 140dp
                        .clip(RoundedCornerShape(12.dp)) // Increased corner radius
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentScale = ContentScale.Crop,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.width(20.dp)) // Increased from 16dp
                
                // Book Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "New Added Book",
                        fontSize = 14.sp, // Increased from 12sp
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp)) // Increased from 8dp
                    
                    Text(
                        text = book.title,
                        fontSize = 20.sp, // Increased from 18sp
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp)) // Increased from 16dp
                    
                    // Read Now Button
                    Button(
                        onClick = onReadNowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp), // Increased from 20dp
                        modifier = Modifier.height(40.dp) // Increased from 36dp
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = "Read",
                            modifier = Modifier.size(18.dp) // Increased from 16dp
                        )
                        Spacer(modifier = Modifier.width(6.dp)) // Increased from 4dp
                        Text(
                            text = "Read Now",
                            fontSize = 14.sp, // Increased from 12sp
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "No Book",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No book available",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun BookOfTheDayLoading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading Book of the Day...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookOfTheDayError(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}