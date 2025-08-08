package com.Optometry.Library.Utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseConfig {
    
    // Replace these with your actual Supabase credentials
    private const val SUPABASE_URL = SupabaseCredentials.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = SupabaseCredentials.SUPABASE_ANON_KEY
    
    val supabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
    
    val postgrest = supabaseClient.postgrest
} 