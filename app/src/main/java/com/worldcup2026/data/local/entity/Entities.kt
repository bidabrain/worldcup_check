package com.worldcup2026.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeTeamId: Int,
    val homeTeamName: String,
    val homeTeamShortName: String,
    val homeTeamCountryCode: String,
    val awayTeamId: Int,
    val awayTeamName: String,
    val awayTeamShortName: String,
    val awayTeamCountryCode: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTimestamp: Long,
    val statusCode: Int,           // 0=not started, 6/7=live, 100=finished
    val roundName: String,
    val tournamentName: String,
    val winnerCode: Int?,
    val cachedAt: Long             // System.currentTimeMillis()
)

@Entity(tableName = "standings")
data class StandingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rank: Int,
    val teamId: Int,
    val teamName: String,
    val teamCountryCode: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val points: Int,
    val groupName: String,
    val cachedAt: Long
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: Int,
    val matchId: Int,
    val minute: Int?,
    val incidentType: String,
    val playerName: String?,
    val isHome: Boolean
)
