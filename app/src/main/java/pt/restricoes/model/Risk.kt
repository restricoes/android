package pt.restricoes.model

data class Risk(
    val municipality: String,
    val population: Int,
    val incidence: Int,
    val risk: String
)