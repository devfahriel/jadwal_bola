package com.example.jadwalbola

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    // 1. Ambil Jadwal berdasarkan Rentang Tanggal & Liga
    // (Kita fokuskan ke Premier League (ID: 39) agar kuota irit & lineup pasti ada)
    @GET("fixtures")
    suspend fun getFixturesRange(
        @Query("from") from: String, // YYYY-MM-DD
        @Query("to") to: String,     // YYYY-MM-DD
        @Query("season") season: Int, // e.g. 2024 / 2025
        @Query("league") league: Int  // 39 = Premier League
    ): ApiResponse

    // 2. Ambil Live Match (Semua Liga)
    @GET("fixtures")
    suspend fun getLiveMatches(
        @Query("live") live: String = "all"
    ): ApiResponse

    // 3. Ambil Detail Pertandingan (Lineup & Events)
    @GET("fixtures")
    suspend fun getMatchDetail(
        @Query("id") id: Int
    ): ApiResponse
}