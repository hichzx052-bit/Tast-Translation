package com.hichamdzz.translator.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hichamdzz.translator.api.*
import com.hichamdzz.translator.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    @Provides @Singleton @Named("whisper")
    fun provideWhisperRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.WHISPER_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("deepl")
    fun provideDeepLRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.DEEPL_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("elevenlabs")
    fun provideElevenLabsRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.ELEVENLABS_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton
    fun provideWhisperApi(@Named("whisper") retrofit: Retrofit): WhisperApi = retrofit.create(WhisperApi::class.java)

    @Provides @Singleton
    fun provideDeepLApi(@Named("deepl") retrofit: Retrofit): DeepLApi = retrofit.create(DeepLApi::class.java)

    @Provides @Singleton
    fun provideElevenLabsApi(@Named("elevenlabs") retrofit: Retrofit): ElevenLabsApi = retrofit.create(ElevenLabsApi::class.java)

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
