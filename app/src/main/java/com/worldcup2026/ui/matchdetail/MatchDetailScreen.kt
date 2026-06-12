package com.worldcup2026.ui.matchdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup2026.R
import com.worldcup2026.domain.model.*
import com.worldcup2026.ui.components.FlagEmoji
import com.worldcup2026.ui.theme.*
import com.worldcup2026.ui.util.translateStatName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> viewModel.loadStats()
            2 -> viewModel.loadLineup()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        // ── Score header ───────────────────────────────────────────────────
        ScoreHeader(match = state.match, onBack = onBack)

        // ── Tabs ───────────────────────────────────────────────────────────
        val tabs = listOf(
            stringResource(R.string.tab_events),
            stringResource(R.string.tab_stats),
            stringResource(R.string.tab_lineup)
        )
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WcBlue,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = WcGold
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Tab content ────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> IncidentsTab(incidents = state.incidents, match = state.match)
            1 -> StatsTab(stats = state.stats, isLoading = state.isLoadingStats, error = state.statsError)
            2 -> LineupTab(lineup = state.lineup, isLoading = state.isLoadingLineup, error = state.lineupError)
        }
    }
}

// ── Score Header ──────────────────────────────────────────────────────────────

@Composable
private fun ScoreHeader(match: Match?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(WcBlueDark, WcBlue)))
            .statusBarsPadding()
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (match == null) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                return@Column
            }

            // Round / Status
            val statusLabel = when (match.status) {
                MatchStatus.FINISHED    -> stringResource(R.string.status_finished)
                MatchStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
                MatchStatus.NOT_STARTED -> {
                    val fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                    Instant.ofEpochSecond(match.startTimestamp)
                        .atZone(ZoneId.systemDefault()).format(fmt)
                }
                else -> match.status.name
            }
            Text(
                text = "${match.roundName}  ·  $statusLabel",
                color = WcGoldLight,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))

            // Teams + Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    FlagEmoji(countryCode = match.homeTeam.countryCode, size = 56.dp, fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(match.homeTeam.name, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }

                // Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (match.status) {
                        MatchStatus.NOT_STARTED -> Text("VS", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        else -> Text(
                            "${match.homeScore ?: 0}  -  ${match.awayScore ?: 0}",
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Away
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    FlagEmoji(countryCode = match.awayTeam.countryCode, size = 56.dp, fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(match.awayTeam.name, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Incidents Tab ─────────────────────────────────────────────────────────────

@Composable
private fun IncidentsTab(incidents: List<Incident>, match: Match?) {
    if (incidents.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_events), color = TextSecondary)
        }
        return
    }

    // Split regular incidents from penalty shootout kicks
    val regular = incidents.filter {
        it.type != IncidentType.PENALTY_SCORED && it.type != IncidentType.PENALTY_MISSED
    }
    val shootout = incidents.filter {
        it.type == IncidentType.PENALTY_SCORED || it.type == IncidentType.PENALTY_MISSED
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(regular) { incident ->
            IncidentRow(incident = incident, match = match)
        }
        if (shootout.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(WcBlue.copy(alpha = 0.12f))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.penalty_shootout), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WcBlue)
                }
            }
            items(shootout) { incident ->
                IncidentRow(incident = incident, match = match)
            }
        }
    }
}

