package com.studyconnect.app.util

data class Subject(val name: String, val emoji: String, val description: String = "")

object Subjects {
    val all = listOf(
        Subject("Statistics", "\uD83D\uDCCA", "Hypothesis testing, probability, regression & data analysis"),
        Subject("Programming", "\uD83D\uDCBB", "Kotlin, Java, Python, OOP & algorithm design"),
        Subject("Mathematics", "\u2797", "Calculus, linear algebra & discrete mathematics"),
        Subject("Databases", "\uD83D\uDDC4\uFE0F", "SQL, ER diagrams, normalization & relational DBs"),
        Subject("Accounting", "\uD83E\uDDFE", "Financial accounting, balance sheets & bookkeeping"),
        Subject("Networking", "\uD83C\uDF10", "Subnetting, IP routing, protocols & network security"),
        Subject("Economics", "\uD83D\uDCC8", "Microeconomics, macroeconomics & market structures"),
        Subject("Other", "\uD83D\uDCDA", "General academic support & elective units")
    )

    fun emoji(name: String): String =
        all.firstOrNull { it.name.equals(name, ignoreCase = true) }?.emoji ?: "\uD83D\uDCDA"
}
