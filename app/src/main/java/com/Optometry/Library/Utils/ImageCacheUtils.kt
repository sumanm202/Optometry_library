package com.Optometry.Library.Utils

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Optimized AsyncImage specifically for book covers with caching
 */
@Composable
fun CachedBookImage(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RectangleShape,
    placeholderColor: Color = Color.LightGray,
    showLoadingIndicator: Boolean = true
) {
    if (!imageUrl.isNullOrEmpty()) {
        val context = LocalContext.current
        
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .crossfade(300)
                .build(),
            contentDescription = "Book cover for $title",
            modifier = modifier.clip(shape),
            contentScale = contentScale
        )
    } else {
        // Fallback for when no image URL is provided
        Box(
            modifier = modifier
                .clip(shape)
                .background(placeholderColor),
            contentAlignment = Alignment.Center
        ) {
            // You can add a book icon or title initials here
        }
    }
}

/**
 * Preload images for better performance
 */
fun preloadImages(context: Context, imageUrls: List<String>) {
    val imageLoader = context.applicationContext as? com.Optometry.Library.MyBookApp
    imageLoader?.let { app ->
        imageUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            app.newImageLoader().enqueue(request)
        }
    }
}

/**
 * Clear image cache
 */
fun clearImageCache(context: Context) {
    val imageLoader = context.applicationContext as? com.Optometry.Library.MyBookApp
    imageLoader?.let { app ->
        app.newImageLoader().memoryCache?.clear()
        app.newImageLoader().diskCache?.clear()
    }
}
