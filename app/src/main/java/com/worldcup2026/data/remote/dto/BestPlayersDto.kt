package com.worldcup2026.data.remote.dto

data class BestPlayersResponse(
    val bestHomeTeamPlayers: List<BestPlayerEntryDto>?,
    val bestAwayTeamPlayers: List<BestPlayerEntryDto>?
)

data class BestPlayerEntryDto(
    val value: String?,   // e.g. "8.6"
    val label: String?,   // "rating"
    val player: BestPlayerRefDto?
)

data class BestPlayerRefDto(
    val id: Int?
)
