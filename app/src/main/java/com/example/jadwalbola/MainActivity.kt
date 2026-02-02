package com.example.jadwalbola

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var rvFootball: RecyclerView
    // Gunakan MaterialButton karena di XML baru kita pakai MaterialButton
    private lateinit var btnJadwal: MaterialButton
    private lateinit var btnLive: MaterialButton
    private lateinit var btnHasil: MaterialButton

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    // Tambahan untuk Header Tanggal (Opsional, biar tidak error jika ID tidak ketemu)
    private var tvDateHeader: TextView? = null

    enum class TabMode { JADWAL, LIVE, HASIL }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi Views
        rvFootball = findViewById(R.id.rvMatches)

        // Casting ke MaterialButton agar sesuai layout baru
        btnJadwal = findViewById(R.id.btnJadwal)
        btnLive = findViewById(R.id.btnLive)
        btnHasil = findViewById(R.id.btnHasil)

        progressBar = findViewById(R.id.progressBar)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle)

        // Coba cari header tanggal (kalau pakai layout baru)
        try {
            tvDateHeader = findViewById(R.id.tvDateHeader)
        } catch (e: Exception) {
            // Abaikan jika pakai layout lama
        }

        rvFootball.layoutManager = LinearLayoutManager(this)

        // 2. Setup Listener Tombol
        btnJadwal.setOnClickListener { changeTab(TabMode.JADWAL) }
        btnLive.setOnClickListener { changeTab(TabMode.LIVE) }
        btnHasil.setOnClickListener { changeTab(TabMode.HASIL) }

        // 3. Load Awal
        changeTab(TabMode.JADWAL)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeTab(mode: TabMode) {
        updateButtonUI(mode)
        fetchData(mode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchData(mode: TabMode) {
        progressBar.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        rvFootball.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // --- BAGIAN INI SANGAT PENTING (JANGAN DIHAPUS) ---
                // Kita PAKSA tanggal ke 1 Februari 2025
                // Agar data muncul meskipun Emulator tahun 2026
                val today = LocalDate.of(2025, 2, 1)

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var dateFrom = today.format(formatter)
                var dateTo = today.format(formatter)

                // Update Header Tanggal (Visual)
                tvDateHeader?.text = "Februari 2025"

                val season = 2024 // Musim 2024/2025
                val leagueId = 39 // Premier League

                // Log ke Logcat (Cek di bagian 'Logcat' dengan kata kunci 'DEBUG_BOLA')
                Log.d("DEBUG_BOLA", "Requesting: $mode | Date: $dateFrom")

                val response: ApiResponse

                if (mode == TabMode.LIVE) {
                    response = RetrofitClient.instance.getLiveMatches()
                } else {
                    if (mode == TabMode.JADWAL) {
                        // Ambil 7 hari ke depan
                        dateFrom = today.format(formatter)
                        dateTo = today.plusDays(7).format(formatter)
                    } else { // HASIL
                        // Ambil 7 hari ke belakang
                        dateFrom = today.minusDays(7).format(formatter)
                        dateTo = today.format(formatter)
                    }

                    response = RetrofitClient.instance.getFixturesRange(
                        from = dateFrom,
                        to = dateTo,
                        season = season,
                        league = leagueId
                    )
                }

                // Sorting Data
                val sortedMatches = if (mode == TabMode.HASIL) {
                    response.response.sortedByDescending { it.fixture.date }
                } else {
                    response.response.sortedBy { it.fixture.date }
                }

                // Pasang ke Adapter
                val adapter = MatchAdapter(sortedMatches) { match ->
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("MATCH_DATA", match)
                    startActivity(intent)
                }
                rvFootball.adapter = adapter

                // Cek apakah hasil kosong?
                if (sortedMatches.isEmpty()) {
                    rvFootball.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE

                    tvEmptyTitle.text = "Data Kosong"
                    tvEmptySubtitle.text = "Tidak ada pertandingan di tanggal:\n$dateFrom s/d $dateTo"
                } else {
                    rvFootball.visibility = View.VISIBLE
                    layoutEmptyState.visibility = View.GONE
                }

            } catch (e: Exception) {
                // Log Error jika ada (misal koneksi mati)
                Log.e("DEBUG_BOLA", "Error: ${e.message}")

                rvFootball.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
                tvEmptyTitle.text = "Gagal Terhubung"
                tvEmptySubtitle.text = "Error: ${e.message}\nPastikan Internet Aktif."
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateButtonUI(activeMode: TabMode) {
        val colorActive = ContextCompat.getColor(this, R.color.primary_green)
        // Transparan untuk tombol tidak aktif (sesuai layout baru)
        val colorInactive = ContextCompat.getColor(this, android.R.color.transparent)

        val textActive = ContextCompat.getColor(this, R.color.white)
        val textInactive = ContextCompat.getColor(this, R.color.text_secondary)

        // Reset semua tombol ke style Inactive
        btnJadwal.backgroundTintList = ColorStateList.valueOf(colorInactive)
        btnJadwal.setTextColor(textInactive)
        btnJadwal.elevation = 0f

        btnLive.backgroundTintList = ColorStateList.valueOf(colorInactive)
        btnLive.setTextColor(textInactive)
        btnLive.elevation = 0f

        btnHasil.backgroundTintList = ColorStateList.valueOf(colorInactive)
        btnHasil.setTextColor(textInactive)
        btnHasil.elevation = 0f

        // Set tombol Aktif
        when (activeMode) {
            TabMode.JADWAL -> {
                btnJadwal.backgroundTintList = ColorStateList.valueOf(colorActive)
                btnJadwal.setTextColor(textActive)
                btnJadwal.elevation = 4f
            }
            TabMode.LIVE -> {
                btnLive.backgroundTintList = ColorStateList.valueOf(colorActive) // Hijau juga saat aktif biar seragam
                btnLive.setTextColor(textActive)
                btnLive.elevation = 4f
            }
            TabMode.HASIL -> {
                btnHasil.backgroundTintList = ColorStateList.valueOf(colorActive)
                btnHasil.setTextColor(textActive)
                btnHasil.elevation = 4f
            }
        }
    }
}