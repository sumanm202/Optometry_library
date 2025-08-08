package com.Optometry.Library.Models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import java.util.UUID

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SupabaseCategory(
    val id: String = UUID.randomUUID().toString(),
    val title: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SupabaseBook(
    val id: String = UUID.randomUUID().toString(),
    val category_id: String,
    val title: String,
    val author: String? = null,
    val category: String? = null,
    val description: String? = null,
    val image: String? = null,
    val book_pdf: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SupabaseHomeLayout(
    val id: String = UUID.randomUUID().toString(),
    val layout_type: Int,
    val title: String,
    val author: String? = null,
    val category: String? = null,
    val description: String? = null,
    val image: String? = null,
    val book_pdf: String
) 