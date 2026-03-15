package com.example.adroller.gamestate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

const val DEFAULT_ROLES_LEFT = 10

object GameState {
    var oddEvenGuess = OddEvenGuess.NONE

    var score by mutableIntStateOf(0)
        private set

    var rollsLeft by mutableIntStateOf(DEFAULT_ROLES_LEFT)
        private set

    private val streak = ArrayList<Int>()

    fun updateGameState(roll: Int = 0) {
        updateStreak(roll)
        updateScore(roll)
        updateRollsLeft(roll)
    }

    private fun updateScore(roll: Int = 0) {
        val streakBonus = if (streak.size > 1) streak.size else 0
        score = score + roll + streakBonus
    }

    private fun updateRollsLeft(roll: Int = 0) {
        var increment = -1

        if (oddEvenGuess != OddEvenGuess.NONE) {
            val rollIsEven = roll % 2 == 0
            val correctGuess = (rollIsEven && oddEvenGuess == OddEvenGuess.EVEN) || (!rollIsEven && oddEvenGuess == OddEvenGuess.ODD)

            if (correctGuess) ++increment else --increment
        }

        rollsLeft = rollsLeft + increment;
    }

    private fun updateStreak(roll: Int = 0) {
        val last = streak.lastOrNull()

        when {
            roll == 1 -> streak.clear()
            streak.isEmpty() || last == 6 || last == roll -> streak.add(roll)
            else -> {
                streak.clear()
            }
        }
    }
}