package com.watermelon.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://xljlceoircpibojirxob.supabase.co",
            supabaseKey = "sb_publishable_A5QRqDQpeb2qgSeLLwsFeg_ipfNRdGA"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
