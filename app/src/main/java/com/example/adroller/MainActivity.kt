package com.example.adroller

import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adroller.gamestate.GameState
import com.example.adroller.ui.theme.RollEmTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

const val BOTTOM_BANNER_ID = "ca-app-pub-2470800019467760/3164718645"
const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
const val TOP_BANNER_ID = "ca-app-pub-2470800019467760/8597513307"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
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
                TopAd()
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Dice()
                }
                BottomAd()
            }
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
    fun TopAd(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxWidth().navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            BannerAd(TOP_BANNER_ID)
        }
    }

    @Composable
    fun BottomAd(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxWidth().navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            BannerAd(BOTTOM_BANNER_ID)
        }
    }

    @Composable
    fun BannerAd(adId: String = "") {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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