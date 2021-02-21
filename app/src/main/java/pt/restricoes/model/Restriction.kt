package pt.restricoes.model

data class Restriction(
    val id: String,
    val title: String,
    val subtitle: String?,
    val icon: String,
    val widget: RestrictionWidget?
)