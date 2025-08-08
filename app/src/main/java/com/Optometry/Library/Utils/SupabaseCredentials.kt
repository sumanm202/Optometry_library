package com.Optometry.Library.Utils

/**
 * Supabase Configuration Template
 * 
 * To use Supabase, you need to:
 * 1. Create a Supabase project at https://supabase.com
 * 2. Create the following tables in your Supabase database:
 * 
 * CREATE TABLE categories (
 *   id UUID PRIMARY KEY,
 *   title TEXT
 * );
 * 
 * CREATE TABLE books (
 *   id UUID PRIMARY KEY,
 *   category_id UUID REFERENCES categories(id),
 *   title TEXT,
 *   author TEXT,
 *   category TEXT,
 *   description TEXT,
 *   image TEXT,
 *   book_pdf TEXT
 * );
 * 
 * CREATE TABLE home_layouts (
 *   id UUID PRIMARY KEY,
 *   layout_type INTEGER,
 *   title TEXT,
 *   author TEXT,
 *   category TEXT,
 *   description TEXT,
 *   image TEXT,
 *   book_pdf TEXT
 * );
 * 
 * 3. Upload your CSV files to these tables
 * 4. Get your Supabase URL and anon key from your project settings
 * 5. Replace the placeholder values in SupabaseConfig.kt
 */
object SupabaseCredentials {
    // Replace these with your actual Supabase credentials
    const val SUPABASE_URL = "https://ffgekclknkopzexnkcxv.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZmZ2VrY2xrbmtvcHpleG5rY3h2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ0NzA5OTEsImV4cCI6MjA3MDA0Njk5MX0.R7qtf8mgbquRDXOuySzySc0ibTP2NAdWoXT1tvn8O1I"
} 