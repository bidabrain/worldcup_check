package com.worldcup2026.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.worldcup2026.data.local.dao.IncidentDao
import com.worldcup2026.data.local.dao.MatchDao
import com.worldcup2026.data.local.dao.StandingsDao
import com.worldcup2026.data.local.entity.IncidentEntity
import com.worldcup2026.data.local.entity.MatchEntity
import com.worldcup2026.data.local.entity.StandingEntity

@Database(
    entities = [MatchEntity::class, StandingEntity::class, IncidentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao
    abstract fun incidentDao(): IncidentDao
    abstract fun standingsDao(): StandingsDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "worldcup2026.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
