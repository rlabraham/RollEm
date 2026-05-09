package com.rltech.rollem.gamestate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

const val DEFAULT_ROLES_LEFT = 10

object GameState {
    private val streak: SnapshotStateList<Int> = mutableStateListOf()

    var oddEvenGuess by mutableStateOf<OddEvenGuess?>(null)

    var rollGuess by mutableIntStateOf(0)

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

    private fun updateScore(roll: Int = 0) {
        val streakBonus = if (streak.size > 1) streak.size else 0
        val rollGuessModifier = if (rollGuess == roll) rollGuess else rollGuess * -1

        score = (score + roll + streakBonus + rollGuessModifier).coerceAtLeast(0L)
    }

    private fun updateRollsLeft(roll: Int = 0) {
        var increment = -1

        if (oddEvenGuess != null) {
            val rollIsEven = roll % 2 == 0
            val correctGuess = (rollIsEven && oddEvenGuess == OddEvenGuess.EVEN) || (!rollIsEven && oddEvenGuess == OddEvenGuess.ODD)

            if (correctGuess) ++increment else --increment
        }

        rollsLeft += increment
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