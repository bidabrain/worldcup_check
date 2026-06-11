package com.worldcup2026

import android.app.Application
import com.worldcup2026.data.local.AppDatabase
import com.worldcup2026.data.remote.NetworkClient
import com.worldcup2026.data.repository.MatchRepository

class WorldCupApp : Application() {

    val repository: MatchRepository by lazy {
        val db = AppDatabase.getInstance(this)
        MatchRepository(
            matchDao = db.matchDao(),
            incidentDao = db.incidentDao(),
            standingsDao = db.standingsDao(),
            api = NetworkClient.api
        )
    }
}
