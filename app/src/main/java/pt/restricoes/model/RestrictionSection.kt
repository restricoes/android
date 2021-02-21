package pt.restricoes.model

data class RestrictionSection(
    val id: String,
    val name: String,
    val items: List<RestrictionItem>
)