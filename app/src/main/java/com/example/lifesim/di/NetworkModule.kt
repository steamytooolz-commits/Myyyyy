package com.example.lifesim.di

import com.example.lifesim.data.local.AppSettingsManager
import com.example.lifesim.data.remote.LLMApiService
import com.example.lifesim.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(settingsManager: AppSettingsManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    var request = chain.request()
                    val customBaseUrl = settingsManager.getBaseUrl()
                    val apiKey = settingsManager.getApiKey()

                    // Replace Base URL dynamically if configured and different from default
                    if (customBaseUrl.isNotBlank() && customBaseUrl != Constants.LLM_API_BASE_URL) {
                        val oldUrlString = request.url.toString()
                        val defaultBase = Constants.LLM_API_BASE_URL
                        if (oldUrlString.startsWith(defaultBase)) {
                            val newUrlString = oldUrlString.replace(defaultBase, customBaseUrl)
                            val newHttpUrl = newUrlString.toHttpUrlOrNull()
                            if (newHttpUrl != null) {
                                request = request.newBuilder().url(newHttpUrl).build()
                            }
                        }
                    }

                    // Add key dynamically if configured
                    val requestBuilder = request.newBuilder()
                    if (apiKey.isNotBlank()) {
                        requestBuilder.header("Authorization", "Bearer $apiKey")
                    }

                    return chain.proceed(requestBuilder.build())
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.LLM_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideLLMApiService(retrofit: Retrofit): LLMApiService {
        return retrofit.create(LLMApiService::class.java)
    }
}

