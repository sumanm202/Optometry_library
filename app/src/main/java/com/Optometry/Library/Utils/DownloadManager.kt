package com.Optometry.Library.Utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.HttpURLConnection

class DownloadManager(context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("downloaded_books", Context.MODE_PRIVATE)
    private val appContext = context.applicationContext
    
    private val _downloadedBooks = MutableStateFlow<Set<String>>(getDownloadedBooks())
    val downloadedBooks: StateFlow<Set<String>> = _downloadedBooks.asStateFlow()
    
    private val _downloadingBooks = MutableStateFlow<Set<String>>(emptySet())
    val downloadingBooks: StateFlow<Set<String>> = _downloadingBooks.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()
    
    private fun getDownloadedBooks(): Set<String> {
        return sharedPrefs.getStringSet("books", emptySet()) ?: emptySet()
    }
    
    fun isBookDownloaded(bookTitle: String): Boolean {
        return _downloadedBooks.value.contains(bookTitle)
    }
    
    fun isBookDownloading(bookTitle: String): Boolean {
        return _downloadingBooks.value.contains(bookTitle)
    }
    
    fun getDownloadProgress(bookTitle: String): Int {
        return _downloadProgress.value[bookTitle] ?: 0
    }
    
    fun getDownloadedBookFile(bookTitle: String): File? {
        return if (isBookDownloaded(bookTitle)) {
            val fileName = "${bookTitle.replace(" ", "_")}.pdf"
            File(appContext.filesDir, fileName)
        } else {
            null
        }
    }
    
    suspend fun downloadBook(bookTitle: String, bookPDFUrl: String): Boolean {
        return try {
            Log.d("DownloadManager", "📥 Starting download for: $bookTitle")
            Log.d("DownloadManager", "📥 Download URL: $bookPDFUrl")
            
            // Validate URL
            if (bookPDFUrl.isBlank()) {
                Log.e("DownloadManager", "❌ Empty PDF URL provided")
                return false
            }
            
            // Add to downloading set and initialize progress
            _downloadingBooks.value = _downloadingBooks.value + bookTitle
            _downloadProgress.value = _downloadProgress.value + (bookTitle to 0)
            
            // Download PDF from Supabase URL
            val fileName = "${bookTitle.replace(" ", "_")}.pdf"
            val outputFile = File(appContext.filesDir, fileName)
            
            try {
                val success = downloadPdfFromUrl(bookPDFUrl, outputFile, bookTitle)
                if (success) {
                    Log.d("DownloadManager", "📄 File downloaded to: ${outputFile.absolutePath}")
                    
                    // Mark as downloaded
                    val updatedBooks = _downloadedBooks.value + bookTitle
                    _downloadedBooks.value = updatedBooks
                    
                    // Save to SharedPreferences
                    sharedPrefs.edit().putStringSet("books", updatedBooks).apply()
                    
                    Log.d("DownloadManager", "✅ Download completed for: $bookTitle")
                } else {
                    Log.e("DownloadManager", "❌ Failed to download PDF from URL")
                    throw IOException("Failed to download PDF from URL")
                }
            } catch (e: IOException) {
                Log.e("DownloadManager", "❌ Failed to download file: ${e.message}")
                throw e
            }
            
            // Remove from downloading set and progress
            _downloadingBooks.value = _downloadingBooks.value - bookTitle
            _downloadProgress.value = _downloadProgress.value - bookTitle
            
            true
            
        } catch (e: Exception) {
            Log.e("DownloadManager", "❌ Download failed for $bookTitle: ${e.message}")
            _downloadingBooks.value = _downloadingBooks.value - bookTitle
            _downloadProgress.value = _downloadProgress.value - bookTitle
            false
        }
    }
    
    private suspend fun downloadPdfFromUrl(urlString: String, outputFile: File, bookTitle: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("DownloadManager", "🌐 Downloading from URL: $urlString")
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 30000 // 30 seconds
                readTimeout = 30000 // 30 seconds
                setRequestProperty("User-Agent", "Optometry-Library-Android")
            }
            
            val responseCode = connection.responseCode
            Log.d("DownloadManager", "📡 Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val contentLength = connection.contentLength.toLong()
                connection.inputStream.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            // Update progress based on actual bytes downloaded
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                _downloadProgress.value = _downloadProgress.value + (bookTitle to progress)
                                Log.d("DownloadManager", "📊 Download progress: $progress% ($totalBytesRead/$contentLength bytes)")
                            }
                        }
                        
                        Log.d("DownloadManager", "📊 Downloaded $totalBytesRead bytes")
                    }
                }
                true
            } else {
                Log.e("DownloadManager", "❌ HTTP Error: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e("DownloadManager", "❌ Download error: ${e.message}")
            false
        }
    }
    
    fun removeDownload(bookTitle: String) {
        val fileName = "${bookTitle.replace(" ", "_")}.pdf"
        val file = File(appContext.filesDir, fileName)
        
        if (file.exists()) {
            file.delete()
            Log.d("DownloadManager", "🗑️ Deleted file: ${file.absolutePath}")
        }
        
        val updatedBooks = _downloadedBooks.value - bookTitle
        _downloadedBooks.value = updatedBooks
        sharedPrefs.edit().putStringSet("books", updatedBooks).apply()
        Log.d("DownloadManager", "🗑️ Removed download for: $bookTitle")
    }
    
    fun clearAllDownloads() {
        // Delete all downloaded PDF files
        appContext.filesDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".pdf")) {
                file.delete()
                Log.d("DownloadManager", "🗑️ Deleted file: ${file.absolutePath}")
            }
        }
        
        _downloadedBooks.value = emptySet()
        _downloadingBooks.value = emptySet()
        _downloadProgress.value = emptyMap()
        sharedPrefs.edit().clear().apply()
        Log.d("DownloadManager", "🗑️ Cleared all downloads")
    }
}