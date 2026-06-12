package com.worldcup2026.data.remote

import com.worldcup2026.data.remote.dto.*
import com.worldcup2026.data.remote.dto.CupTreeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SofaScoreApi {

    // All football matches on a given date (filter by uniqueTournamentId=16 in repo)
    @GET("sport/football/scheduled-events/{date}")
    suspend fun getScheduledEvents(
        @Path("date") date: String   // "YYYY-MM-DD"
    ): ScheduledEventsResponse

    // Live matches
    @GET("sport/football/events/live")
    suspend fun getLiveEvents(): ScheduledEventsResponse

    // Match detail
    @GET("event/{matchId}")
    suspend fun getMatchDetail(
        @Path("matchId") matchId: Int
    ): MatchDetailWrapper

    // Match incidents (goals, cards, subs)
    @GET("event/{matchId}/incidents")
    suspend fun getIncidents(
        @Path("matchId") matchId: Int
    ): IncidentsResponse

    // Match lineups
    @GET("event/{matchId}/lineups")
    suspend fun getLineups(
        @Path("matchId") matchId: Int
    ): LineupsResponse

    // Best players with ratings
    @GET("event/{matchId}/best-players/summary")
    suspend fun getBestPlayers(
        @Path("matchId") matchId: Int
    ): BestPlayersResponse

    // Match statistics
    @GET("event/{matchId}/statistics")
    suspend fun getStatistics(
        @Path("matchId") matchId: Int
    ): StatisticsResponse

    // Group standings  — uniqueTournamentId=16, seasonId=58210
    @GET("unique-tournament/{tournamentId}/season/{seasonId}/standings/total")
    suspend fun getStandings(
        @Path("tournamentId") tournamentId: Int = 16,
        @Path("seasonId") seasonId: Int = 58210
    ): StandingsResponse

    // Knockout bracket
    @GET("unique-tournament/{tournamentId}/season/{seasonId}/cuptrees")
    suspend fun getCupTree(
        @Path("tournamentId") tournamentId: Int = 16,
        @Path("seasonId") seasonId: Int = 58210
    ): CupTreeResponse
}

data class MatchDetailWrapper(val event: EventDto?)
