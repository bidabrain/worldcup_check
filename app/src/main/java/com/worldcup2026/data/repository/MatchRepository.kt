package com.worldcup2026.data.repository

import com.worldcup2026.data.local.dao.IncidentDao
import com.worldcup2026.data.local.dao.MatchDao
import com.worldcup2026.data.local.dao.StandingsDao
import com.worldcup2026.data.remote.SofaScoreApi
import com.worldcup2026.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val WC_TOURNAMENT_ID = 16

class MatchRepository(
    private val matchDao: MatchDao,
    private val incidentDao: IncidentDao,
    private val standingsDao: StandingsDao,
    private val api: SofaScoreApi
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ── Observe cached matches for a given local date ─────────────────────────

    fun observeMatchesByDate(date: LocalDate): Flow<List<Match>> {
        val zone = ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zone).toEpochSecond()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toEpochSecond()
        return matchDao.observeMatchesByDay(dayStart, dayEnd)
            .map { list -> list.map { it.toDomain() } }
    }

    fun observeLiveMatches(): Flow<List<Match>> =
        matchDao.observeLiveMatches().map { list -> list.map { it.toDomain() } }

    // ── Sync from network ─────────────────────────────────────────────────────

    suspend fun syncDate(date: LocalDate, tournamentId: Int = WC_TOURNAMENT_ID): Result<Unit> = runCatching {
        val dateStr = date.format(dateFormatter)
        val response = api.getScheduledEvents(dateStr)
        val entities = response.events
            ?.filter { it.tournament?.uniqueTournament?.id == tournamentId }
            ?.mapNotNull { it.toEntity() }
            ?: emptyList()
        matchDao.upsertMatches(entities)
    }

    suspend fun syncLive(): Result<Unit> = runCatching {
        val response = api.getLiveEvents()
        val entities = response.events
            ?.filter { it.tournament?.uniqueTournament?.id == WC_TOURNAMENT_ID }
            ?.mapNotNull { it.toEntity() }
            ?: emptyList()
        matchDao.upsertMatches(entities)
    }

    // ── Match detail + incidents ──────────────────────────────────────────────

    suspend fun getMatch(matchId: Int): Match? =
        matchDao.getMatch(matchId)?.toDomain()

    suspend fun syncMatchDetail(matchId: Int): Result<Unit> = runCatching {
        val wrapper = api.getMatchDetail(matchId)
        wrapper.event?.toEntity()?.let { matchDao.upsertMatch(it) }
    }

    fun observeIncidents(matchId: Int): Flow<List<Incident>> =
        incidentDao.observeIncidents(matchId).map { list -> list.map { it.toDomain() } }

    suspend fun syncIncidents(matchId: Int): Result<Unit> = runCatching {
        val response = api.getIncidents(matchId)
        val entities = response.incidents
            ?.mapNotNull { it.toEntity(matchId) }
            ?: emptyList()
        incidentDao.upsertIncidents(entities)
    }

    // ── Lineups (not cached, fetched on demand) ───────────────────────────────

    suspend fun getLineups(matchId: Int): Result<Lineup> = runCatching {
        val response = api.getLineups(matchId)

        fun mapPlayers(teamDto: com.worldcup2026.data.remote.dto.LineupTeamDto?) =
            teamDto?.players
                ?.filter { it.substitute != true }
                ?.map { p ->
                    LineupPlayer(
                        id = p.player?.id ?: 0,
                        name = p.player?.name ?: "",
                        shortName = p.player?.shortName ?: p.player?.name ?: "",
                        jerseyNumber = p.jerseyNumber,
                        position = p.positionStringShort,
                        positionX = p.coordinates?.x,
                        positionY = p.coordinates?.y,
                        rating = p.avgRating?.rating
                    )
                } ?: emptyList()

        Lineup(
            matchId = matchId,
            homeFormation = response.home?.formation,
            awayFormation = response.away?.formation,
            homePlayers = mapPlayers(response.home),
            awayPlayers = mapPlayers(response.away)
        )
    }

    // ── Statistics (not cached, fetched on demand) ────────────────────────────

    suspend fun getStatistics(matchId: Int): Result<List<StatItem>> = runCatching {
        val response = api.getStatistics(matchId)
        val allPeriod = response.statistics?.firstOrNull { it.period == "ALL" }
        allPeriod?.groups?.flatMap { group ->
            group.statisticsItems?.mapNotNull { item ->
                val name = item.name ?: return@mapNotNull null
                StatItem(
                    name = name,
                    homeValue = item.home ?: "-",
                    awayValue = item.away ?: "-",
                    homePercent = item.homeValue?.let { h ->
                        item.awayValue?.let { a ->
                            if (h + a > 0) h / (h + a) else null
                        }
                    },
                    awayPercent = item.awayValue?.let { a ->
                        item.homeValue?.let { h ->
                            if (h + a > 0) a / (h + a) else null
                        }
                    }
                )
            } ?: emptyList()
        } ?: emptyList()
    }

    // ── Standings ─────────────────────────────────────────────────────────────

    fun observeStandings(): Flow<Map<String, List<Standing>>> =
        standingsDao.observeStandings().map { list ->
            list.map { it.toDomain() }.groupBy { it.groupName }
        }

    suspend fun syncStandings(seasonId: Int): Result<Unit> = runCatching {
        val response = api.getStandings(WC_TOURNAMENT_ID, seasonId)
        val entities = response.standings?.flatMap { group ->
            group.rows?.mapNotNull { row -> row.toEntity(group.name ?: "?") } ?: emptyList()
        } ?: emptyList()
        standingsDao.deleteAll()
        standingsDao.insertAll(entities)
    }

    // ── Knockout bracket (not cached, fetched on demand) ─────────────────────

    suspend fun getKnockout(seasonId: Int): Result<List<KnockoutRound>> = runCatching {
        val response = api.getCupTree(WC_TOURNAMENT_ID, seasonId)
        response.cupTrees?.firstOrNull()?.rounds
            ?.sortedBy { it.order }
            ?.map { round ->
                KnockoutRound(
                    name = round.description ?: "",
                    order = round.order ?: 0,
                    matches = round.blocks?.mapNotNull { block ->
                        val home = block.participants?.firstOrNull { it.order == 1 }
                        val away = block.participants?.firstOrNull { it.order == 2 }
                        run {
                            val homeName = home?.team?.shortName ?: home?.team?.name ?: "TBD"
                            val awayName = away?.team?.shortName ?: away?.team?.name ?: "TBD"
                            val homeCode = home?.team?.country?.alpha2 ?: home?.team?.nameCode ?: ""
                            val awayCode = away?.team?.country?.alpha2 ?: away?.team?.nameCode ?: ""
                            // 1. Try by country code (most reliable — same source as schedule API)
                            // 2. Fall back to team name match
                            val dbMatch =
                                matchDao.findFinishedMatchByTeamCodes(homeCode, awayCode)
                                    .takeIf { homeCode.isNotEmpty() && awayCode.isNotEmpty() }
                                    ?: matchDao.findFinishedMatchByTeamNames(homeName, awayName)
                            KnockoutMatch(
                                homeTeamName = homeName,
                                homeTeamCode = homeCode,
                                awayTeamName = awayName,
                                awayTeamCode = awayCode,
                                homeScore = block.homeTeamScore,
                                awayScore = block.awayTeamScore,
                                finished = block.finished ?: false,
                                homeWinner = home?.winner ?: false,
                                awayWinner = away?.winner ?: false,
                                matchId = dbMatch?.id
                            )
                        }
                    } ?: emptyList()
                )
            } ?: emptyList()
    }

    // ── Helper: all match days stored in cache ────────────────────────────────

    suspend fun getCachedMatchDays(): List<LocalDate> {
        val zone = ZoneId.systemDefault()
        return matchDao.getAllMatchDays()
            .map { ts -> java.time.Instant.ofEpochSecond(ts).atZone(zone).toLocalDate() }
            .distinct()
            .sorted()
    }
}
