package com.worldcup2026.data.remote

import com.worldcup2026.data.remote.dto.ApiLineupsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SofaScoreApiV2 {

    @GET("event/{matchId}/lineups")
    suspend fun getLineups(
        @Path("matchId") matchId: Int
    ): ApiLineupsResponse
}
