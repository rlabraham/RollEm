package com.rltech.rollem.game.data

import android.content.Context
import com.rltech.rollem.game.OddEvenGuess
import com.rltech.rollem.game.save.LAST_ROLLS_TOTAL_KEY
import com.rltech.rollem.game.save.LAST_SCORE_KEY
import com.rltech.rollem.game.save.ROLL_RECORDS_KEY
import com.rltech.rollem.game.save.SaveManager

object GameDataHelper {
    private var gameData = GameData()

    fun setFinalScore(score: Long) {
        gameData.finalScore = score
    }

    fun recordRoll(
        roll: Int,
        streakSize: Int,
        oddEvenGuess: OddEvenGuess?,
        rollGuess: Int
    ) {
        gameData.totalRolls++

        val rollRecord = RollRecord(
            rollNumber = gameData.totalRolls,
            rollValue = roll,
            streakSize = streakSize,
            oddEvenPrediction = oddEvenGuess?.name ?: "n/a",
            valuePrediction = rollGuess
        )

        gameData.rollRecords.add(rollRecord)
    }

    fun avgRoll(
        rolls: Int = gameData.totalRolls,
        values: List<Int> = gameData.rollRecords.map { it.rollValue }
    ): Double =
        if (rolls == 0) 0.0 else values.average()

    fun highestStreak(
        streaks: List <Int> = gameData.rollRecords.map { it.rollValue }
    ): Int =
        streaks.maxOrNull() ?: 0

    fun totalCorrectOddEvenGuesses(
        oddEvenPredictions: List<String> = gameData.rollRecords.map { it.oddEvenPrediction },
        values: List<Int> = gameData.rollRecords.map { it.rollValue }
    ): Int =
        oddEvenPredictions.zip(values).count { (guess, value) ->
            val isEven = value % 2 == 0
            (isEven && guess.equals("even", ignoreCase = true)) ||
                    (!isEven && guess.equals("odd", ignoreCase = true))
        }

    fun totalCorrectNextGuesses(
        valuePredictions: List<Int> = gameData.rollRecords.map { it.valuePrediction },
        values: List<Int> = gameData.rollRecords.map { it.rollValue }
    ): Int =
        valuePredictions.zip(values).count { (guess, value) -> guess == value }

    fun saveGameData(context: Context) {
        SaveManager.saveLong(context, LAST_SCORE_KEY, gameData.finalScore)
        SaveManager.saveInt(context, LAST_ROLLS_TOTAL_KEY, gameData.totalRolls)

        SaveManager.saveList(context, ROLL_RECORDS_KEY, gameData.rollRecords)

    }

    fun getSavedGameData(context: Context) = GameData(
        finalScore = SaveManager.getLong(context, LAST_SCORE_KEY),
        totalRolls = SaveManager.getInt(context, LAST_ROLLS_TOTAL_KEY),
        rollRecords = SaveManager.getList<RollRecord>(context, ROLL_RECORDS_KEY) as MutableList,
    )

    fun resetGameData() {
        gameData = GameData()
    }

    fun getCurrentGameDataSnapshot(): GameData =
        gameData.copy(rollRecords = gameData.rollRecords.toMutableList())

}
