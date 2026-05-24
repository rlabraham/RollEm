package com.rltech.rollem

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.games.PlayGamesSdk
import com.rltech.rollem.admob.Ads
import com.rltech.rollem.admob.Ads.BannerAd
import com.rltech.rollem.admob.BOTTOM_BANNER_ID
import com.rltech.rollem.admob.TOP_BANNER_ID
import com.rltech.rollem.game.OddEvenGuess
import com.rltech.rollem.game.save.LAST_SCORE_KEY
import com.rltech.rollem.game.save.SaveManager
import com.rltech.rollem.game.state.GameState
import com.rltech.rollem.googleplay.Authentication
import com.rltech.rollem.googleplay.LeaderBoard
import com.rltech.rollem.ui.stats.StatsActivity
import com.rltech.rollem.ui.theme.RollEmTheme


private val CHALK_BOARD_FONT = FontFamily(Font(R.font.chalk_board))

class MainActivity : ComponentActivity() {
    private val leaderboardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("Leaderboards", "Leaderboard closed with resultCode=${result.resultCode}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Ads.init(this)
        PlayGamesSdk.initialize(this)
        Authentication.refreshAuthenticationStatus(this)

        enableEdgeToEdge()
        setContent {
            RollEmTheme {
                RollEmApp()
            }
        }
    }

    @Preview
    @Composable
    fun RollEmApp() {
        val configuration = LocalConfiguration.current
        var showGameOverDialog by remember { mutableStateOf(false) }
        var finalScore by remember { mutableLongStateOf(0L) }

        val bgImage = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                R.drawable.roll_em_background_portrait
            else
                R.drawable.roll_em_background_landscape


        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(bgImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.fillMaxSize()) {
                BannerAd(TOP_BANNER_ID)
                HelperOptions()
                Hud()
                GuessControlsRow()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Dice(
                        onGameOver = { score ->
                            finalScore = score
                            showGameOverDialog = true
                        }
                    )
                }
                BannerAd(BOTTOM_BANNER_ID)
            }

