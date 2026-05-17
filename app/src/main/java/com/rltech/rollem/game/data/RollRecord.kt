package com.rltech.rollem.game.data

import kotlinx.serialization.Serializable

@Serializable
data class RollRecord (
    val rollNumber: Int,
    val rollValue: Int,
    val streakSize: Int,
    val oddEvenPrediction: String,
    val valuePrediction: Int,
)