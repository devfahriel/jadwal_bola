package com.example.jadwalbola

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Menghilangkan ActionBar di Splash Screen
        supportActionBar?.hide()

        // Tunggu 3 detik, lalu pindah ke MainActivity
        lifecycleScope.launch {
            delay(3000) // 3000 ms = 3 detik
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup SplashActivity agar tidak bisa diback
        }
    }
}