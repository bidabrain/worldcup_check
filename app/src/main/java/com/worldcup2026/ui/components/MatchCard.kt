package com.worldcup2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.worldcup2026.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup2026.domain.model.Match
import com.worldcup2026.domain.model.MatchStatus
import com.worldcup2026.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // Round label
            Text(
                text = match.roundName.ifBlank { match.tournamentName },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Home team
                TeamColumn(
                    countryCode = match.homeTeam.countryCode,
                    name = match.homeTeam.shortName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Score / Time
                ScoreOrTime(match = match, modifier = Modifier.weight(1f))

                // Away team
                TeamColumn(
                    countryCode = match.awayTeam.countryCode,
                    name = match.awayTeam.shortName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(
    countryCode: String,
    name: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FlagEmoji(countryCode = countryCode, size = 36.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            color = TextPrimary
        )
    }
}

@Composable
private fun ScoreOrTime(match: Match, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (match.status) {
            MatchStatus.FINISHED, MatchStatus.IN_PROGRESS -> {
                if (match.status == MatchStatus.IN_PROGRESS) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WcRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.live_badge),
                            color = SurfaceWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = "${match.homeScore ?: 0}  -  ${match.awayScore ?: 0}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            else -> {
                val time = Instant.ofEpochSecond(match.startTimestamp)
                    .atZone(ZoneId.systemDefault())
                val fmt = DateTimeFormatter.ofPattern("HH:mm")
                Text(
                    text = time.format(fmt),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WcBlue
                )
                Text(
                    text = stringResource(R.string.match_not_started),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
