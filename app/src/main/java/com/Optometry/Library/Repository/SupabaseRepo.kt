package com.Optometry.Library.Repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.Optometry.Library.Models.BooksModel
import com.Optometry.Library.Models.HomeModel
import com.Optometry.Library.Models.SupabaseBook
import com.Optometry.Library.Models.SupabaseCategory
import com.Optometry.Library.Models.SupabaseHomeLayout
import com.Optometry.Library.Utils.MyResponses
import com.Optometry.Library.Utils.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupabaseRepo(val context: Context) {

    private val homeLD = MutableLiveData<MyResponses<ArrayList<HomeModel>>>()
    private val categoriesLD = MutableLiveData<MyResponses<ArrayList<SupabaseCategory>>>()
    private val booksLD = MutableLiveData<MyResponses<ArrayList<SupabaseBook>>>()

    val homeLiveData get() = homeLD
    val categoriesLiveData get() = categoriesLD
    val booksLiveData get() = booksLD

    @SuppressLint("SuspiciousIndentation")
    suspend fun getHomeData() {
        homeLiveData.postValue(MyResponses.Loading())
        
        try {
            val tempList = ArrayList<HomeModel>()
            
            // First, try to get BOD data from home_layouts table
            val homeLayouts = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["home_layouts"].select().decodeList<SupabaseHomeLayout>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching home_layouts: ${e.message}")
                    emptyList()
                }
            }
            
            Log.d("SupabaseRepo", "Fetched ${homeLayouts.size} home layouts")
            
            // Handle BOD layouts (New Added Book section) from home_layouts
            if (homeLayouts.isNotEmpty()) {
                val bodLayouts = homeLayouts.filter { it.layout_type == 1 } // LAYOUT_BOD
                
                if (bodLayouts.isNotEmpty()) {
                    val bodBook = bodLayouts.first() // Take the first BOD book
                                         if (bodBook.title.isNotEmpty() && bodBook.book_pdf.isNotEmpty()) {
                         val bookModel = com.Optometry.Library.Models.BooksModel(
                             image = bodBook.image ?: "https://via.placeholder.com/150x200?text=Book",
                             title = bodBook.title,
                             description = bodBook.description ?: "No description available",
                             author = bodBook.author ?: "Unknown Author",
                             bookPDF = bodBook.book_pdf
                         )
                        
                        val homeModel = HomeModel(
                            catTitle = "New Added Book",
                            booksList = null, // BOD doesn't need booksList
                            bod = bookModel, // Set the BOD book
                            LAYOUT_TYPE = 1 // LAYOUT_BOD
                        )
                        
                        tempList.add(homeModel)
                        Log.d("SupabaseRepo", "Added BOD model for book: ${bodBook.title}")
                    }
                }
            }
            
            // Get categories from categories table
            val categories = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching categories: ${e.message}")
                    emptyList()
                }
            }
            
            Log.d("SupabaseRepo", "Fetched ${categories.size} categories")
            
            // Get books from books table
            val books = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["books"].select().decodeList<SupabaseBook>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching books: ${e.message}")
                    emptyList()
                }
            }
            
            Log.d("SupabaseRepo", "Fetched ${books.size} books")
            
            // Create LAYOUT_HOME sections for each category
            if (categories.isNotEmpty() && books.isNotEmpty()) {
                for (category in categories) {
                    Log.d("SupabaseRepo", "Processing category: ${category.title}")
                    
                    // Filter books for this category using category_id foreign key
                    val categoryBooks = books.filter { book -> 
                        book.category_id == category.id
                    }
                    
                    Log.d("SupabaseRepo", "Found ${categoryBooks.size} books for category: ${category.title}")
                    
                    if (categoryBooks.isNotEmpty()) {
                        val booksList = ArrayList<com.Optometry.Library.Models.BooksModel>()
                        
                        for (book in categoryBooks) {
                            Log.d("SupabaseRepo", "Processing book: ${book.title} in category: ${category.title}")
                            
                                                         // Validate data before creating book model
                             if (book.title.isNotEmpty() && book.book_pdf.isNotEmpty()) {
                                 val bookModel = com.Optometry.Library.Models.BooksModel(
                                     image = book.image ?: "https://via.placeholder.com/150x200?text=Book",
                                     title = book.title,
                                     description = book.description ?: "No description available",
                                     author = book.author ?: "Unknown Author",
                                     bookPDF = book.book_pdf
                                 )
                                 booksList.add(bookModel)
                                 Log.d("SupabaseRepo", "Added book: ${book.title}")
                             } else {
                                 Log.w("SupabaseRepo", "Skipping book with empty title or PDF: ${book.title}")
                             }
                        }
                        
                        Log.d("SupabaseRepo", "Category: '${category.title}', Books: ${booksList.size}")
                        
                        if (booksList.isNotEmpty()) {
                            val homeModel = HomeModel(
                                catTitle = category.title,
                                booksList = booksList,
                                LAYOUT_TYPE = 0 // LAYOUT_HOME
                            )
                            
                            tempList.add(homeModel)
                            Log.d("SupabaseRepo", "Added home model for category: ${category.title}")
                        } else {
                            Log.w("SupabaseRepo", "No valid books found for category: ${category.title}")
                        }
                    }
                }
                         } else {
                 // Fallback: If no categories table, we can't create LAYOUT_HOME sections
                 Log.d("SupabaseRepo", "No categories found - cannot create LAYOUT_HOME sections without categories table")
                 
                 if (books.isNotEmpty()) {
                     Log.d("SupabaseRepo", "Found ${books.size} books but no categories to group them by")
                     Log.d("SupabaseRepo", "You need to either:")
                     Log.d("SupabaseRepo", "1. Add categories to your categories table, OR")
                     Log.d("SupabaseRepo", "2. Update your books to have valid category_id values")
                 }
             }
            
            if (tempList.isNotEmpty()) {
                Log.d("SupabaseRepo", "Successfully created ${tempList.size} home models")
                tempList.forEach { homeModel ->
                    Log.d("SupabaseRepo", "Final model: catTitle=${homeModel.catTitle}, LAYOUT_TYPE=${homeModel.LAYOUT_TYPE}, booksList.size=${homeModel.booksList?.size}, bod=${homeModel.bod?.title}")
                }
                homeLiveData.postValue(MyResponses.Success(tempList))
            } else {
                Log.d("SupabaseRepo", "No data found in either table, creating dummy data for testing")
                
                // Create dummy data for testing
                val dummyList = ArrayList<HomeModel>()
                
                // Add a BOD model
                val dummyBodBook = com.Optometry.Library.Models.BooksModel(
                    image = "https://via.placeholder.com/150x200?text=Test+Book",
                    title = "Test BOD Book",
                    description = "This is a test book for BOD layout",
                    author = "Test Author",
                    bookPDF = "https://example.com/test.pdf"
                )
                
                val bodModel = HomeModel(
                    catTitle = "New Added Book",
                    booksList = null,
                    bod = dummyBodBook,
                    LAYOUT_TYPE = 1
                )
                dummyList.add(bodModel)
                
                // Add multiple category models to test
                for (categoryIndex in 1..2) {
                    val dummyBooks = ArrayList<com.Optometry.Library.Models.BooksModel>()
                    for (i in 1..3) {
                        val dummyBook = com.Optometry.Library.Models.BooksModel(
                            image = "https://via.placeholder.com/150x200?text=Book+$i",
                            title = "Test Book $i (Category $categoryIndex)",
                            description = "This is test book $i in category $categoryIndex",
                            author = "Test Author $i",
                            bookPDF = "https://example.com/test$i.pdf"
                        )
                        dummyBooks.add(dummyBook)
                    }
                    
                    val categoryModel = HomeModel(
                        catTitle = "Test Category $categoryIndex",
                        booksList = dummyBooks,
                        LAYOUT_TYPE = 0
                    )
                    dummyList.add(categoryModel)
                }
                
                Log.d("SupabaseRepo", "Created dummy data: ${dummyList.size} models")
                dummyList.forEach { homeModel ->
                    Log.d("SupabaseRepo", "Dummy model: catTitle=${homeModel.catTitle}, LAYOUT_TYPE=${homeModel.LAYOUT_TYPE}, booksList.size=${homeModel.booksList?.size}, bod=${homeModel.bod?.title}")
                }
                
                homeLiveData.postValue(MyResponses.Success(dummyList))
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error fetching home data: ${e.message}")
            homeLiveData.postValue(MyResponses.Error("Failed to fetch data: ${e.message}"))
        }
    }

    suspend fun getCategories() {
        categoriesLiveData.postValue(MyResponses.Loading())
        
        try {
            val categories = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
            }
            
            if (categories.isNotEmpty()) {
                val categoriesList = ArrayList<SupabaseCategory>()
                categoriesList.addAll(categories)
                categoriesLiveData.postValue(MyResponses.Success(categoriesList))
            } else {
                categoriesLiveData.postValue(MyResponses.Error("No categories found"))
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error fetching categories: ${e.message}")
            categoriesLiveData.postValue(MyResponses.Error("Failed to fetch categories: ${e.message}"))
        }
    }

    suspend fun getBooksByCategory(categoryId: String) {
        booksLiveData.postValue(MyResponses.Loading())
        
        try {
            val books = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["books"]
                    .select()
                    .decodeList<SupabaseBook>()
            }
            
            // Filter books by category_id in Kotlin
            val filteredBooks = books.filter { book: SupabaseBook -> book.category_id == categoryId }
            
            if (filteredBooks.isNotEmpty()) {
                val booksList = ArrayList<SupabaseBook>()
                booksList.addAll(filteredBooks)
                booksLiveData.postValue(MyResponses.Success(booksList))
            } else {
                booksLiveData.postValue(MyResponses.Error("No books found for this category"))
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error fetching books: ${e.message}")
            booksLiveData.postValue(MyResponses.Error("Failed to fetch books: ${e.message}"))
        }
    }

    suspend fun getAllBooks() {
        booksLiveData.postValue(MyResponses.Loading())
        
        try {
            val books = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["books"].select().decodeList<SupabaseBook>()
            }
            
            if (books.isNotEmpty()) {
                val booksList = ArrayList<SupabaseBook>()
                booksList.addAll(books)
                booksLiveData.postValue(MyResponses.Success(booksList))
            } else {
                booksLiveData.postValue(MyResponses.Error("No books found"))
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error fetching all books: ${e.message}")
            booksLiveData.postValue(MyResponses.Error("Failed to fetch books: ${e.message}"))
        }
    }

    suspend fun testSupabaseConnection() {
        Log.d("SupabaseRepo", "Testing Supabase connection...")
        
        try {
            // Test categories table
            val categories = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching categories: ${e.message}")
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Categories test: Found ${categories.size} categories")
            categories.forEach { category ->
                Log.d("SupabaseRepo", "Category: id=${category.id}, title=${category.title}")
            }
            
            // Test books table
            val books = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["books"].select().decodeList<SupabaseBook>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching books: ${e.message}")
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Books test: Found ${books.size} books")
            books.take(5).forEach { book ->
                Log.d("SupabaseRepo", "Book: id=${book.id}, title=${book.title}, category=${book.category}, category_id=${book.category_id}, book_pdf=${book.book_pdf}")
            }
            
            // Test home_layouts table
            val homeLayouts = withContext(Dispatchers.IO) {
                try {
                    SupabaseConfig.postgrest["home_layouts"].select().decodeList<SupabaseHomeLayout>()
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching home_layouts: ${e.message}")
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Home layouts test: Found ${homeLayouts.size} home layouts")
            homeLayouts.forEach { layout ->
                Log.d("SupabaseRepo", "Home layout: id=${layout.id}, layout_type=${layout.layout_type}, title=${layout.title}, book_pdf=${layout.book_pdf}")
            }
            
            // Check if we have enough data to show both layouts
            if (categories.isEmpty() && books.isEmpty() && homeLayouts.isEmpty()) {
                Log.e("SupabaseRepo", "ALL TABLES ARE EMPTY! You need to add data to your Supabase database.")
            } else if (homeLayouts.isEmpty()) {
                Log.w("SupabaseRepo", "home_layouts table is empty - no BOD layout will show")
            } else if (categories.isEmpty() && books.isEmpty()) {
                Log.w("SupabaseRepo", "categories and books tables are empty - no category layouts will show")
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error in testSupabaseConnection: ${e.message}")
        }
    }

    suspend fun testBothLayouts() {
        Log.d("SupabaseRepo", "Testing both layouts with dummy data...")
        
        val testList = ArrayList<HomeModel>()
        
        // Add BOD layout
        val bodBook = com.Optometry.Library.Models.BooksModel(
            image = "https://via.placeholder.com/150x200?text=BOD+Book",
            title = "Test BOD Book",
            description = "This is a test BOD book",
            author = "BOD Author",
            bookPDF = "https://example.com/bod.pdf"
        )
        
        val bodModel = HomeModel(
            catTitle = "New Added Book",
            booksList = null,
            bod = bodBook,
            LAYOUT_TYPE = 1 // LAYOUT_BOD
        )
        testList.add(bodModel)
        
        // Add HOME layout
        val homeBooks = ArrayList<com.Optometry.Library.Models.BooksModel>()
        for (i in 1..3) {
            val book = com.Optometry.Library.Models.BooksModel(
                image = "https://via.placeholder.com/150x200?text=Home+Book+$i",
                title = "Test Home Book $i",
                description = "This is test home book $i",
                author = "Home Author $i",
                bookPDF = "https://example.com/home$i.pdf"
            )
            homeBooks.add(book)
        }
        
        val homeModel = HomeModel(
            catTitle = "Test Home Category",
            booksList = homeBooks,
            LAYOUT_TYPE = 0 // LAYOUT_HOME
        )
        testList.add(homeModel)
        
        Log.d("SupabaseRepo", "Test list created with ${testList.size} models")
        testList.forEach { homeModel ->
            Log.d("SupabaseRepo", "Test model: catTitle=${homeModel.catTitle}, LAYOUT_TYPE=${homeModel.LAYOUT_TYPE}, booksList.size=${homeModel.booksList?.size}, bod=${homeModel.bod?.title}")
        }
        
        homeLiveData.postValue(MyResponses.Success(testList))
    }

    suspend fun checkCurrentData() {
        Log.d("SupabaseRepo", "=== CHECKING CURRENT DATA IN YOUR SUPABASE DATABASE ===")
        
        try {
            // Test basic Supabase connection first
            Log.d("SupabaseRepo", "Testing Supabase connection...")
            
            // Check categories table
            val categories = withContext(Dispatchers.IO) {
                try {
                    Log.d("SupabaseRepo", "Attempting to fetch categories...")
                    val result = SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
                    Log.d("SupabaseRepo", "Categories fetch successful: ${result.size} items")
                    result
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching categories: ${e.message}")
                    Log.e("SupabaseRepo", "Exception type: ${e.javaClass.simpleName}")
                    e.printStackTrace()
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Categories table: Found ${categories.size} categories")
            categories.forEach { category ->
                Log.d("SupabaseRepo", "  - ID: ${category.id}, Title: '${category.title}'")
            }
            
            // Check books table
            val books = withContext(Dispatchers.IO) {
                try {
                    Log.d("SupabaseRepo", "Attempting to fetch books...")
                    val result = SupabaseConfig.postgrest["books"].select().decodeList<SupabaseBook>()
                    Log.d("SupabaseRepo", "Books fetch successful: ${result.size} items")
                    result
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching books: ${e.message}")
                    Log.e("SupabaseRepo", "Exception type: ${e.javaClass.simpleName}")
                    e.printStackTrace()
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Books table: Found ${books.size} books")
            books.take(10).forEach { book ->
                Log.d("SupabaseRepo", "  - ID: ${book.id}, Title: '${book.title}', Category: '${book.category}', PDF: '${book.book_pdf}'")
            }
            
            // Check home_layouts table
            val homeLayouts = withContext(Dispatchers.IO) {
                try {
                    Log.d("SupabaseRepo", "Attempting to fetch home_layouts...")
                    val result = SupabaseConfig.postgrest["home_layouts"].select().decodeList<SupabaseHomeLayout>()
                    Log.d("SupabaseRepo", "Home layouts fetch successful: ${result.size} items")
                    result
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error fetching home_layouts: ${e.message}")
                    Log.e("SupabaseRepo", "Exception type: ${e.javaClass.simpleName}")
                    e.printStackTrace()
                    emptyList()
                }
            }
            Log.d("SupabaseRepo", "Home layouts table: Found ${homeLayouts.size} layouts")
            homeLayouts.forEach { layout ->
                Log.d("SupabaseRepo", "  - ID: ${layout.id}, Type: ${layout.layout_type}, Title: '${layout.title}', PDF: '${layout.book_pdf}'")
            }
            
            // Summary
            Log.d("SupabaseRepo", "")
            Log.d("SupabaseRepo", "=== SUMMARY ===")
            if (categories.isEmpty()) {
                Log.e("SupabaseRepo", "❌ Categories table is EMPTY - LAYOUT_HOME will not show")
            } else {
                Log.d("SupabaseRepo", "✅ Categories table has ${categories.size} categories")
            }
            
            if (books.isEmpty()) {
                Log.e("SupabaseRepo", "❌ Books table is EMPTY - LAYOUT_HOME will not show")
            } else {
                Log.d("SupabaseRepo", "✅ Books table has ${books.size} books")
            }
            
            if (homeLayouts.isEmpty()) {
                Log.e("SupabaseRepo", "❌ Home layouts table is EMPTY - LAYOUT_BOD will not show")
            } else {
                Log.d("SupabaseRepo", "✅ Home layouts table has ${homeLayouts.size} layouts")
            }
            
            Log.d("SupabaseRepo", "=== END OF DATA CHECK ===")
            
            // Test the actual matching logic
            Log.d("SupabaseRepo", "")
            Log.d("SupabaseRepo", "=== TESTING BOOK-CATEGORY MATCHING ===")
            
            if (categories.isNotEmpty() && books.isNotEmpty()) {
                for (category in categories) {
                    val matchingBooks = books.filter { book -> book.category_id == category.id }
                    Log.d("SupabaseRepo", "Category '${category.title}' (${category.id}): ${matchingBooks.size} books")
                    
                    if (matchingBooks.isNotEmpty()) {
                        Log.d("SupabaseRepo", "  Sample books:")
                        matchingBooks.take(3).forEach { book ->
                            Log.d("SupabaseRepo", "    - ${book.title} (category_id: ${book.category_id})")
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error in checkCurrentData: ${e.message}")
            Log.e("SupabaseRepo", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
        }
    }

    suspend fun showRequiredDataStructure() {
        Log.d("SupabaseRepo", "=== REQUIRED DATA STRUCTURE FOR YOUR SUPABASE DATABASE ===")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "1. home_layouts table (for BOD layout):")
        Log.d("SupabaseRepo", "   - id: UUID (auto-generated)")
        Log.d("SupabaseRepo", "   - layout_type: INTEGER (1 for BOD)")
        Log.d("SupabaseRepo", "   - title: TEXT (book title)")
        Log.d("SupabaseRepo", "   - author: TEXT (book author)")
        Log.d("SupabaseRepo", "   - category: TEXT (book category)")
        Log.d("SupabaseRepo", "   - description: TEXT (book description)")
        Log.d("SupabaseRepo", "   - image: TEXT (book cover image URL)")
        Log.d("SupabaseRepo", "   - book_pdf: TEXT (PDF file URL)")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "2. categories table (for category names):")
        Log.d("SupabaseRepo", "   - id: UUID (auto-generated)")
        Log.d("SupabaseRepo", "   - title: TEXT (category name)")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "3. books table (for books in categories):")
        Log.d("SupabaseRepo", "   - id: UUID (auto-generated)")
        Log.d("SupabaseRepo", "   - category_id: UUID (references categories.id)")
        Log.d("SupabaseRepo", "   - title: TEXT (book title)")
        Log.d("SupabaseRepo", "   - author: TEXT (book author)")
        Log.d("SupabaseRepo", "   - category: TEXT (category name - can be used instead of category_id)")
        Log.d("SupabaseRepo", "   - description: TEXT (book description)")
        Log.d("SupabaseRepo", "   - image: TEXT (book cover image URL)")
        Log.d("SupabaseRepo", "   - book_pdf: TEXT (PDF file URL)")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "=== EXACT SQL TO RUN IN YOUR SUPABASE SQL EDITOR ===")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "-- Step 1: Create categories table (if not exists)")
        Log.d("SupabaseRepo", "CREATE TABLE IF NOT EXISTS categories (")
        Log.d("SupabaseRepo", "    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,")
        Log.d("SupabaseRepo", "    title TEXT NOT NULL")
        Log.d("SupabaseRepo", ");")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "-- Step 2: Create books table (if not exists)")
        Log.d("SupabaseRepo", "CREATE TABLE IF NOT EXISTS books (")
        Log.d("SupabaseRepo", "    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,")
        Log.d("SupabaseRepo", "    category_id UUID REFERENCES categories(id),")
        Log.d("SupabaseRepo", "    title TEXT NOT NULL,")
        Log.d("SupabaseRepo", "    author TEXT,")
        Log.d("SupabaseRepo", "    category TEXT,")
        Log.d("SupabaseRepo", "    description TEXT,")
        Log.d("SupabaseRepo", "    image TEXT,")
        Log.d("SupabaseRepo", "    book_pdf TEXT NOT NULL")
        Log.d("SupabaseRepo", ");")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "-- Step 3: Insert sample categories")
        Log.d("SupabaseRepo", "INSERT INTO categories (title) VALUES")
        Log.d("SupabaseRepo", "('Education'),")
        Log.d("SupabaseRepo", "('Clinical Practice'),")
        Log.d("SupabaseRepo", "('Research');")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "-- Step 4: Insert sample books")
        Log.d("SupabaseRepo", "INSERT INTO books (title, author, category, description, image, book_pdf) VALUES")
        Log.d("SupabaseRepo", "('Clinical Optics', 'Dr. Johnson', 'Education', 'Clinical optics guide', 'https://via.placeholder.com/150x200?text=Optics', 'https://example.com/optics.pdf'),")
        Log.d("SupabaseRepo", "('Eye Anatomy', 'Dr. Brown', 'Education', 'Eye anatomy reference', 'https://via.placeholder.com/150x200?text=Anatomy', 'https://example.com/anatomy.pdf'),")
        Log.d("SupabaseRepo", "('Patient Care', 'Dr. Wilson', 'Clinical Practice', 'Patient care guide', 'https://via.placeholder.com/150x200?text=Care', 'https://example.com/care.pdf'),")
        Log.d("SupabaseRepo", "('Research Methods', 'Dr. Davis', 'Research', 'Research methodology', 'https://via.placeholder.com/150x200?text=Research', 'https://example.com/research.pdf');")
        Log.d("SupabaseRepo", "")
        Log.d("SupabaseRepo", "=== END OF SQL COMMANDS ===")
    }

    suspend fun testBasicConnection() {
        Log.d("SupabaseRepo", "=== TESTING BASIC SUPABASE CONNECTION ===")
        
        try {
            // Test if we can connect to Supabase at all
            Log.d("SupabaseRepo", "Testing basic connection...")
            
            // Try to fetch any data from any table
            val testResult = withContext(Dispatchers.IO) {
                try {
                    Log.d("SupabaseRepo", "Attempting basic connection test...")
                    val result = SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
                    Log.d("SupabaseRepo", "✅ Basic connection successful! Found ${result.size} items")
                    result
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "❌ Basic connection failed: ${e.message}")
                    Log.e("SupabaseRepo", "Exception type: ${e.javaClass.simpleName}")
                    e.printStackTrace()
                    emptyList()
                }
            }
            
            if (testResult.isNotEmpty()) {
                Log.d("SupabaseRepo", "✅ Supabase connection is working!")
            } else {
                Log.d("SupabaseRepo", "⚠️ Connection works but no data found")
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Connection test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Search functionality using real Supabase data
    suspend fun searchBooks(query: String): List<BooksModel> {
        return try {
            Log.d("SupabaseRepo", "🔍 Searching books with query: $query")
            
            // Get all books and filter client-side for now (simpler approach)
            val response = SupabaseConfig.postgrest["books"]
                .select()
                .decodeList<SupabaseBook>()
            
            // Filter books client-side by title, author, and description
            val filteredBooks = response.filter { supabaseBook ->
                val title = supabaseBook.title ?: ""
                val author = supabaseBook.author ?: ""
                val description = supabaseBook.description ?: ""
                
                title.contains(query, ignoreCase = true) ||
                author.contains(query, ignoreCase = true) ||
                description.contains(query, ignoreCase = true)
            }
            
            Log.d("SupabaseRepo", "✅ Found ${filteredBooks.size} books matching '$query' out of ${response.size} total books")
            
            // Convert SupabaseBook to BooksModel
            filteredBooks.map { supabaseBook: SupabaseBook ->
                BooksModel(
                    id = supabaseBook.id,
                    title = supabaseBook.title ?: "Unknown Title",
                    author = supabaseBook.author ?: "Unknown Author", 
                    description = supabaseBook.description ?: "",
                    image = supabaseBook.image ?: "",
                    bookPDF = supabaseBook.book_pdf ?: ""
                )
            }
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Search failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getSearchSuggestions(query: String): List<String> {
        return try {
            Log.d("SupabaseRepo", "💡 Getting search suggestions for: $query")
            
            // For now, return empty list - suggestions will be handled by recent searches
            emptyList()
            
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Failed to get suggestions: ${e.message}")
            emptyList()
        }
    }
    
    // New methods that return data directly for StateFlow
    suspend fun getCategoriesDirectly(): List<SupabaseCategory> {
        return try {
            Log.d("SupabaseRepo", "🔍 Fetching categories directly...")
            val categories = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["categories"].select().decodeList<SupabaseCategory>()
            }
            Log.d("SupabaseRepo", "✅ Fetched ${categories.size} categories directly")
            categories
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Failed to fetch categories: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAllBooksDirectly(): List<SupabaseBook> {
        return try {
            Log.d("SupabaseRepo", "🔍 Fetching all books directly...")
            val books = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["books"].select().decodeList<SupabaseBook>()
            }
            Log.d("SupabaseRepo", "✅ Fetched ${books.size} books directly")
            
            // Debug: Log each book
            books.take(5).forEach { book ->
                Log.d("SupabaseRepo", "Book: id=${book.id}, title=${book.title}, category_id=${book.category_id}")
            }
            
            // If no data found, create dummy data for testing
            if (books.isEmpty()) {
                Log.d("SupabaseRepo", "⚠️ No books found, creating dummy data for testing")
                val dummyBooks = listOf(
                    SupabaseBook(
                        id = "dummy-book-1",
                        category_id = "dummy-category-1",
                        title = "Test Book 1",
                        author = "Test Author 1",
                        category = "Test Category",
                        description = "This is a test book 1",
                        image = "https://via.placeholder.com/150x200?text=Book+1",
                        book_pdf = "https://example.com/test1.pdf"
                    ),
                    SupabaseBook(
                        id = "dummy-book-2",
                        category_id = "dummy-category-1",
                        title = "Test Book 2",
                        author = "Test Author 2",
                        category = "Test Category",
                        description = "This is a test book 2",
                        image = "https://via.placeholder.com/150x200?text=Book+2",
                        book_pdf = "https://example.com/test2.pdf"
                    )
                )
                Log.d("SupabaseRepo", "Created ${dummyBooks.size} dummy books")
                return dummyBooks
            }
            
            books
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Failed to fetch books: ${e.message}")
            e.printStackTrace()
            
            // Create dummy data on error for testing
            Log.d("SupabaseRepo", "Creating dummy books due to error")
            val dummyBooks = listOf(
                SupabaseBook(
                    id = "dummy-book-1",
                    category_id = "dummy-category-1",
                    title = "Test Book 1",
                    author = "Test Author 1",
                    category = "Test Category",
                    description = "This is a test book 1",
                    image = "https://via.placeholder.com/150x200?text=Book+1",
                    book_pdf = "https://example.com/test1.pdf"
                ),
                SupabaseBook(
                    id = "dummy-book-2",
                    category_id = "dummy-category-1",
                    title = "Test Book 2",
                    author = "Test Author 2",
                    category = "Test Category",
                    description = "This is a test book 2",
                    image = "https://via.placeholder.com/150x200?text=Book+2",
                    book_pdf = "https://example.com/test2.pdf"
                )
            )
            return dummyBooks
        }
    }
    
    // New method to fetch home layout data for BOD section
    suspend fun getHomeLayoutDirectly(): List<SupabaseHomeLayout> {
        return try {
            Log.d("SupabaseRepo", "🔍 Fetching home layout data directly...")
            val homeLayouts = withContext(Dispatchers.IO) {
                SupabaseConfig.postgrest["home_layouts"].select().decodeList<SupabaseHomeLayout>()
            }
            Log.d("SupabaseRepo", "✅ Fetched ${homeLayouts.size} home layouts directly")
            
            // Debug: Log each home layout
            homeLayouts.forEach { layout ->
                Log.d("SupabaseRepo", "Home layout: id=${layout.id}, title=${layout.title}, layout_type=${layout.layout_type}")
            }
            
            // If no data found, create dummy data for testing
            if (homeLayouts.isEmpty()) {
                Log.d("SupabaseRepo", "⚠️ No home layouts found, creating dummy data for testing")
                val dummyLayout = SupabaseHomeLayout(
                    id = "dummy-bod-id",
                    layout_type = 1,
                    title = "Test BOD Book",
                    author = "Test Author",
                    category = "Test Category",
                    description = "This is a test book for BOD layout",
                    image = "https://via.placeholder.com/150x200?text=Test+Book",
                    book_pdf = "https://example.com/test.pdf"
                )
                Log.d("SupabaseRepo", "Created dummy layout: id=${dummyLayout.id}, title=${dummyLayout.title}")
                return listOf(dummyLayout)
            }
            
            homeLayouts
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "❌ Failed to fetch home layouts: ${e.message}")
            e.printStackTrace()
            
            // Create dummy data on error for testing
            Log.d("SupabaseRepo", "Creating dummy data due to error")
            val dummyLayout = SupabaseHomeLayout(
                id = "dummy-bod-id",
                layout_type = 1,
                title = "Test BOD Book",
                author = "Test Author",
                category = "Test Category",
                description = "This is a test book for BOD layout",
                image = "https://via.placeholder.com/150x200?text=Test+Book",
                book_pdf = "https://example.com/test.pdf"
            )
            return listOf(dummyLayout)
        }
    }
} 
