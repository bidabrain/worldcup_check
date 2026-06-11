package com.worldcup2026.domain.model

import java.time.LocalDate

data class TournamentConfig(
    val label: String,
    val uniqueTournamentId: Int,
    val seasonId: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    val days: List<LocalDate> = generateSequence(startDate) { it.plusDays(1) }
        .takeWhile { !it.isAfter(endDate) }
        .toList()
}

val TOURNAMENTS = listOf(
    TournamentConfig(
        label = "2026",
        uniqueTournamentId = 16,
        seasonId = 58210,
        startDate = LocalDate.of(2026, 6, 11),
        endDate   = LocalDate.of(2026, 7, 19)
    ),
    TournamentConfig(
        label = "2022",
        uniqueTournamentId = 16,
        seasonId = 41087,
        startDate = LocalDate.of(2022, 11, 20),
        endDate   = LocalDate.of(2022, 12, 18)
    ),
    TournamentConfig(
        label = "2018",
        uniqueTournamentId = 16,
        seasonId = 15586,
        startDate = LocalDate.of(2018, 6, 14),
        endDate   = LocalDate.of(2018, 7, 15)
    ),
)
