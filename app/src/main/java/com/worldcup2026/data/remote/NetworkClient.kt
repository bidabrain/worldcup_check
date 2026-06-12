package com.worldcup2026.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val BASE_URL = "https://www.sofascore.com/api/v1/"
    private const val API_BASE_URL = "https://api.sofascore.com/api/v1/"

    private fun buildClient(userAgent: String) = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", "https://www.sofascore.com/")
                .header("Origin", "https://www.sofascore.com")
                .header("Cache-Control", "no-cache")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: SofaScoreApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(buildClient("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SofaScoreApi::class.java)

    val apiV2: SofaScoreApiV2 = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(buildClient("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SofaScoreApiV2::class.java)
}
