package com.worldcup2026.ui.knockout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.worldcup2026.R
import com.worldcup2026.data.repository.MatchRepository
import com.worldcup2026.domain.model.KnockoutRound
import com.worldcup2026.domain.model.TOURNAMENTS
import com.worldcup2026.domain.model.TournamentConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class KnockoutUiState(
    val tournament: TournamentConfig = TOURNAMENTS.first(),
    val rounds: List<KnockoutRound> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,   // background date-sync to resolve match IDs
    @StringRes val errorResId: Int? = null
)

class KnockoutViewModel(private val repo: MatchRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(KnockoutUiState())
    val uiState: StateFlow<KnockoutUiState> = _uiState.asStateFlow()

    init {
        load(silent = false)
    }

    fun selectTournament(tournament: TournamentConfig) {
        _uiState.update { it.copy(tournament = tournament) }
        load(silent = false)
    }

    fun refresh() = load(silent = true)

    private fun load(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true, errorResId = null) }
            else _uiState.update { it.copy(isRefreshing = true) }

            val tournament = _uiState.value.tournament

            // Step 1: fetch bracket structure
            repo.getKnockout(tournament.seasonId)
                .onSuccess { rounds -> _uiState.update { it.copy(rounds = rounds, errorResId = null) } }
                .onFailure { _uiState.update { it.copy(errorResId = R.string.error_knockout_failed) } }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }

            // Step 2: if any finished match still has no matchId, sync knockout-phase
            // dates so the DB name-lookup can resolve them on the next getKnockout() call.
            val needsSync = _uiState.value.rounds
                .flatMap { it.matches }
                .any { it.finished && it.matchId == null }

            if (needsSync) {
                _uiState.update { it.copy(isSyncing = true) }

                // Knockout phase = last ~20 days of the tournament
                val knockoutStart = maxOf(
                    tournament.endDate.minusDays(20),
                    tournament.startDate
                )
                var date = knockoutStart
                while (!date.isAfter(tournament.endDate.plusDays(1))) {
                    repo.syncDate(date, tournament.uniqueTournamentId)
                    date = date.plusDays(1)
                }

                // Step 3: reload bracket — DB lookup should now resolve match IDs
                repo.getKnockout(tournament.seasonId)
                    .onSuccess { rounds -> _uiState.update { it.copy(rounds = rounds) } }

                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    companion object {
        fun factory(repo: MatchRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                KnockoutViewModel(repo) as T
        }
    }
}
