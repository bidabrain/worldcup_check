package com.worldcup2026.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import com.worldcup2026.domain.model.TOURNAMENTS
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
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
import com.worldcup2026.domain.model.Standing
import com.worldcup2026.ui.components.FlagEmoji
import com.worldcup2026.ui.theme.*

@Composable
fun StandingsScreen(viewModel: StandingsViewModel, onSupportClick: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WcBlue)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.align(Alignment.Center)) {
                Row(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.standings_title, state.tournament.label),
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
                    if (state.isRefreshing) {
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

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WcBlue)
                    }
                }
                state.groups.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_data), color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.errorResId?.let { resId ->
                            item {
                                Text(
                                    text = stringResource(resId),
                                    color = WcYellow,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        items(state.groups.entries.toList().sortedBy { it.key }) { (groupName, rows) ->
                            GroupCard(groupName = groupName, rows = rows)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(groupName: String, rows: List<Standing>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WcBlue)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = groupName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            StandingsHeaderRow()
            HorizontalDivider(color = CardBorder)

            rows.forEachIndexed { index, standing ->
                StandingRow(standing = standing, isQualified = index < 2)
                if (index < rows.lastIndex) HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun StandingsHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.col_rank), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
        Text(stringResource(R.string.col_team), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        for (resId in listOf(R.string.col_played, R.string.col_wins, R.string.col_draws, R.string.col_losses)) {
            Text(stringResource(resId), color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        }
        Text(stringResource(R.string.col_goal_diff), color = TextSecondary, fontSize = 11.sp,
            modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
        Text(stringResource(R.string.col_points), color = TextSecondary, fontSize = 11.sp,
            modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun StandingRow(standing: Standing, isQualified: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isQualified) WcBlue.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .then(if (isQualified) Modifier.clip(RoundedCornerShape(4.dp)).background(WcBlue) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = standing.rank.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isQualified) Color.White else TextSecondary
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            FlagEmoji(countryCode = standing.teamCountryCode, size = 20.dp, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = standing.teamName,
                fontSize = 13.sp,
                fontWeight = if (isQualified) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary,
                maxLines = 1
            )
        }

        for (v in listOf(standing.played, standing.wins, standing.draws, standing.losses)) {
            Text(v.toString(), fontSize = 13.sp, color = TextPrimary,
                modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        }

        val gdColor = when {
            standing.goalDiff > 0 -> WcGreen
            standing.goalDiff < 0 -> WcRed
            else -> TextSecondary
        }
        Text(
            text = if (standing.goalDiff > 0) "+${standing.goalDiff}" else standing.goalDiff.toString(),
            fontSize = 13.sp, color = gdColor,
            modifier = Modifier.width(32.dp), textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = standing.points.toString(),
            fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = if (isQualified) WcBlue else TextPrimary,
            modifier = Modifier.width(36.dp), textAlign = TextAlign.Center
        )
    }
}
