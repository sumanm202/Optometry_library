package com.Optometry.Library.Models

import java.io.Serializable

data class BooksModel(
    val id: String = "",
    val image: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val bookPDF: String = "",
):Serializable