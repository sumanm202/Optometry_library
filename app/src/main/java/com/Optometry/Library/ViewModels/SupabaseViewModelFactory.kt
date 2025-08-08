package com.Optometry.Library.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Optometry.Library.Repository.SupabaseRepo

class SupabaseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupabaseViewModel::class.java)) {
            val repository = SupabaseRepo(context)
            @Suppress("UNCHECKED_CAST")
            return SupabaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 