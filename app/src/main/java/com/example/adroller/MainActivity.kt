package com.example.adroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adroller.ui.theme.DiceRollerTheme
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
        initAds()
        enableEdgeToEdge()
        setContent {
            DiceRollerTheme {
                DiceRollerApp()
            }
        }
    }

    @Preview
    @Composable
    fun DiceRollerApp() {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAd()
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DiceWithButtonAndImage()
            }
            BottomAd()
        }
    }

    @Composable
    fun DiceWithButtonAndImage(modifier: Modifier = Modifier) {
        var result by remember { mutableStateOf(1) }

        val imageResource = when (result) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = result.toString()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {result = (1..6).random()}) {
                Text(stringResource(R.string.roll))
            }
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

                        val adRequest = AdRequest.Builder().build();
                        val adUnitId = if (adRequest.isTestDevice(context)) TEST_BANNER_ID else adId

                        this.adUnitId = adUnitId
                        loadAd(adRequest)
                    }
                }
            )
        }
    }

    private fun initAds() {
        MobileAds.initialize(this)
    }
}