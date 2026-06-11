package com.worldcup2026.ui.matchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldcup2026.data.repository.MatchRepository
import com.worldcup2026.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MatchDetailUiState(
    val match: Match? = null,
    val incidents: List<Incident> = emptyList(),
    val lineup: Lineup? = null,
    val stats: List<StatItem> = emptyList(),
    val isLoadingMatch: Boolean = true,
    val isLoadingLineup: Boolean = false,
    val isLoadingStats: Boolean = false,
    val lineupError: String? = null,
    val statsError: String? = null
)

class MatchDetailViewModel(
    private val matchId: Int,
    private val repo: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    init {
        loadMatch()
        observeIncidents()
    }

    private fun loadMatch() {
        viewModelScope.launch {
            // Try cache first
            val cached = repo.getMatch(matchId)
            if (cached != null) _uiState.update { it.copy(match = cached, isLoadingMatch = false) }

            // Sync from network
            repo.syncMatchDetail(matchId).onSuccess {
                val fresh = repo.getMatch(matchId)
                _uiState.update { it.copy(match = fresh, isLoadingMatch = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMatch = false) }
            }

            // Sync incidents
            repo.syncIncidents(matchId)
        }
    }

    private fun observeIncidents() {
        viewModelScope.launch {
            repo.observeIncidents(matchId).collect { incidents ->
                _uiState.update { it.copy(incidents = incidents) }
            }
        }
    }

    fun loadLineup() {
        if (_uiState.value.lineup != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLineup = true, lineupError = null) }
            repo.getLineups(matchId)
                .onSuccess { lineup -> _uiState.update { it.copy(lineup = lineup) } }
                .onFailure { _uiState.update { it.copy(lineupError = "阵容加载失败") } }
            _uiState.update { it.copy(isLoadingLineup = false) }
        }
    }

    fun loadStats() {
        if (_uiState.value.stats.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, statsError = null) }
            repo.getStatistics(matchId)
                .onSuccess { stats -> _uiState.update { it.copy(stats = stats) } }
                .onFailure { _uiState.update { it.copy(statsError = "统计数据加载失败") } }
            _uiState.update { it.copy(isLoadingStats = false) }
        }
    }

    companion object {
        fun factory(matchId: Int, repo: MatchRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                MatchDetailViewModel(matchId, repo) as T
        }
    }
}
