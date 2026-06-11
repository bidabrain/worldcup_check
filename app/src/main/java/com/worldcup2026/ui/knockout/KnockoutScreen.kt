package com.worldcup2026.ui.knockout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup2026.R
import com.worldcup2026.domain.model.KnockoutMatch
import com.worldcup2026.domain.model.KnockoutRound
import com.worldcup2026.domain.model.TOURNAMENTS
import com.worldcup2026.ui.components.FlagEmoji
import com.worldcup2026.ui.theme.*

@Composable
fun KnockoutScreen(
    viewModel: KnockoutViewModel,
    onMatchClick: (Int) -> Unit,
    onSupportClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        // ── Top bar ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WcBlue)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.align(Alignment.Center)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.knockout_title, state.tournament.label),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = WcGoldLight)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TOURNAMENTS.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tournament_picker_label, t.label)) },
                            onClick = { viewModel.selectTournament(t); expanded = false }
                        )
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.refresh() }, modifier = Modifier.size(40.dp)) {
                    if (state.isRefreshing || state.isSyncing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = Color.White)
                    }
                }
                IconButton(onClick = onSupportClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.support_developer),
                        tint = WcGoldLight, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WcBlue)
                    }
                }
                state.rounds.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.no_knockout_data), color = TextSecondary)
                            state.errorResId?.let { resId ->
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(resId), color = WcYellow, fontSize = 12.sp)
                            }
                        }
                    }
                }
                else -> {
                    // Sort rounds: chronological order, 3rd place last
                    val sorted = state.rounds.sortedWith(compareBy(
                        { if (it.name.contains("3rd", ignoreCase = true) || it.name.contains("third", ignoreCase = true)) 1 else 0 },
                        { it.order }
                    ))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        state.errorResId?.let { resId ->
                            Text(stringResource(resId), color = WcYellow, fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                        if (state.isSyncing) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = WcBlue, modifier = Modifier.size(14.dp), strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.syncing_match_data), fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        // Horizontal scrollable bracket
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            sorted.forEach { round ->
                                RoundColumn(round = round, onMatchClick = onMatchClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundColumn(round: KnockoutRound, onMatchClick: (Int) -> Unit) {
    val displayName = roundDisplayName(round.name)

    Column(
        modifier = Modifier.width(148.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Round header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(WcBlue)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            round.matches.forEach { match ->
                MatchCard(match = match, onMatchClick = onMatchClick)
            }
        }
    }
}

@Composable
private fun MatchCard(match: KnockoutMatch, onMatchClick: (Int) -> Unit) {
    val clickable = match.matchId != null
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(if (clickable) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .then(
                if (clickable) Modifier.clickable { onMatchClick(match.matchId!!) }
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
            TeamRow(
                teamName = match.homeTeamName,
                teamCode = match.homeTeamCode,
                score = match.homeScore,
                isWinner = match.homeWinner
            )
            HorizontalDivider(
                color = CardBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            TeamRow(
                teamName = match.awayTeamName,
                teamCode = match.awayTeamCode,
                score = match.awayScore,
                isWinner = match.awayWinner
            )
            if (clickable) {
                Text(
                    text = stringResource(R.string.view_details),
                    fontSize = 9.sp,
                    color = WcBlue.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun TeamRow(
    teamName: String,
    teamCode: String,
    score: String?,
    isWinner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (teamCode.isNotEmpty()) {
            FlagEmoji(countryCode = teamCode, size = 16.dp)
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = teamName,
            fontSize = 12.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            color = if (isWinner) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        if (score != null) {
            Text(
                text = score,
                fontSize = 13.sp,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (isWinner) WcBlue else TextSecondary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun roundDisplayName(name: String): String = when {
    name.contains("16", ignoreCase = true)                                              -> stringResource(R.string.round_of_16)
    name.contains("Quarter", ignoreCase = true)                                         -> stringResource(R.string.quarterfinals)
    name.contains("Semi", ignoreCase = true)                                            -> stringResource(R.string.semifinals)
    name.contains("3rd", ignoreCase = true) || name.contains("third", ignoreCase = true) -> stringResource(R.string.third_place_match)
    name.contains("Final", ignoreCase = true)                                           -> stringResource(R.string.final_match)
    else                                                                                -> name
}
