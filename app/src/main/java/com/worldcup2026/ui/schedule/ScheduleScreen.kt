package com.worldcup2026.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
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
import com.worldcup2026.domain.model.TOURNAMENTS
import com.worldcup2026.ui.components.MatchCard
import com.worldcup2026.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
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
            TournamentPicker(
                selected = state.tournament.label,
                onSelect = { viewModel.selectTournament(it) },
                modifier = Modifier.align(Alignment.Center)
            )
            // Live polling indicator (left)
            if (state.isLivePolling) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(WcRed)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.live_indicator), color = WcRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            // Right-side icons
            Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                if (state.isOffline) {
                    Icon(Icons.Default.CloudOff, contentDescription = stringResource(R.string.offline),
                        tint = WcGoldLight, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onSupportClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.support_developer),
                        tint = WcGoldLight, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Month Calendar ─────────────────────────────────────────────────
        MonthCalendar(
            selectedDate = state.selectedDate,
            displayMonth = state.displayMonth,
            matchDays = state.matchDays,
            tournamentStart = state.tournament.startDate,
            tournamentEnd = state.tournament.endDate,
            onDateSelected = { viewModel.selectDate(it) },
            onMonthChange = { viewModel.selectMonth(it) }
        )

        // ── Match list ─────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WcBlue)
                    }
                }
                state.matches.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (state.isOffline) stringResource(R.string.no_cached_data)
                                       else stringResource(R.string.no_matches_today),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (state.isOffline) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(containerColor = WcBlue)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        state.errorResId?.let { resId ->
                            item { OfflineBanner(message = stringResource(resId)) }
                        }
                        items(state.matches, key = { it.id }) { match ->
                            MatchCard(match = match, onClick = { onMatchClick(match.id) })
                        }
                    }
                }
            }

            if (!state.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = WcBlue,
                    contentColor = Color.White
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    selectedDate: LocalDate,
    displayMonth: YearMonth,
    matchDays: Set<LocalDate>,
    tournamentStart: LocalDate,
    tournamentEnd: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val today = LocalDate.now()
    val monthPattern = stringResource(R.string.month_format)
    val monthFmt = DateTimeFormatter.ofPattern(monthPattern)

    // Locale-aware week header labels (Mon–Sun), short names
    val locale = Locale.getDefault()
    val weekLabels = (1..7).map { DayOfWeek.of(it).getDisplayName(TextStyle.NARROW, locale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WcBlueDark)
            .padding(bottom = 8.dp)
    ) {
        // Month navigation row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onMonthChange(displayMonth.minusMonths(1)) },
                enabled = displayMonth.minusMonths(1).atEndOfMonth() >= tournamentStart
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.prev_month), tint = Color.White)
            }
            Text(
                text = displayMonth.format(monthFmt),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { onMonthChange(displayMonth.plusMonths(1)) },
                enabled = displayMonth.plusMonths(1).atDay(1) <= tournamentEnd.plusDays(1)
                    || matchDays.any { YearMonth.from(it) == displayMonth.plusMonths(1) }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next_month), tint = Color.White)
            }
        }

        // Week day header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekLabels.forEach { label ->
                Text(
                    text = label,
                    color = WcGoldLight.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Build calendar grid
        val firstOfMonth = displayMonth.atDay(1)
        // Monday-based: Monday=0 … Sunday=6
        val startOffset = (firstOfMonth.dayOfWeek.value - 1) // Mon=1 → offset=0
        val daysInMonth = displayMonth.lengthOfMonth()
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    } else {
                        val date = displayMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isMatchDay = date in matchDays
                        // A date is "active" if it's within the tournament window (with +1 day buffer
                        // for timezone: e.g. a UTC Dec-18 final becomes Dec-19 local in UTC+9),
                        // OR if we already have cached matches for it.
                        val inTournament = (!date.isBefore(tournamentStart) && !date.isAfter(tournamentEnd.plusDays(1)))
                            || date in matchDays

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> WcGold
                                        isToday -> WcBlueLight.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (inTournament) Modifier.clickable { onDateSelected(date) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> WcBlueDark
                                        inTournament -> Color.White
                                        else -> Color.White.copy(alpha = 0.3f)
                                    }
                                )
                                if (isMatchDay) {
                                    Icon(
                                        imageVector = Icons.Default.SportsSoccer,
                                        contentDescription = null,
                                        tint = if (isSelected) WcBlueDark else WcGold,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun TournamentPicker(
    selected: String,
    onSelect: (com.worldcup2026.domain.model.TournamentConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val pickerLabel = stringResource(R.string.tournament_picker_label, selected)
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pickerLabel,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = WcGoldLight)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TOURNAMENTS.forEach { t ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tournament_picker_label, t.label)) },
                    onClick = { onSelect(t); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WcYellow.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = WcYellow, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = message, color = WcYellow, style = MaterialTheme.typography.bodySmall)
    }
}
