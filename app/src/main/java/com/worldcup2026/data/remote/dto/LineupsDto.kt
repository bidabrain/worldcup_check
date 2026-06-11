package com.worldcup2026.data.remote.dto

data class LineupsResponse(
    val home: LineupTeamDto?,
    val away: LineupTeamDto?
)

data class LineupTeamDto(
    val formation: String?,
    val players: List<LineupPlayerDto>?
)

data class LineupPlayerDto(
    val player: PlayerRefDto?,
    val jerseyNumber: Int?,
    val position: String?,
    val positionStringShort: String?,   // "GK","D","M","F"
    val substitute: Boolean?,
    val avgRating: RatingDto?,
    val coordinates: CoordinatesDto?
)

data class RatingDto(
    val rating: Float?
)

data class CoordinatesDto(
    val x: Float?,
    val y: Float?
)
