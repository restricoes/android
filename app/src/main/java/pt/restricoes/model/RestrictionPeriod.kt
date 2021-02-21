package pt.restricoes.model

import java.util.*

data class RestrictionPeriod(
    val period: String, // "week", "weekend", "holiday", "all"
    val startTime: Date?,
    val endTime: Date?
)