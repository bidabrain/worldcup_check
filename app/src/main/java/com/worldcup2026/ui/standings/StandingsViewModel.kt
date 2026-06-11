package com.worldcup2026.ui.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.worldcup2026.R
import com.worldcup2026.data.repository.MatchRepository
import com.worldcup2026.domain.model.Standing
import com.worldcup2026.domain.model.TOURNAMENTS
import com.worldcup2026.domain.model.TournamentConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StandingsUiState(
    val tournament: TournamentConfig = TOURNAMENTS.first(),
    val groups: Map<String, List<Standing>> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    @StringRes val errorResId: Int? = null
)

class StandingsViewModel(private val repo: MatchRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeStandings().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
        refresh(silent = false)
    }

    fun selectTournament(tournament: TournamentConfig) {
        _uiState.update { it.copy(tournament = tournament) }
        refresh(silent = false)
    }

    fun refresh(silent: Boolean = true) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true) }
            else _uiState.update { it.copy(isRefreshing = true) }

            val seasonId = _uiState.value.tournament.seasonId

            repo.syncStandings(seasonId)
                .onFailure { _uiState.update { it.copy(errorResId = R.string.error_standings_failed) } }
                .onSuccess { _uiState.update { it.copy(errorResId = null) } }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    companion object {
        fun factory(repo: MatchRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                StandingsViewModel(repo) as T
        }
    }
}