            if (showGameOverDialog) {
                GameOverDialog(
                    finalScore = finalScore,

                    onClose = {
                        resetGame(finalScore)
                        showGameOverDialog = false
                    }
                )
            }
        }
    }

    @Composable
    fun HelperOptions(){
        var showRulesDialog by remember { mutableStateOf(false) }
        val textColor = 0xFF2196F3

        if (showRulesDialog) {
            RulesDialog(onDismiss = { showRulesDialog = false })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                text = stringResource(R.string.how_to_play),
                textDecoration = TextDecoration.Underline,
                color = Color(textColor),
                modifier = Modifier.clickable {
                    showRulesDialog = true
                }
            )
            Text(
                text = stringResource(R.string.reset),
                textDecoration = TextDecoration.Underline,
                color = Color(textColor),
                modifier = Modifier.clickable {
                    GameState.resetGameState(this@MainActivity)
                }
            )
            Text(
                text = stringResource(R.string.game_stats),
                textDecoration = TextDecoration.Underline,
                color = Color(textColor),
                modifier = Modifier.clickable {
                    goToStatsScreen()
                }
            )
            if (Authentication.isAuthenticated) {
                Text(
                    text = stringResource(R.string.high_scores),
                    textDecoration = TextDecoration.Underline,
                    color = Color(textColor),
                    modifier = Modifier.clickable {
                        LeaderBoard.showLeaderboard(leaderboardLauncher, this@MainActivity)
                    }
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in),
                    textDecoration = TextDecoration.Underline,
                    color = Color(textColor),
                    modifier = Modifier.clickable {
                        Authentication.signIn(this@MainActivity)
                    }
                )
            }
        }
    }

    @Composable
    fun Hud() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF8B4513))
                .background(Color.Black)
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreDisplay()
            RollsLeftDisplay()
            StreakDisplay()
        }
    }

    @Composable
    fun Dice(
        modifier: Modifier = Modifier,
        onGameOver: (Long) -> Unit
    ) {
        var result by remember { mutableIntStateOf(1) }

        val imageResource = when (result) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        val context = LocalContext.current
        val isPreview = LocalInspectionMode.current

        val mp = remember(context, isPreview) {
            if (isPreview) null else MediaPlayer.create(context, R.raw.dice_roll)
        }

        Column(
            modifier = modifier
                .clickable(onClick = {
                    mp?.start()
                    result = (1..6).random()
                    GameState.updateGameState(result)
                    if (GameState.rollsLeft <= 0) {
                        onGameOver(GameState.score)
                    }
                }),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = result.toString(),
            )
        }
    }

    @Composable
    fun ScoreDisplay(modifier: Modifier = Modifier) {
        Text(
            modifier = modifier,
            fontFamily = CHALK_BOARD_FONT,
            color = Color.Green,
            text = stringResource(R.string.score, GameState.score),
        )
    }

    @Composable
    fun RollsLeftDisplay(modifier: Modifier = Modifier) {
        Text(
            modifier = modifier,
            fontFamily = CHALK_BOARD_FONT,
            color = Color.Red,
            text = stringResource(R.string.rolls_left, GameState.rollsLeft)
        )
    }

    @Composable
    fun StreakDisplay(modifier: Modifier = Modifier) {
        if (!GameState.getStreak.isEmpty()) {
            val streakAsCSV = GameState.getStreak.joinToString(", ")
            Text(
                modifier = modifier,
                color = Color.Yellow,
                fontFamily = CHALK_BOARD_FONT,
                text = stringResource(R.string.current_streak, streakAsCSV)
            )
        }
    }

    @Composable
    fun GuessControlsRow(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OddEvenSelector()
            RollGuessSelector()
        }
    }

    @Composable
    fun OddEvenSelector() {
        val options = OddEvenGuess.entries.toTypedArray()

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = {
                        GameState.oddEvenGuess = if (GameState.oddEvenGuess == option) null else option
                    },
                    selected = option == GameState.oddEvenGuess,
                    label = {
                        Text(stringResource(option.resource))
                    },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor =
                            if (option == OddEvenGuess.ODD)
                                Color.Red
                            else
                                Color.Green
                    )
                )
            }
        }
    }

    @Composable
    fun RollGuessSelector(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.next_guess, GameState.rollGuess),
                    color = Color.Black,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                (0..6).forEach { guess ->
                    DropdownMenuItem(
                        text = { Text(guess.toString()) },
                        onClick = {
                            GameState.rollGuess = guess
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Preview("Rules")
    @Composable
    fun RulesDialog(onDismiss: () -> Unit = {}) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.game_rules),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.game_rules_body)
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    @Preview("Game Over")
    @Composable
    fun GameOverDialog(finalScore: Long = 0L, onClose: () -> Unit = {}) {
        val isPreview = LocalInspectionMode.current
        val lastScore = if (isPreview) 0L else SaveManager.getLong(this@MainActivity, LAST_SCORE_KEY)

        AlertDialog(
            onDismissRequest = onClose,
            containerColor = Color(0xFF5B5B5B),
            title = {
                Text(
                    text = stringResource(R.string.game_over),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71A1A)
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.prev_score, lastScore),
                        fontSize = 12.sp,
                        color = Color(0xFFFBC02D)
                    )
                    Text(
                        text = stringResource(R.string.final_score, finalScore),
                        color = Color(0xFF2B8130)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.submit_score))
                }
            }
        )
    }

    private fun goToStatsScreen() {
        startActivity(Intent(this@MainActivity, StatsActivity::class.java))
    }

    private fun resetGame(finalScore: Long = 0L) {
        LeaderBoard.submitScore(finalScore, this@MainActivity)
        GameState.resetGameState(this@MainActivity)

        if (Ads.interstitialAd != null) {
            Ads.interstitialAd?.show(this)
        } else {
            Ads.loadInterstitialAd(this)
        }
    }
}