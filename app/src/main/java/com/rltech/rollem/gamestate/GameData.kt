package com.rltech.rollem.gamestate

import java.util.SortedMap

object GameData {
    var finalScore: Long = 0L
    var totalRolls: Int = 0

    val rollValueMap = mutableMapOf<Int, Int>()
    val rollScoreMap = mutableMapOf<Int, Long>()
    val rollStreakMap = mutableMapOf<Int, Int>()
    val rollOddEvenMap = mutableMapOf<Int, String>()
    val rollNextGuessMap = mutableMapOf<Int, Int>()

    fun avgRoll(
        rolls: Int = totalRolls,
        valueMap: Map<Int, Int> = rollValueMap
    ): Double =
        if (rolls == 0)
            0.0
        else
            valueMap.values.sum().toDouble() / rolls

    fun rollCounts(
        valueMap: Map<Int, Int> = rollValueMap
    ): SortedMap<Int, Int> =
        valueMap.values
            .groupingBy { it }
            .eachCount()
            .toSortedMap(compareByDescending { it })

    fun highestStreak(
        streakMap: Map<Int, Int> = rollStreakMap
    ): Int =
        streakMap.values.maxOrNull() ?: 0

    fun totalCorrectOddEvenGuesses(
        oddEvenMap: Map<Int, String> = rollOddEvenMap,
        valueMap: Map<Int, Int> = rollValueMap
    ): Int =
        oddEvenMap.count { (roll, guess) ->
            val value = valueMap[roll] ?: return@count false
            val isEven = value % 2 == 0

            (isEven && guess.equals("even", ignoreCase = true)) ||
                    (!isEven && guess.equals("odd", ignoreCase = true))
        }

    fun totalCorrectNextGuesses(
        nextGuessMap: Map<Int, Int> = rollNextGuessMap,
        valueMap: Map<Int, Int> = rollValueMap
    ): Int =
        nextGuessMap.count{ (roll, guess) ->
            val value = valueMap[roll] ?: return@count false

            guess == value
        }
}
