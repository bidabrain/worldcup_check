package com.worldcup2026.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScheduledEventsResponse(
    val events: List<EventDto>?
)

data class EventDto(
    val id: Int,
    val homeTeam: TeamDto?,
    val awayTeam: TeamDto?,
    val homeScore: ScoreDto?,
    val awayScore: ScoreDto?,
    val startTimestamp: Long?,
    val status: StatusDto?,
    val tournament: TournamentDto?,
    val roundInfo: RoundInfoDto?,
    val winnerCode: Int?
)

data class TeamDto(
    val id: Int?,
    val name: String?,
    val shortName: String?,
    val country: CountryDto?
)

data class CountryDto(
    val alpha2: String?,
    val alpha3: String?
)

data class ScoreDto(
    val current: Int?,
    val display: Int?
)

data class StatusDto(
    val code: Int?,      // 0=not started, 6/7=in progress, 100=finished
    val type: String?,
    val description: String?
)

data class TournamentDto(
    val id: Int?,
    val name: String?,
    val uniqueTournament: UniqueTournamentDto?
)

data class UniqueTournamentDto(
    val id: Int?,
    val name: String?
)

data class RoundInfoDto(
    val round: Int?,
    val name: String?,
    @SerializedName("slugName") val slugName: String?
)
