# Image Caching Implementation

## Overview
This implementation provides optimized image loading with caching to improve performance and reduce data usage in the Optometry Library app.

## Features Implemented

### 1. Coil Image Cache Configuration
- **Memory Cache**: Uses 25% of available device memory for caching images
- **Disk Cache**: Uses 2% of available disk space for persistent image storage
- **Crossfade Animation**: 300ms smooth transition when images load
- **Cache Headers**: Disabled to always cache images regardless of server cache headers

### 2. Custom CachedBookImage Component
- **Optimized Loading**: Uses Coil's AsyncImage with enhanced caching
- **Fallback Support**: Shows placeholder when no image URL is provided
- **Consistent API**: Easy to use across all screens

### 3. Cache Management
- **Preload Function**: `preloadImages()` to preload images for better performance
- **Clear Cache**: `clearImageCache()` to clear both memory and disk cache
- **Profile Integration**: Added "Clear Image Cache" option in Profile screen

## Implementation Details

### Application Level Configuration
```kotlin
// MyBookApp.kt
class MyBookApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% of available disk space
                    .build()
            }
            .respectCacheHeaders(false) // Always cache
            .crossfade(true)
            .crossfade(300)
            .build()
    }
}
```

### Usage in Screens
```kotlin
// Replace AsyncImage with CachedBookImage
CachedBookImage(
    imageUrl = book.image,
    title = book.title,
    modifier = Modifier.size(120.dp, 160.dp),
    contentScale = ContentScale.Crop,
    shape = RoundedCornerShape(12.dp)
)
```

## Benefits

1. **Faster Loading**: Cached images load instantly on subsequent visits
2. **Reduced Data Usage**: Images are stored locally after first download
3. **Better UX**: Smooth crossfade animations and loading states
4. **Offline Support**: Cached images work without internet connection
5. **Memory Efficient**: Automatic cache size management

## Cache Management

### Clear Cache
Users can clear the image cache from the Profile screen:
- Navigate to Profile → "Clear Image Cache"
- This clears both memory and disk cache

### Preload Images
```kotlin
// Preload images for better performance
preloadImages(context, listOf("url1", "url2", "url3"))
```

## Updated Screens
The following screens now use optimized image loading:
- MainScreen (Book of the Day and Category sections)
- SearchScreen (Search results)
- DetailsScreen (Book details)
- CategoryDetailsScreen (Category books)
- BookOfTheDayComponents (Book of the day card)

## Performance Improvements

1. **First Load**: Images download and cache automatically
2. **Subsequent Loads**: Images load instantly from cache
3. **Memory Management**: Automatic cache size limits prevent memory issues
4. **Network Efficiency**: Reduces redundant downloads

## Technical Notes

- Uses Coil 2.5.0 for image loading
- Implements both memory and disk caching
- Crossfade animations for smooth transitions
- Fallback placeholders for missing images
- Cache size limits to prevent storage issues
