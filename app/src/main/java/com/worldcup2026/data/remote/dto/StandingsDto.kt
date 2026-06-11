package com.worldcup2026.data.remote.dto

data class StandingsResponse(
    val standings: List<StandingGroupDto>?
)

data class StandingGroupDto(
    val name: String?,
    val rows: List<StandingRowDto>?
)

data class StandingRowDto(
    val position: Int?,
    val team: TeamDto?,
    val matches: Int?,
    val wins: Int?,
    val draws: Int?,
    val losses: Int?,
    val scoresFor: Int?,
    val scoresAgainst: Int?,
    val points: Int?
)
