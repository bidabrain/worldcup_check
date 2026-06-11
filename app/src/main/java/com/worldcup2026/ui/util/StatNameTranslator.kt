package com.worldcup2026.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.worldcup2026.R

/**
 * Maps a SofaScore English stat name (as returned by the API) to the
 * locale-appropriate translated string.  Falls back to the original API
 * name when no mapping is found.
 */
@Composable
fun translateStatName(apiName: String): String = when (apiName.lowercase().trim()) {
    "ball possession"       -> stringResource(R.string.stat_ball_possession)
    "total shots"           -> stringResource(R.string.stat_total_shots)
    "shots on target"       -> stringResource(R.string.stat_shots_on_target)
    "shots off target"      -> stringResource(R.string.stat_shots_off_target)
    "blocked shots"         -> stringResource(R.string.stat_blocked_shots)
    "hit woodwork"          -> stringResource(R.string.stat_hit_woodwork)
    "big chances"           -> stringResource(R.string.stat_big_chances)
    "big chances missed"    -> stringResource(R.string.stat_big_chances_missed)
    "corner kicks"          -> stringResource(R.string.stat_corner_kicks)
    "offsides"              -> stringResource(R.string.stat_offsides)
    "fouls"                 -> stringResource(R.string.stat_fouls)
    "free kicks"            -> stringResource(R.string.stat_free_kicks)
    "yellow cards"          -> stringResource(R.string.stat_yellow_cards)
    "red cards"             -> stringResource(R.string.stat_red_cards)
    "goalkeeper saves"      -> stringResource(R.string.stat_goalkeeper_saves)
    "passes"                -> stringResource(R.string.stat_passes)
    "accurate passes"       -> stringResource(R.string.stat_accurate_passes)
    "long balls"            -> stringResource(R.string.stat_long_balls)
    "accurate long balls"   -> stringResource(R.string.stat_accurate_long_balls)
    "crosses"               -> stringResource(R.string.stat_crosses)
    "accurate crosses"      -> stringResource(R.string.stat_accurate_crosses)
    "tackles"               -> stringResource(R.string.stat_tackles)
    "clearances"            -> stringResource(R.string.stat_clearances)
    "interceptions"         -> stringResource(R.string.stat_interceptions)
    "dribbles"              -> stringResource(R.string.stat_dribbles)
    "duels won"             -> stringResource(R.string.stat_duels_won)
    "aerial duels won"      -> stringResource(R.string.stat_aerial_duels_won)
    "attacks"               -> stringResource(R.string.stat_attacks)
    "dangerous attacks"     -> stringResource(R.string.stat_dangerous_attacks)
    "expected goals"        -> stringResource(R.string.stat_expected_goals)
    "shots inside box"      -> stringResource(R.string.stat_shots_inside_box)
    "shots outside box"     -> stringResource(R.string.stat_shots_outside_box)
    "total passes"          -> stringResource(R.string.stat_total_passes)
    else                    -> apiName
}
