package com.rltech.rollem.game.data

import android.content.Context
import com.rltech.rollem.game.OddEvenGuess
import com.rltech.rollem.game.save.LAST_ROLLS_TOTAL_KEY
import com.rltech.rollem.game.save.LAST_SCORE_KEY
import com.rltech.rollem.game.save.ODD_EVEN_VALUES_KEY
import com.rltech.rollem.game.save.ROLL_GUESSES_KEY
import com.rltech.rollem.game.save.ROLL_VALUES_KEY
import com.rltech.rollem.game.save.SCORE_VALUES_KEY
import com.rltech.rollem.game.save.STREAK_VALUES_KEY
import com.rltech.rollem.game.save.SaveManager
import java.util.SortedMap

object GameDataHelper {
    private var gameData = GameData()

    fun setFinalScore(score: Long) {
        gameData.finalScore = score
    }

    fun recordRoll(
        roll: Int,
        score: Long,
        streakSize: Int,
        oddEvenGuess: OddEvenGuess?,
        rollGuess: Int
    ) {
        gameData.totalRolls++

        gameData.rollValues.add(roll)
        gameData.rollScores.add(score)
        gameData.rollStreaks.add(streakSize)
        gameData.oddEvenPredictions.add(oddEvenGuess?.name ?: "n/a")
        gameData.nextRollPredictions.add(rollGuess)
    }

    fun avgRoll(
        rolls: Int = gameData.totalRolls,
        values: List<Int> = gameData.rollValues
    ): Double =
        if (rolls == 0) 0.0 else values.sum().toDouble() / rolls

    fun rollCounts(
        values: List <Int> = gameData.rollValues
    ): SortedMap<Int, Int> =
        values.groupingBy { it }
            .eachCount()
            .toSortedMap(compareByDescending { it })

    fun highestStreak(
        streaks: List <Int> = gameData.rollStreaks
    ): Int =
        streaks.maxOrNull() ?: 0

    fun totalCorrectOddEvenGuesses(
        oddEvenPreds: List<String> = gameData.oddEvenPredictions,
        values: List<Int> = gameData.rollValues
    ): Int =
        oddEvenPreds.zip(values).count { (guess, value) ->
            val isEven = value % 2 == 0
            (isEven && guess.equals("even", ignoreCase = true)) ||
                    (!isEven && guess.equals("odd", ignoreCase = true))
        }

    fun totalCorrectNextGuesses(
        nextRollPreds: List<Int> = gameData.nextRollPredictions,
        values: List<Int> = gameData.rollValues
    ): Int =
        nextRollPreds.zip(values).count { (guess, value) ->
            guess == value
        }

    fun saveGameData(context: Context) {
        SaveManager.saveLong(context, LAST_SCORE_KEY, gameData.finalScore)
        SaveManager.saveInt(context, LAST_ROLLS_TOTAL_KEY, gameData.totalRolls)

        SaveManager.saveList(context, ROLL_VALUES_KEY, gameData.rollValues)
        SaveManager.saveList(context, SCORE_VALUES_KEY, gameData.rollScores)
        SaveManager.saveList(context, STREAK_VALUES_KEY, gameData.rollStreaks)
        SaveManager.saveList(context, ODD_EVEN_VALUES_KEY, gameData.oddEvenPredictions)
        SaveManager.saveList(context, ROLL_GUESSES_KEY, gameData.nextRollPredictions)
    }

    fun getSavedGameData(context: Context) = GameData(
        finalScore = SaveManager.getLong(context, LAST_SCORE_KEY),
        totalRolls = SaveManager.getInt(context, LAST_ROLLS_TOTAL_KEY),
        rollValues = SaveManager.getList<Int>(context, ROLL_VALUES_KEY) as MutableList,
        rollScores = SaveManager.getList<Long>(context, SCORE_VALUES_KEY) as MutableList,
        rollStreaks = SaveManager.getList<Int>(context, STREAK_VALUES_KEY) as MutableList,
        oddEvenPredictions = SaveManager.getList<String>(context, ODD_EVEN_VALUES_KEY) as MutableList,
        nextRollPredictions = SaveManager.getList<Int>(context, ROLL_GUESSES_KEY) as MutableList
    )

    fun resetGameData() {
        gameData = GameData()
    }
}
