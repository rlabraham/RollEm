package com.rltech.rollem.googleplay

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.games.PlayGames
import com.rltech.rollem.R

object Authentication {
    var isAuthenticated by mutableStateOf(false)

    fun refreshAuthenticationStatus(activity: Activity, onComplete: ((Boolean) -> Unit)? = null) {
        PlayGames.getGamesSignInClient(activity)
            .isAuthenticated()
            .addOnCompleteListener { authTask ->
                val authenticatedNow = authTask.isSuccessful && authTask.result?.isAuthenticated == true
                isAuthenticated = authenticatedNow
                onComplete?.invoke(authenticatedNow)
            }
    }

    fun signIn(activity: Activity) {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)

        refreshAuthenticationStatus(activity) { authenticatedNow ->
            if (authenticatedNow) {
                isAuthenticated = true
                Toast.makeText(activity, activity.getString(R.string.playgames_signin_success), Toast.LENGTH_SHORT).show()
            } else {
                gamesSignInClient.signIn().addOnCompleteListener { signInTask ->
                    val signInSucceeded = signInTask.isSuccessful && signInTask.result?.isAuthenticated == true
                    isAuthenticated = signInSucceeded

                    val message = if (signInSucceeded) {
                        activity.getString(R.string.playgames_signin_success)
                    } else {
                        activity.getString(R.string.playgames_signin_fail)
                    }
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}