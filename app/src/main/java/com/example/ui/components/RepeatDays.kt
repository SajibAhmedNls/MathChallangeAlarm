package com.example.ui.components

// Day indexes follow AlarmEntity: 1=Mon ... 7=Sun
val DayShortLabels = listOf("M", "T", "W", "T", "F", "S", "S")
val DayFullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val DayAbbreviations = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/** Human-friendly description of a repeat schedule, e.g. "Weekdays" or "Mon, Wed, Fri". */
fun repeatSummary(days: Collection<Int>): String {
    val sorted = days.filter { it in 1..7 }.toSortedSet()
    return when (sorted) {
        emptySet<Int>() -> "Once"
        (1..7).toSet() -> "Every day"
        (1..5).toSet() -> "Weekdays"
        setOf(6, 7) -> "Weekends"
        else -> sorted.joinToString(", ") { DayAbbreviations[it - 1] }
    }
}