@Composable
private fun IncidentRow(incident: Incident, match: Match?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isHome = incident.isHome

        if (isHome) {
            Text(
                text = incident.playerName ?: "",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }

        // Icon + Minute/Label
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
            Text(
                text = incidentIcon(incident.type),
                fontSize = 18.sp
            )
            val minuteLabel = when {
                incident.minute != null -> "${incident.minute}′"
                incident.type == IncidentType.PENALTY_SCORED ||
                incident.type == IncidentType.PENALTY_MISSED -> "PK"
                else -> ""
            }
            if (minuteLabel.isNotEmpty()) {
                Text(text = minuteLabel, fontSize = 11.sp, color = TextSecondary)
            }
        }

        if (!isHome) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = incident.playerName ?: "",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

private fun incidentIcon(type: IncidentType) = when (type) {
    IncidentType.GOAL            -> "⚽"
    IncidentType.OWN_GOAL        -> "⚽"
    IncidentType.PENALTY_GOAL    -> "⚽"
    IncidentType.YELLOW_CARD     -> "🟨"
    IncidentType.RED_CARD        -> "🟥"
    IncidentType.SUBSTITUTION    -> "🔄"
    IncidentType.PENALTY_SCORED  -> "✅"
    IncidentType.PENALTY_MISSED  -> "❌"
    else                         -> "•"
}

// ── Stats Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsTab(stats: List<StatItem>, isLoading: Boolean, error: String?) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WcBlue)
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = TextSecondary)
        }
        stats.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_stats), color = TextSecondary)
        }
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { item -> StatRow(item) }
        }
    }
}

@Composable
private fun StatRow(item: StatItem) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.homeValue, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(translateStatName(item.name), color = TextSecondary, fontSize = 12.sp)
            Text(item.awayValue, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        if (item.homePercent != null && item.awayPercent != null) {
            val hw = item.homePercent.coerceAtLeast(0.001f)
            val aw = item.awayPercent.coerceAtLeast(0.001f)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                Box(modifier = Modifier.weight(hw).fillMaxHeight().background(WcBlue))
                Box(modifier = Modifier.weight(aw).fillMaxHeight().background(WcRed))
            }
        }
    }
}

// ── Lineup Tab ────────────────────────────────────────────────────────────────

@Composable
private fun LineupTab(lineup: Lineup?, isLoading: Boolean, error: String?) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WcBlue)
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = TextSecondary)
        }
        lineup == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_lineup), color = TextSecondary)
        }
        else -> FootballPitch(lineup = lineup)
    }
}

// Use SofaScore coordinates when available, fall back to formation-based positioning.
// SofaScore uses a 0-100 scale (auto-detected: if value > 1 → divide by 100).
// Coordinate system: x = left→right, y = 0 near home goal → 100 near away goal.
// We map home players to the top half (y: 0.04–0.46) and away to the bottom (0.54–0.96).
private fun resolvePosition(
    rawX: Float?,
    rawY: Float?,
    fallback: Pair<Float, Float>,
    isHome: Boolean
): Pair<Float, Float> {
    if (rawX == null || rawY == null) return fallback

    // Normalize to 0-1
    val scale = if (rawX > 1f || rawY > 1f) 100f else 1f
    val nx = (rawX / scale).coerceIn(0f, 1f)
    val ny = (rawY / scale).coerceIn(0f, 1f)

    // Map into the correct half of the pitch
    return if (isHome) {
        Pair(nx, 0.04f + ny * 0.42f)       // top half:    y 0.04 → 0.46
    } else {
        Pair(1f - nx, 0.96f - ny * 0.42f)  // bottom half: y 0.96 → 0.54, x mirrored
    }
}

// Parse formation string "4-3-3" → listOf(4, 3, 3)
private fun parseFormation(formation: String?): List<Int> {
    if (formation == null) return emptyList()
    return formation.split("-").mapNotNull { it.trim().toIntOrNull() }
}

