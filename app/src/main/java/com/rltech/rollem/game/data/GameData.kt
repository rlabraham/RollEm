package com.rltech.rollem.game.data

private const val DEFAULT_FINAL_SCORE = 0L
private const val DEFAULT_TOTAL_ROLLS = 0

data class GameData(
    var finalScore: Long = DEFAULT_FINAL_SCORE,
    var totalRolls: Int = DEFAULT_TOTAL_ROLLS,
    val rollRecords: MutableList<RollRecord> = mutableListOf()
)