package com.example.eventradar.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val URL = "https://vcyelbmbzccdtjzyooan.supabase.co"
    const val ANON_KEY = "sb_publishable_gocrHo49UetLELdeHm5cNQ_YNvogXXT"

    val client = createSupabaseClient(URL, ANON_KEY) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
