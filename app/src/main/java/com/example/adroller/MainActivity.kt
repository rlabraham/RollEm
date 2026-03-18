package com.example.adroller

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adroller.gamestate.GameState
import com.example.adroller.gamestate.OddEvenGuess
import com.example.adroller.ui.theme.RollEmTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

const val BOTTOM_BANNER_ID = "ca-app-pub-2470800019467760/3164718645"
const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
const val TOP_BANNER_ID = "ca-app-pub-2470800019467760/8597513307"

const val RESET_AD_ID = "ca-app-pub-2470800019467760/7973716834"
const val RESET_AD_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

private val CHALK_BOARD_FONT = FontFamily(Font(R.font.chalk_board))

class MainActivity : ComponentActivity() {
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        loadInterstitialAd(this)
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
                OddEvenNoneSelector()
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Dice()
                }
                BannerAd(BOTTOM_BANNER_ID)
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
                text = stringResource(R.string.high_scores),
                textDecoration = TextDecoration.Underline,
                color = Color(textColor)
            )
            Text(
                text = stringResource(R.string.reset),
                textDecoration = TextDecoration.Underline,
                color = Color(textColor),
                modifier = Modifier.clickable{
                    resetGame()
                }
            )
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
    fun Dice(modifier: Modifier = Modifier) {
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
                        resetGame()
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
    fun OddEvenNoneSelector(modifier: Modifier = Modifier) {
        val options = OddEvenGuess.entries.toTypedArray()

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
    }

    @Composable
    fun BannerAd(adId: String = "") {
        Box(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)

                            val adRequest = AdRequest.Builder().build()
                            val adUnitId = if (adRequest.isTestDevice(context)) TEST_BANNER_ID else adId

                            this.adUnitId = adUnitId
                            loadAd(adRequest)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun RulesDialog(onDismiss: () -> Unit) {
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
                    text = """
You start with 10 rolls

With each roll you earn points equal to that roll

If you roll the same value multiple times in a row they add the length of that streak to their score. This happens each time the streak is continued.
  • A 6 always continues the streak
  • A 1 always ends the streak

With each roll you may guess odd or even
  • If they guess correctly they don't lose a roll
  • If they guess incorrectly they lose two rolls (instead of one)

The game ends when all rolls are depleted
                    """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    private fun resetGame() {
        if (interstitialAd != null) {
            interstitialAd?.show(this)
        } else {
            loadInterstitialAd(this)
        }
        GameState.resetGameState()
    }

    private fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        val adUnitId = if (adRequest.isTestDevice(context)) RESET_AD_TEST_ID else RESET_AD_ID

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            interstitialAd = null
                        }

                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitialAd(this@MainActivity)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            loadInterstitialAd(this@MainActivity)
                        }
                    }
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            },
        )
    }
}