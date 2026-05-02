package com.rltech.rollem.gamestate

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.edit

const val DEFAULT_ROLES_LEFT = 10

private const val PREFS_NAME = "game_prefs"
private const val LAST_SCORE_KEY = "last_score"

object GameState {
    private val streak: SnapshotStateList<Int> = mutableStateListOf()

    var oddEvenGuess by mutableStateOf<OddEvenGuess?>(null)

    var score by mutableLongStateOf(0)
        private set

    var rollsLeft by mutableIntStateOf(DEFAULT_ROLES_LEFT)
        private set

    val getStreak = streak

    fun updateGameState(roll: Int = 0) {
        updateStreak(roll)
        updateScore(roll)
        updateRollsLeft(roll)
    }

    fun resetGameState() {
        score = 0
        rollsLeft = DEFAULT_ROLES_LEFT
        oddEvenGuess = null
        streak.clear()
    }

    fun saveLastScore(context: Context, score: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(LAST_SCORE_KEY, score) }
    }

    fun getLastScore(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(LAST_SCORE_KEY, 0L)
    }

    private fun updateScore(roll: Int = 0) {
        val streakBonus = if (streak.size > 1) streak.size else 0
        score = score + roll + streakBonus
    }

    private fun updateRollsLeft(roll: Int = 0) {
        var increment = -1

        if (oddEvenGuess != null) {
            val rollIsEven = roll % 2 == 0
            val correctGuess = (rollIsEven && oddEvenGuess == OddEvenGuess.EVEN) || (!rollIsEven && oddEvenGuess == OddEvenGuess.ODD)

            if (correctGuess) ++increment else --increment
        }

        rollsLeft = rollsLeft + increment
    }

    private fun updateStreak(roll: Int = 0) {
        val last = streak.lastOrNull()

        when {
            roll == 1 -> streak.clear()
            streak.isEmpty() || last == 6 || last == roll -> streak.add(roll)
            else -> {
                streak.clear()
                streak.add(roll)
            }
        }
    }
}