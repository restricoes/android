package pt.restricoes.services

import java.text.Normalizer

object Search {
    private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    fun <A> Collection<A>.search(input: String, field: (A) -> String): Collection<A> {
        val search = normalize(input)

        return this
            .map { a ->
                val potential = normalize(field(a))

                val score =
                    when {
                        potential.startsWith(search) -> 1
                        potential.contains(search) -> 2
                        else -> 0
                    }

                Pair(score, a)
            }
            .filter { p -> p.first > 0 }
            .sortedBy { p -> p.first }
            .map { p -> p.second }
    }

    private fun normalize(str: String): String {
        val temp = Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
    }
}