package com.Optometry.Library.Models

// Constants for layout types (moved from deleted Adapters)
const val LAYOUT_HOME = 1

data class HomeModel(
    val catTitle:String?=null,
    val booksList:ArrayList<BooksModel>?=null,

    val bod:BooksModel?=null,
    val LAYOUT_TYPE:Int = LAYOUT_HOME
)
