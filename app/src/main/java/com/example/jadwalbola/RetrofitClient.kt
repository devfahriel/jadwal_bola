package com.example.jadwalbola
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 1. BASE URL BARU (Khusus Akun Direct/Dashboard)
    private const val BASE_URL = "https://v3.football.api-sports.io/"

    val instance: FootballApiService by lazy {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // 2. HEADER BARU
                // Ganti "MASUKKAN_KEY_DARI_DASHBOARD_ANDA" dengan kode dari dashboard (kotak biru di screenshot)
                .addHeader("x-apisports-key", "2103efe7a5f69a3c9a823ce570e82b6c")
                .build()
            chain.proceed(request)
        }.build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }
}