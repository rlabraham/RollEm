package com.rltech.rollem.googleplay

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.games.PlayGames
import com.rltech.rollem.R

object LeaderBoard {
    fun showLeaderboard(
        launcher: ActivityResultLauncher<Intent>,
        activity: Activity
    ) {
        Authentication.refreshAuthenticationStatus(activity) { authenticated ->
            if (!authenticated) {
                Authentication.signIn(activity)
                return@refreshAuthenticationStatus
            }

            PlayGames.getLeaderboardsClient(activity)
                .getLeaderboardIntent(activity.getString(R.string.leaderboard_id))
                .addOnSuccessListener { intent ->
                    launcher.launch(intent)
                }
                .addOnFailureListener { error ->
                    Log.e("Leaderboards", "Failed to open leaderboard", error)
                    Toast.makeText(activity, activity.getString(R.string.playgames_signin_fail), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun submitScore(score: Long, activity: Activity) {
        PlayGames.getLeaderboardsClient(activity).submitScore(
            activity.getString(R.string.leaderboard_id),
            score
        )
    }
}