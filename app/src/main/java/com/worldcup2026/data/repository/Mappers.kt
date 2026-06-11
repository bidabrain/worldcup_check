package com.worldcup2026.data.repository

import com.worldcup2026.data.local.entity.IncidentEntity
import com.worldcup2026.data.local.entity.MatchEntity
import com.worldcup2026.data.local.entity.StandingEntity
import com.worldcup2026.data.remote.dto.*
import com.worldcup2026.domain.model.*

// ── EventDto → MatchEntity ───────────────────────────────────────────────────

fun EventDto.toEntity(): MatchEntity? {
    val home = homeTeam ?: return null
    val away = awayTeam ?: return null
    return MatchEntity(
        id = id,
        homeTeamId = home.id ?: 0,
        homeTeamName = home.name ?: "",
        homeTeamShortName = home.shortName ?: home.name ?: "",
        homeTeamCountryCode = home.country?.alpha2 ?: home.country?.alpha3 ?: "",
        awayTeamId = away.id ?: 0,
        awayTeamName = away.name ?: "",
        awayTeamShortName = away.shortName ?: away.name ?: "",
        awayTeamCountryCode = away.country?.alpha2 ?: away.country?.alpha3 ?: "",
        homeScore = homeScore?.current,
        awayScore = awayScore?.current,
        startTimestamp = startTimestamp ?: 0L,
        statusCode = status?.code ?: 0,
        roundName = roundInfo?.name ?: roundInfo?.round?.toString() ?: "",
        tournamentName = tournament?.name ?: "",
        winnerCode = winnerCode,
        cachedAt = System.currentTimeMillis()
    )
}

// ── MatchEntity → Match (domain) ─────────────────────────────────────────────

fun MatchEntity.toDomain() = Match(
    id = id,
    homeTeam = Team(homeTeamId, homeTeamName, homeTeamShortName, homeTeamCountryCode),
    awayTeam = Team(awayTeamId, awayTeamName, awayTeamShortName, awayTeamCountryCode),
    homeScore = homeScore,
    awayScore = awayScore,
    startTimestamp = startTimestamp,
    status = statusCode.toMatchStatus(),
    roundName = roundName,
    tournamentName = tournamentName,
    winnerCode = winnerCode
)

private fun Int.toMatchStatus() = when (this) {
    0 -> MatchStatus.NOT_STARTED
    6, 7,           // 1st / 2nd half
    31,             // extra time
    32,             // extra time half-time
    41              // penalties
        -> MatchStatus.IN_PROGRESS
    100,            // finished (regular time)
    110,            // finished after extra time
    120             // finished after penalties
        -> MatchStatus.FINISHED
    else -> MatchStatus.NOT_STARTED
}

// ── StandingRowDto → StandingEntity ──────────────────────────────────────────

fun StandingRowDto.toEntity(groupName: String): StandingEntity? {
    val t = team ?: return null
    val gf = scoresFor ?: 0
    val ga = scoresAgainst ?: 0
    return StandingEntity(
        rank = position ?: 0,
        teamId = t.id ?: 0,
        teamName = t.name ?: "",
        teamCountryCode = t.country?.alpha2 ?: t.country?.alpha3 ?: "",
        played = matches ?: 0,
        wins = wins ?: 0,
        draws = draws ?: 0,
        losses = losses ?: 0,
        goalsFor = gf,
        goalsAgainst = ga,
        goalDiff = gf - ga,
        points = points ?: 0,
        groupName = groupName,
        cachedAt = System.currentTimeMillis()
    )
}

fun StandingEntity.toDomain() = Standing(
    rank = rank,
    teamId = teamId,
    teamName = teamName,
    teamCountryCode = teamCountryCode,
    played = played,
    wins = wins,
    draws = draws,
    losses = losses,
    goalsFor = goalsFor,
    goalsAgainst = goalsAgainst,
    goalDiff = goalDiff,
    points = points,
    groupName = groupName
)

// ── IncidentDto → IncidentEntity ─────────────────────────────────────────────

fun IncidentDto.toEntity(matchId: Int): IncidentEntity? {
    val incId = id ?: return null
    val type = resolveType(incidentType, incidentClass)
    // Substitution: SofaScore may use player=playerIn or explicit playerIn/playerOut fields
    val playerName = if (type == "SUBSTITUTION") {
        val inPlayer = (playerIn ?: player)?.let { it.shortName ?: it.name }
        val outPlayer = playerOut?.let { it.shortName ?: it.name }
        when {
            inPlayer != null && outPlayer != null -> "$inPlayer ↑  $outPlayer ↓"
            inPlayer != null -> "$inPlayer ↑"
            outPlayer != null -> "$outPlayer ↓"
            else -> null
        }
    } else {
        player?.shortName ?: player?.name
    }
    return IncidentEntity(
        id = incId,
        matchId = matchId,
        minute = time,
        incidentType = type,
        playerName = playerName,
        isHome = isHome ?: true
    )
}

private fun resolveType(type: String?, cls: String?) = when {
    type == "goal" && cls == "ownGoal"  -> "OWN_GOAL"
    type == "goal" && cls == "penalty"  -> "PENALTY_GOAL"
    type == "goal"                      -> "GOAL"
    type == "card" && cls == "yellow"   -> "YELLOW_CARD"
    type == "card" && cls == "red"      -> "RED_CARD"
    type == "substitution"              -> "SUBSTITUTION"
    type == "penaltyShootout" && cls == "missed" -> "PENALTY_MISSED"
    type == "penaltyShootout"           -> "PENALTY_SCORED"   // scored / default
    else -> "OTHER"
}

fun IncidentEntity.toDomain() = Incident(
    id = id,
    matchId = matchId,
    minute = minute,
    type = incidentTypeFromString(incidentType),
    playerName = playerName,
    isHome = isHome
)

fun incidentTypeFromString(s: String) = when (s) {
    "GOAL"            -> IncidentType.GOAL
    "OWN_GOAL"        -> IncidentType.OWN_GOAL
    "PENALTY_GOAL"    -> IncidentType.PENALTY_GOAL
    "YELLOW_CARD"     -> IncidentType.YELLOW_CARD
    "RED_CARD"        -> IncidentType.RED_CARD
    "SUBSTITUTION"    -> IncidentType.SUBSTITUTION
    "PENALTY_SCORED"  -> IncidentType.PENALTY_SCORED
    "PENALTY_MISSED"  -> IncidentType.PENALTY_MISSED
    else              -> IncidentType.OTHER
}
