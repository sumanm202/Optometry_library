package com.Optometry.Library.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Optometry.Library.Models.BooksModel
import com.Optometry.Library.Repository.SupabaseRepo
import com.Optometry.Library.Utils.MyResponses
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SearchViewModel(private val repo: SupabaseRepo) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<MyResponses<List<BooksModel>>>(MyResponses.Success(emptyList()))
    val searchResults: StateFlow<MyResponses<List<BooksModel>>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _isVoiceSearching = MutableStateFlow(false)
    val isVoiceSearching: StateFlow<Boolean> = _isVoiceSearching.asStateFlow()
    
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    
    // Using real Supabase data only - no more mock data
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            _searchResults.value = MyResponses.Success(emptyList())
        }
    }
    
    fun performSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = MyResponses.Loading()
            
            try {
                // Use real Supabase repository search only
                val searchResults = repo.searchBooks(query)
                _searchResults.value = MyResponses.Success(searchResults)
                
                // Add to recent searches if not empty and not already present
                if (query.isNotEmpty() && !_recentSearches.value.contains(query)) {
                    val updatedRecent = listOf(query) + _recentSearches.value.take(4)
                    _recentSearches.value = updatedRecent
                }
                
            } catch (e: Exception) {
                _searchResults.value = MyResponses.Error("Search failed: ${e.message}")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun startVoiceSearch() {
        _isVoiceSearching.value = true
    }
    
    fun stopVoiceSearch() {
        _isVoiceSearching.value = false
    }
    
    fun onVoiceSearchResult(result: String) {
        _isVoiceSearching.value = false
        updateSearchQuery(result)
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = MyResponses.Success(emptyList())
    }
    
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
    
    fun selectRecentSearch(query: String) {
        updateSearchQuery(query)
    }
}