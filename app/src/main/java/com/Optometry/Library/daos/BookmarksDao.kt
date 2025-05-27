package com.Optometry.Library.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.Optometry.Library.entities.BookmarksEntity

@Dao
interface BookmarksDao {

    @Query("SELECT * from bookmarks WHERE bookId == :bookId")
    suspend fun getBookmarks(bookId: String): List<BookmarksEntity>

    @Insert
    suspend fun addBookmark(entity: BookmarksEntity)


}