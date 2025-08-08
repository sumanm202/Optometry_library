package com.Optometry.Library.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Optometry.Library.Models.SupabaseBook
import com.Optometry.Library.Models.SupabaseCategory
import com.Optometry.Library.Models.SupabaseHomeLayout
import com.Optometry.Library.Repository.SupabaseRepo
import com.Optometry.Library.Utils.MyResponses
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupabaseViewModel(val repo: SupabaseRepo) : ViewModel() {
    
    private val _homeState = MutableStateFlow<MyResponses<Any>>(MyResponses.Loading())
    val homeState: StateFlow<MyResponses<Any>> = _homeState.asStateFlow()
    
    private val _categoriesState = MutableStateFlow<MyResponses<List<SupabaseCategory>>>(MyResponses.Loading())
    val categoriesState: StateFlow<MyResponses<List<SupabaseCategory>>> = _categoriesState.asStateFlow()
    
    private val _booksState = MutableStateFlow<MyResponses<List<SupabaseBook>>>(MyResponses.Loading())
    val booksState: StateFlow<MyResponses<List<SupabaseBook>>> = _booksState.asStateFlow()
    
    private val _homeLayoutState = MutableStateFlow<MyResponses<List<SupabaseHomeLayout>>>(MyResponses.Loading())
    val homeLayoutState: StateFlow<MyResponses<List<SupabaseHomeLayout>>> = _homeLayoutState.asStateFlow()

    fun getHomeData() {
        viewModelScope.launch {
            repo.getHomeData()
            // Update state based on repo response
            _homeState.value = MyResponses.Loading()
            // In a real implementation, you would observe the repo's LiveData
            // and convert it to StateFlow
        }
    }

    fun getCategories() {
        viewModelScope.launch {
            _categoriesState.value = MyResponses.Loading()
            try {
                val categories = repo.getCategoriesDirectly()
                _categoriesState.value = MyResponses.Success(categories)
            } catch (e: Exception) {
                _categoriesState.value = MyResponses.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    fun getBooksByCategory(categoryId: String) {
        viewModelScope.launch {
            _booksState.value = MyResponses.Loading()
            try {
                repo.getBooksByCategory(categoryId)
                // This still uses the old method - would need to be updated if needed
            } catch (e: Exception) {
                _booksState.value = MyResponses.Error("Failed to load books: ${e.message}")
            }
        }
    }

    fun getAllBooks() {
        viewModelScope.launch {
            _booksState.value = MyResponses.Loading()
            try {
                val books = repo.getAllBooksDirectly()
                _booksState.value = MyResponses.Success(books)
            } catch (e: Exception) {
                _booksState.value = MyResponses.Error("Failed to load books: ${e.message}")
            }
        }
    }
    
    fun getHomeLayout() {
        viewModelScope.launch {
            _homeLayoutState.value = MyResponses.Loading()
            try {
                val homeLayouts = repo.getHomeLayoutDirectly()
                _homeLayoutState.value = MyResponses.Success(homeLayouts)
            } catch (e: Exception) {
                _homeLayoutState.value = MyResponses.Error("Failed to load home layout: ${e.message}")
            }
        }
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            repo.testSupabaseConnection()
        }
    }

    fun testBothLayouts() {
        viewModelScope.launch {
            repo.testBothLayouts()
        }
    }

    fun showRequiredDataStructure() {
        viewModelScope.launch {
            repo.showRequiredDataStructure()
        }
    }

    fun checkCurrentData() {
        viewModelScope.launch {
            repo.checkCurrentData()
        }
    }

    fun testBasicConnection() {
        viewModelScope.launch {
            repo.testBasicConnection()
        }
    }
} 