package com.worldcup2026.data.local.dao

import androidx.room.*
import com.worldcup2026.data.local.entity.IncidentEntity
import com.worldcup2026.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Query("SELECT * FROM matches WHERE startTimestamp >= :dayStart AND startTimestamp < :dayEnd ORDER BY startTimestamp ASC")
    fun observeMatchesByDay(dayStart: Long, dayEnd: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE statusCode IN (6, 7, 31, 32, 41) ORDER BY startTimestamp ASC")
    fun observeLiveMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatch(matchId: Int): MatchEntity?

    @Query("SELECT DISTINCT startTimestamp FROM matches ORDER BY startTimestamp ASC")
    suspend fun getAllMatchDays(): List<Long>

    @Upsert
    suspend fun upsertMatches(matches: List<MatchEntity>)

    @Upsert
    suspend fun upsertMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE cachedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    // Knockout bracket — lookup by country code (alpha2 / nameCode)
    @Query("""
        SELECT * FROM matches WHERE statusCode IN (100, 110, 120)
        AND ((homeTeamCountryCode = :codeA AND awayTeamCountryCode = :codeB)
          OR (homeTeamCountryCode = :codeB AND awayTeamCountryCode = :codeA))
        ORDER BY startTimestamp DESC LIMIT 1
    """)
    suspend fun findFinishedMatchByTeamCodes(codeA: String, codeB: String): MatchEntity?

    // Knockout bracket — fallback lookup by full or short name
    @Query("""
        SELECT * FROM matches WHERE statusCode IN (100, 110, 120)
        AND ((homeTeamName = :nameA AND awayTeamName = :nameB)
          OR (homeTeamName = :nameB AND awayTeamName = :nameA)
          OR (homeTeamShortName = :nameA AND awayTeamShortName = :nameB)
          OR (homeTeamShortName = :nameB AND awayTeamShortName = :nameA))
        ORDER BY startTimestamp DESC LIMIT 1
    """)
    suspend fun findFinishedMatchByTeamNames(nameA: String, nameB: String): MatchEntity?
}

@Dao
interface IncidentDao {

    @Query("SELECT * FROM incidents WHERE matchId = :matchId ORDER BY minute ASC")
    fun observeIncidents(matchId: Int): Flow<List<IncidentEntity>>

    @Upsert
    suspend fun upsertIncidents(incidents: List<IncidentEntity>)

    @Query("DELETE FROM incidents WHERE matchId = :matchId")
    suspend fun deleteForMatch(matchId: Int)
}
