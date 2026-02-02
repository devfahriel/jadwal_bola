package com.example.jadwalbola

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Wrapper Utama Response
data class ApiResponse(
    val response: List<MatchData>
)

@Parcelize
data class MatchData(
    val fixture: Fixture,
    val league: LeagueInfo,
    val teams: Teams,
    val goals: Goals,
    val score: ScoreData,
    val events: List<Event>? = null,    // Untuk Detail Gol
    val lineups: List<Lineup>? = null   // Untuk Detail Pemain
) : Parcelable

@Parcelize
data class Fixture(
    val id: Int,
    val date: String, // ISO String
    val status: Status
) : Parcelable

@Parcelize
data class Status(
    val long: String, // "Match Finished"
    val short: String // "FT", "NS", "1H"
) : Parcelable

@Parcelize
data class LeagueInfo(
    val id: Int,
    val name: String,
    val logo: String
) : Parcelable

@Parcelize
data class Teams(
    val home: TeamObj,
    val away: TeamObj
) : Parcelable

@Parcelize
data class TeamObj(
    val id: Int,
    val name: String,
    val logo: String
) : Parcelable

@Parcelize
data class Goals(
    val home: Int?,
    val away: Int?
) : Parcelable

@Parcelize
data class ScoreData(
    val fulltime: Goals
) : Parcelable

// --- MODEL UNTUK DETAIL (GOL & LINEUP) ---

@Parcelize
data class Event(
    val time: TimeDetail,
    val team: TeamObj,
    val player: PlayerEvent?,
    val type: String,   // "Goal", "Card"
    val detail: String? // "Normal Goal", "Yellow Card"
) : Parcelable

@Parcelize
data class TimeDetail(
    val elapsed: Int,
    val extra: Int?
) : Parcelable

@Parcelize
data class PlayerEvent(
    val name: String?
) : Parcelable

@Parcelize
data class Lineup(
    val team: TeamObj,
    val startXI: List<PlayerWrapper>?,
    val substitutes: List<PlayerWrapper>?
) : Parcelable

@Parcelize
data class PlayerWrapper(
    val player: PlayerInfo
) : Parcelable

@Parcelize
data class PlayerInfo(
    val id: Int,
    val name: String,
    val number: Int?,
    val pos: String? // "G", "D", "M", "F"
) : Parcelable