package pt.restricoes.model

import pt.restricoes.services.Time
import java.util.Date

data class Municipality(
    val name: String,
    val district: String,
    val region: String,
    val holiday: Date?
) {
    constructor(name: String, district: String, region: String, holiday: String) :
            this(name, district, region, Time.parseDate(holiday))
}