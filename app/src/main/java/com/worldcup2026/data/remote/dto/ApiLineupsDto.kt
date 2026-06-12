package com.worldcup2026.data.remote.dto

data class ApiLineupsResponse(
    val home: ApiLineupTeamDto?,
    val away: ApiLineupTeamDto?
)

data class ApiLineupTeamDto(
    val formation: String?,
    val players: List<ApiLineupPlayerDto>?
)

data class ApiLineupPlayerDto(
    val player: PlayerRefDto?,
    val jerseyNumber: String?,
    val position: String?,
    val substitute: Boolean?,
    val statistics: ApiPlayerStatisticsDto?
)

data class ApiPlayerStatisticsDto(
    val rating: Float?
)
