package com.worldcup2026.data.remote.dto

data class CupTreeResponse(
    val cupTrees: List<CupTreeDto>?
)

data class CupTreeDto(
    val rounds: List<CupRoundDto>?
)

data class CupRoundDto(
    val description: String?,
    val order: Int?,
    val blocks: List<CupBlockDto>?
)

data class CupBlockDto(
    val id: Int?,
    val participants: List<CupParticipantDto>?,
    val homeTeamScore: String?,
    val awayTeamScore: String?,
    val result: String?,
    val finished: Boolean?
)

data class CupParticipantDto(
    val team: CupTeamDto?,
    val winner: Boolean?,
    val order: Int?     // 1 = home, 2 = away
)

data class CupTeamDto(
    val name: String?,
    val shortName: String?,
    val nameCode: String?,          // FIFA 3-letter code e.g. "NED","USA"
    val country: CountryDto?
)
