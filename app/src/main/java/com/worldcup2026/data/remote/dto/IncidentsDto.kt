package com.worldcup2026.data.remote.dto

data class IncidentsResponse(
    val incidents: List<IncidentDto>?
)

data class IncidentDto(
    val id: Int?,
    val time: Int?,
    val incidentType: String?,   // "goal","card","substitution"
    val incidentClass: String?,  // "regular","ownGoal","penalty","yellow","red"
    val player: PlayerRefDto?,   // scorer / carded player / player coming ON for subs
    val playerIn: PlayerRefDto?, // substitution: player entering (alternate field name)
    val playerOut: PlayerRefDto?,// substitution: player leaving
    val isHome: Boolean?
)

data class PlayerRefDto(
    val id: Int?,
    val name: String?,
    val shortName: String?
)
