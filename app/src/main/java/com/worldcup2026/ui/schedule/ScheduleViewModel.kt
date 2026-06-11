package com.worldcup2026.ui.schedule

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldcup2026.R
import com.worldcup2026.data.repository.MatchRepository
import com.worldcup2026.domain.model.Match
import com.worldcup2026.domain.model.MatchStatus
import com.worldcup2026.domain.model.TOURNAMENTS
import com.worldcup2026.domain.model.TournamentConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ScheduleUiState(
    val tournament: TournamentConfig = TOURNAMENTS.first(),
    val selectedDate: LocalDate = LocalDate.now(),  // overridden in VM init
    val displayMonth: YearMonth = YearMonth.now(),  // overridden in VM init
    val matches: List<Match> = emptyList(),
    val matchDays: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLivePolling: Boolean = false,
    @StringRes val errorResId: Int? = null,
    val isOffline: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(private val repo: MatchRepository) : ViewModel() {

    private val initialDate: LocalDate = LocalDate.now().let { today ->
        val wc = TOURNAMENTS.first()
        if (!today.isBefore(wc.startDate) && !today.isAfter(wc.endDate)) today else wc.startDate
    }

    private val _selectedDate = MutableStateFlow(initialDate)

    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            selectedDate = initialDate,
            displayMonth = YearMonth.from(initialDate)
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var livePollingJob: Job? = null
    private var monthSyncJob: Job? = null

    init {
        viewModelScope.launch {
            _selectedDate
                .flatMapLatest { date -> repo.observeMatchesByDate(date) }
                .collect { matches ->
                    _uiState.update { it.copy(matches = matches) }
                    manageLivePolling(matches)
                }
        }
        // Show whatever is already cached, then kick off the full month sync
        viewModelScope.launch {
            val cachedDays = repo.getCachedMatchDays().toSet()
            _uiState.update { it.copy(matchDays = cachedDays) }
        }
        refresh(silent = false)
        syncMonthDays(YearMonth.from(initialDate))
    }

    // ── Tournament / date / month selection ───────────────────────────────────

    fun selectTournament(tournament: TournamentConfig) {
        val newDate = tournament.startDate
        _selectedDate.value = newDate
        val newMonth = YearMonth.from(newDate)
        _uiState.update { it.copy(tournament = tournament, selectedDate = newDate, displayMonth = newMonth) }
        refresh(silent = false)
        syncMonthDays(newMonth)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
        refresh(silent = true)
    }

    fun selectMonth(month: YearMonth) {
        _uiState.update { it.copy(displayMonth = month) }
        syncMonthDays(month)
    }

    // ── Manual refresh ────────────────────────────────────────────────────────

    fun refresh(silent: Boolean = true) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true) }
            else _uiState.update { it.copy(isRefreshing = true) }

            val date = _selectedDate.value
            val tournamentId = _uiState.value.tournament.uniqueTournamentId
            repo.syncDate(date, tournamentId)
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            errorResId = if (s.matches.isEmpty()) R.string.error_offline_cache else null,
                            isOffline = true
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isOffline = false, errorResId = null) }
                }

            val days = repo.getCachedMatchDays().toSet()
            _uiState.update { it.copy(matchDays = days, isLoading = false, isRefreshing = false) }
        }
    }

    // ── Background month sync — fills in calendar dots for the displayed month ─

    private fun syncMonthDays(month: YearMonth) {
        monthSyncJob?.cancel()
        monthSyncJob = viewModelScope.launch {
            val tournament = _uiState.value.tournament
            val tournamentId = tournament.uniqueTournamentId
            // Clamp to the tournament window (+1 day buffer for timezone)
            val first = month.atDay(1).coerceAtLeast(tournament.startDate)
            val last  = month.atEndOfMonth().coerceAtMost(tournament.endDate.plusDays(1))
            if (first.isAfter(last)) return@launch

            var date = first
            while (!date.isAfter(last)) {
                repo.syncDate(date, tournamentId)
                // Refresh dots incrementally after each day so they appear as data arrives
                val days = repo.getCachedMatchDays().toSet()
                _uiState.update { it.copy(matchDays = days) }
                date = date.plusDays(1)
            }
        }
    }

    // ── Live polling ──────────────────────────────────────────────────────────

    private fun manageLivePolling(matches: List<Match>) {
        val hasLive = matches.any { it.status == MatchStatus.IN_PROGRESS }
        if (hasLive) {
            if (livePollingJob?.isActive != true) {
                livePollingJob = viewModelScope.launch {
                    _uiState.update { it.copy(isLivePolling = true) }
                    while (true) {
                        delay(30_000L)
                        repo.syncLive()
                        repo.syncDate(
                            _selectedDate.value,
                            _uiState.value.tournament.uniqueTournamentId
                        )
                        if (_uiState.value.matches.none { it.status == MatchStatus.IN_PROGRESS }) {
                            break
                        }
                    }
                    _uiState.update { it.copy(isLivePolling = false) }
                }
            }
        } else {
            if (livePollingJob?.isActive == true) {
                livePollingJob?.cancel()
                livePollingJob = null
                _uiState.update { it.copy(isLivePolling = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        livePollingJob?.cancel()
        monthSyncJob?.cancel()
    }

    companion object {
        fun factory(repo: MatchRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                ScheduleViewModel(repo) as T
        }
    }
}
