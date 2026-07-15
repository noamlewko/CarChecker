package com.noamlewkowicz.carchecker.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides the configured Retrofit API service used by the application.
 */
object RetrofitClient {

    private const val BASE_URL = "https://data.gov.il/"

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 15L
    private const val CALL_TIMEOUT_SECONDS = 45L

    /**
     * Logs request and response details during development.
     */
    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    /**
     * Configures networking behavior and allows extra response time because
     * the DataGov API may respond slowly.
     */
    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(
                CONNECT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                READ_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                WRITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .callTimeout(
                CALL_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .addInterceptor(loggingInterceptor)
            .build()

    /**
     * Creates the DataGov API implementation only when it is first required.
     */
    val apiService: DataGovApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(DataGovApiService::class.java)
    }
}