// Assign (x 0..1, y 0..1) positions to players based on formation rows.
// y=0 = home goal line, y=1 = away goal line.
// isHome=true  → GK near y=0, forwards near y=0.45
// isHome=false → GK near y=1, forwards near y=0.55
private fun formationPositions(players: List<LineupPlayer>, formation: String?, isHome: Boolean): List<Pair<Float, Float>> {
    val rows = parseFormation(formation)
    val result = mutableListOf<Pair<Float, Float>>()

    // Y levels for home: GK=0.05, rows spread toward 0.45
    // Y levels for away: GK=0.95, rows spread toward 0.55 (mirrored)
    val gkY  = if (isHome) 0.05f else 0.95f

    // Outfield row Y positions (home: 0.20 → 0.44, away: 0.80 → 0.56)
    val rowCount = rows.size
    val rowYs = if (rowCount == 0) emptyList() else {
        if (isHome) List(rowCount) { i -> 0.20f + i * (0.44f - 0.20f) / (rowCount - 1).coerceAtLeast(1) }
        else        List(rowCount) { i -> 0.80f - i * (0.80f - 0.56f) / (rowCount - 1).coerceAtLeast(1) }
    }

    // GK first
    result.add(Pair(0.5f, gkY))

    // Outfield players row by row
    var idx = 1
    rows.forEachIndexed { rowIdx, count ->
        val y = rowYs.getOrElse(rowIdx) { if (isHome) 0.35f else 0.65f }
        for (pos in 0 until count) {
            val x = if (count == 1) 0.5f
                    else 0.1f + pos * 0.8f / (count - 1)
            result.add(Pair(x, y))
            idx++
        }
    }

    // Fallback: remaining players stacked in center
    while (result.size < players.size) {
        result.add(Pair(0.5f, if (isHome) 0.30f else 0.70f))
    }

    return result
}

@Composable
private fun FootballPitch(lineup: Lineup) {
    val homePositions = formationPositions(lineup.homePlayers, lineup.homeFormation, isHome = true)
    val awayPositions = formationPositions(lineup.awayPlayers, lineup.awayFormation, isHome = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Formations label
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(lineup.homeFormation ?: "-", fontWeight = FontWeight.Bold, color = WcBlue)
            Text(stringResource(R.string.formation_label), color = TextSecondary, fontSize = 12.sp)
            Text(lineup.awayFormation ?: "-", fontWeight = FontWeight.Bold, color = WcRed)
        }

        // Pitch
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2E7D32))
        ) {
            val pitchW = maxWidth
            val pitchH = maxHeight
            val dotHalfW = 32.dp   // half of PlayerDot column width (64dp)

            // Center circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            // Center line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            // Home players
            lineup.homePlayers.forEachIndexed { i, player ->
                val (fx, fy) = resolvePosition(
                    rawX = player.positionX,
                    rawY = player.positionY,
                    fallback = homePositions.getOrElse(i) { Pair(0.5f, 0.25f) },
                    isHome = true
                )
                PlayerDot(
                    player = player,
                    isHome = true,
                    modifier = Modifier.offset(pitchW * fx - dotHalfW, pitchH * fy - dotHalfW)
                )
            }

            // Away players
            lineup.awayPlayers.forEachIndexed { i, player ->
                val (fx, fy) = resolvePosition(
                    rawX = player.positionX,
                    rawY = player.positionY,
                    fallback = awayPositions.getOrElse(i) { Pair(0.5f, 0.75f) },
                    isHome = false
                )
                PlayerDot(
                    player = player,
                    isHome = false,
                    modifier = Modifier.offset(pitchW * fx - dotHalfW, pitchH * fy - dotHalfW)
                )
            }
        }
    }
}

@Composable
private fun PlayerDot(player: LineupPlayer, isHome: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (player.rating != null) {
            val ratingColor = when {
                player.rating >= 8.0f -> Color(0xFF4CAF50)  // 绿色
                player.rating >= 7.0f -> Color(0xFFFFD700)  // 金黄
                player.rating >= 6.0f -> Color(0xFFFF9800)  // 橙色
                else                  -> Color(0xFFF44336)  // 红色
            }
            Text(
                text = String.format("%.1f", player.rating),
                fontSize = 9.sp,
                color = ratingColor,
                fontWeight = FontWeight.Bold
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (isHome) WcBlue else WcRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.jerseyNumber?.toString() ?: player.position?.take(1) ?: "?",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = player.shortName,
            fontSize = 9.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
    }
}
