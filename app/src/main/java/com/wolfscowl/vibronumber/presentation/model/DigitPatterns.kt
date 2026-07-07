package com.wolfscowl.vibronumber.presentation.model

object DigitPatterns {
    val patterns = mapOf(
        0 to listOf(0, 1, 2, 5, 8, 7, 6, 3, 0),
        1 to listOf(1, 4, 7),
        2 to listOf(0, 1, 2, 5, 4, 3, 6, 7, 8),
        3 to listOf(0, 1, 2, 5, 4, 5, 8, 7, 6),
        4 to listOf(0, 3, 4, 5, 1, 4, 7), // Alternative (0, 3, 4, 1, 4, 7)
        5 to listOf(2, 1, 0, 3, 4, 5, 8, 7, 6),
        6 to listOf(2, 1, 0, 3, 6, 7, 8, 5, 4, 3),
        7 to listOf(0, 1, 2, 5, 8),
        8 to listOf(1, 0, 3, 4, 5, 8, 7, 6, 3, 4, 5, 2, 1), // Alternative (2, 1, 0, 3, 4, 5, 8, 7, 6, 3, 4, 5, 2)
        9 to listOf(2, 1, 0, 3, 4, 5, 2, 5, 8, 7, 6) // Alternative (5, 4, 3, 0, 1, 2, 5, 8, 7, 6)
    )
}
