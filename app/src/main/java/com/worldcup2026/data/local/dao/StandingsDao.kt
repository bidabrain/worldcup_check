package com.worldcup2026.data.local.dao

import androidx.room.*
import com.worldcup2026.data.local.entity.StandingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StandingsDao {

    @Query("SELECT * FROM standings ORDER BY groupName ASC, rank ASC")
    fun observeStandings(): Flow<List<StandingEntity>>

    @Query("DELETE FROM standings")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(standings: List<StandingEntity>)
}
