package com.example.jadwalbola

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MatchAdapter(
    private val matches: List<MatchData>,
    private val onItemClick: (MatchData) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvHomeName: TextView = itemView.findViewById(R.id.tvHomeName)
        val tvAwayName: TextView = itemView.findViewById(R.id.tvAwayName)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val imgHome: ImageView = itemView.findViewById(R.id.imgHome)
        val imgAway: ImageView = itemView.findViewById(R.id.imgAway)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        // Pastikan layout item_match.xml sudah benar
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        val context = holder.itemView.context

        // 1. Tampilkan Nama Tim
        holder.tvHomeName.text = match.teams.home.name
        holder.tvAwayName.text = match.teams.away.name

        // 2. Tampilkan Tanggal (Hanya ambil 10 karakter pertama: YYYY-MM-DD)
        holder.tvDate.text = try { match.fixture.date.substring(0, 10) } catch (e: Exception) { "" }

        // 3. Tampilkan Logo Tim
        Glide.with(context).load(match.teams.home.logo).into(holder.imgHome)
        Glide.with(context).load(match.teams.away.logo).into(holder.imgAway)

        // --- LOGIKA UTAMA: SKOR vs WAKTU ---
        val statusShort = match.fixture.status.short

        // Daftar status yang dianggap "Belum Main" (Tampilkan JAM)
        // NS = Not Started, TBD = To Be Defined, PST = Postponed
        if (statusShort == "NS" || statusShort == "TBD" || statusShort == "PST" || statusShort == "CANC") {

            // A. TAMPILKAN JAM
            val jam = try {
                // Format API: 2025-02-01T15:00:00+00:00
                // Kita ambil karakter ke 11 s/d 16 (15:00)
                match.fixture.date.substring(11, 16)
            } catch (e: Exception) {
                "-"
            }
            holder.tvScore.text = jam

            // Atur Status Text
            if (statusShort == "PST") {
                holder.tvStatus.text = "TUNDA"
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.live_red_bg)) // Merah muda untuk tunda
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.live_red_text))
            } else {
                holder.tvStatus.text = "JADWAL"
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.scheduled_gray_bg))
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.scheduled_gray_text))
            }

        } else {
            // B. TAMPILKAN SKOR (Live atau Selesai)
            val h = match.goals.home ?: 0
            val a = match.goals.away ?: 0
            holder.tvScore.text = "$h - $a"

            // Cek apakah Full Time atau Live
            if (statusShort == "FT" || statusShort == "AET" || statusShort == "PEN") {
                holder.tvStatus.text = "FULL TIME"
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.finished_green_bg))
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.finished_green_text))
            } else {
                // Live (1H, 2H, HT, dll)
                holder.tvStatus.text = "LIVE • $statusShort"
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.live_red_bg))
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.live_red_text))
            }
        }

        holder.itemView.setOnClickListener { onItemClick(match) }
    }

    override fun getItemCount() = matches.size
}