package com.worldcup2026.data.remote.dto

data class StatisticsResponse(
    val statistics: List<StatsPeriodDto>?
)

data class StatsPeriodDto(
    val period: String?,   // "ALL","1ST","2ND"
    val groups: List<StatsGroupDto>?
)

data class StatsGroupDto(
    val groupName: String?,
    val statisticsItems: List<StatItemDto>?
)

data class StatItemDto(
    val name: String?,
    val home: String?,
    val away: String?,
    val compareCode: Int?,   // 1=home better, 2=away better, 3=equal
    val statisticsType: String?,   // "positive","negative"
    val valueType: String?,         // "event","percentage","team"
    val homeValue: Float?,
    val awayValue: Float?,
    val renderType: Int?
)
