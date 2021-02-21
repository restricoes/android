package pt.restricoes.model

import java.util.*

data class RestrictionValue(
    val id: String,
    val restriction: Restriction?,
    val startDate: Date,
    val endDate: Date,
    val periods: List<RestrictionPeriod>,
    val sections: List<RestrictionSection>
)