package com.example.rollem.admob

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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

object Ads {
    var interstitialAd: InterstitialAd? = null
        private set

    fun init(context: Context) {
        MobileAds.initialize(context)
        loadInterstitialAd(context)
    }

    fun loadInterstitialAd(context: Context) {
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
                            loadInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            loadInterstitialAd(context)
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
}