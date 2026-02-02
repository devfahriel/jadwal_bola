package com.example.jadwalbola

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val match = intent.getParcelableExtra<MatchData>("MATCH_DATA")

        if (match != null) {
            setupBasicUI(match)
            fetchMatchDetail(match.fixture.id)
            setupLiveStreaming(match) // Fungsi Video Auto-Click
        }
    }

    private fun setupLiveStreaming(match: MatchData) {
        val cvLiveStream = findViewById<CardView>(R.id.cvLiveStream)
        val wvLiveStream = findViewById<WebView>(R.id.wvLiveStream)
        val status = match.fixture.status.short

        // Status Live: Babak 1, Babak 2, Istirahat, Extra Time, Penalti, atau LIVE
        val liveStatuses = listOf("1H", "2H", "HT", "ET", "P", "LIVE")

        if (liveStatuses.contains(status)) {
            cvLiveStream.visibility = View.VISIBLE

            // --- KONFIGURASI AGAR BISA AUTO-KLIK ---
            wvLiveStream.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false // Izinkan autoplay
            }

            wvLiveStream.webChromeClient = WebChromeClient()

            // Logika Auto-Click Video Pertama
            wvLiveStream.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // SCRIPT RAHASIA: Cari video pertama, lalu klik otomatis
                    // Kita cari elemen link video (href diawali /watch) lalu klik elemen pertama (index 0)
                    val autoClickScript = """
                        (function() {
                            var videos = document.querySelectorAll('a[href^="/watch"]');
                            if (videos.length > 0) {
                                videos[0].click();
                            }
                        })();
                    """
                    view?.evaluateJavascript(autoClickScript, null)
                }
            }

            val home = match.teams.home.name
            val away = match.teams.away.name
            val query = "$home vs $away live match"

            // URL Pencarian + Filter Live (&sp=EgJAAQ%253D%253D)
            val searchUrl = "https://m.youtube.com/results?search_query=$query"

            wvLiveStream.loadUrl(searchUrl)
        } else {
            cvLiveStream.visibility = View.GONE
        }
    }

    private fun setupBasicUI(match: MatchData) {
        findViewById<TextView>(R.id.tvHomeNameDetail).text = match.teams.home.name
        findViewById<TextView>(R.id.tvAwayNameDetail).text = match.teams.away.name
        findViewById<TextView>(R.id.tvStatusDetail).text = match.fixture.status.long

        Glide.with(this).load(match.teams.home.logo).into(findViewById(R.id.imgHomeDetail))
        Glide.with(this).load(match.teams.away.logo).into(findViewById(R.id.imgAwayDetail))

        val tvScore = findViewById<TextView>(R.id.tvScoreDetail)
        val statusShort = match.fixture.status.short

        if (statusShort == "NS" || statusShort == "TBD" || statusShort == "PST" || statusShort == "CANC") {
            tvScore.text = try { match.fixture.date.substring(11, 16) } catch (e: Exception) { "-" }
        } else {
            val h = match.goals.home ?: 0
            val a = match.goals.away ?: 0
            tvScore.text = "$h - $a"
        }
    }

    private fun fetchMatchDetail(matchId: Int) {
        val tvGoals = findViewById<TextView>(R.id.tvGoalScorers)
        val tvHomeLineup = findViewById<TextView>(R.id.tvHomeLineup)
        val tvAwayLineup = findViewById<TextView>(R.id.tvAwayLineup)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMatchDetail(matchId)
                if (response.response.isNotEmpty()) {
                    val detail = response.response[0]
                    val statusShort = detail.fixture.status.short
                    val isScheduled = (statusShort == "NS" || statusShort == "TBD" || statusShort == "PST" || statusShort == "CANC")

                    // LOGIKA GOL
                    if (isScheduled) {
                        tvGoals.text = "-"
                    } else {
                        if (!detail.events.isNullOrEmpty()) {
                            val sb = StringBuilder()
                            var hasGoal = false
                            detail.events.forEach { event ->
                                if (event.type == "Goal") {
                                    hasGoal = true
                                    val menit = event.time.elapsed
                                    val player = event.player?.name ?: "Gol"
                                    val team = event.team.name
                                    val extra = if (event.detail == "Own Goal") "(OG)" else ""
                                    sb.append("$menit' $player $extra ($team)\n")
                                }
                            }
                            tvGoals.text = if (hasGoal) sb.toString() else "Tidak ada gol tercipta."
                        } else {
                            tvGoals.text = "Tidak ada gol atau data belum tersedia."
                        }
                    }

                    // LOGIKA LINEUP
                    if (isScheduled) {
                        tvHomeLineup.text = "-"
                        tvAwayLineup.text = "-"
                    } else {
                        if (!detail.lineups.isNullOrEmpty()) {
                            val homeId = detail.teams.home.id
                            val homeLineup = detail.lineups.find { it.team.id == homeId }
                            val awayLineup = detail.lineups.find { it.team.id != homeId }

                            tvHomeLineup.text = if (homeLineup != null && !homeLineup.startXI.isNullOrEmpty())
                                formatPlayers(homeLineup.startXI) else "Data pemain tidak tersedia."

                            tvAwayLineup.text = if (awayLineup != null && !awayLineup.startXI.isNullOrEmpty())
                                formatPlayers(awayLineup.startXI) else "Data pemain tidak tersedia."
                        } else {
                            tvHomeLineup.text = "Data pemain tidak tersedia."
                            tvAwayLineup.text = "-"
                        }
                    }
                }
            } catch (e: Exception) {
                tvGoals.text = "-"
                tvHomeLineup.text = "-"
                tvAwayLineup.text = "-"
            }
        }
    }

    private fun formatPlayers(players: List<PlayerWrapper>?): String {
        if (players.isNullOrEmpty()) return "-"
        val sb = StringBuilder()
        for (p in players) {
            val pName = p.player.name ?: "Player"
            sb.append("${p.player.number}. $pName\n")
        }
        return sb.toString()
    }
}