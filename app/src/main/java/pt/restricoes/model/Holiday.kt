package pt.restricoes.model

import pt.restricoes.services.Time

object Holiday {

    val all = listOf(
        "2021-04-02",
        "2021-04-04",
        "2021-04-25",
        "2021-05-01",
        "2021-06-03",
        "2021-06-10",
        "2021-08-15",
        "2021-10-05",
        "2021-11-01",
        "2021-12-01",
        "2021-12-08",
        "2021-12-25"
    ).map(Time::parseDate)
}