package com.worldcup2026.domain.model

data class Team(
    val id: Int,
    val name: String,
    val shortName: String,
    val countryCode: String  // e.g. "BRA", "USA"
)

data class Match(
    val id: Int,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTimestamp: Long,   // UTC epoch seconds
    val status: MatchStatus,
    val roundName: String,
    val tournamentName: String,
    val winnerCode: Int?        // 1=home, 2=away, 3=draw
)

enum class MatchStatus {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED,
    POSTPONED,
    CANCELED
}

data class Incident(
    val id: Int,
    val matchId: Int,
    val minute: Int?,
    val type: IncidentType,
    val playerName: String?,
    val isHome: Boolean
)

enum class IncidentType {
    GOAL, OWN_GOAL, PENALTY_GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION,
    PENALTY_SCORED, PENALTY_MISSED,  // penalty shootout kicks
    OTHER
}

data class LineupPlayer(
    val id: Int,
    val name: String,
    val shortName: String,
    val jerseyNumber: Int?,
    val position: String?,    // "GK","DF","MF","FW"
    val positionX: Float?,    // 0.0–1.0 relative field position
    val positionY: Float?,
    val rating: Float?
)

data class Lineup(
    val matchId: Int,
    val homeFormation: String?,
    val awayFormation: String?,
    val homePlayers: List<LineupPlayer>,
    val awayPlayers: List<LineupPlayer>
)

data class StatItem(
    val name: String,
    val homeValue: String,
    val awayValue: String,
    val homePercent: Float?,   // null if not percentage-based
    val awayPercent: Float?
)

data class KnockoutMatch(
    val homeTeamName: String,
    val homeTeamCode: String,   // alpha2 for flag
    val awayTeamName: String,
    val awayTeamCode: String,
    val homeScore: String?,
    val awayScore: String?,
    val finished: Boolean,
    val homeWinner: Boolean,
    val awayWinner: Boolean,
    val matchId: Int? = null    // SofaScore event ID for navigation
)

data class KnockoutRound(
    val name: String,
    val order: Int,
    val matches: List<KnockoutMatch>
)

data class Standing(
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
    val groupName: String
)
