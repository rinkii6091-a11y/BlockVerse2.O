package com.example.ads

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Google AdMob Monetization Manager for BlockFlow.
 *
 * Implements high-earning, non-irritating monetization:
 * 1. Home Screen Banner Ad: Steady recurring impressions without obstructing gameplay.
 * 2. Rewarded Ads on Defeat: High-value player opt-in (Second Chance / Revive & Double Coins) with premium eCPMs.
 * 3. Interstitial Ads on Victory: Smart frequency capped (every 2nd victory) to maximize LTV without player churn.
 *
 * Uses official Google AdMob test ad unit IDs during development.
 */
object AdMobManager {

    // Official Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading: Boolean = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading: Boolean = false

    // Smart Frequency Capping: show interstitial every 2nd victory
    private var levelWinsSinceLastInterstitial = 0
    private const val INTERSTITIAL_WIN_INTERVAL = 2

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .build()
            MobileAds.setRequestConfiguration(reqConfig)

            val appContext = context.applicationContext
            Thread {
                try {
                    MobileAds.initialize(appContext) {
                        isInitialized = true
                        loadInterstitial(appContext)
                        loadRewarded(appContext)
                    }
                } catch (_: Exception) {}
            }.start()
        } catch (_: Exception) {}
    }

    fun prepareGameAds(context: Context) {
        if (!isInitialized) {
            initialize(context)
        } else {
            loadInterstitial(context.applicationContext)
            loadRewarded(context.applicationContext)
        }
    }

    // ==========================================
    // INTERSTITIAL ADS (On Level Win)
    // ==========================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                TEST_INTERSTITIAL_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                    }
                }
            )
        } catch (_: Exception) {
            isInterstitialLoading = false
        }
    }

    /**
     * Shows an interstitial ad upon winning a level.
     * Capped at every 2nd win to preserve high player retention and avoid annoyance.
     */
    fun showInterstitialOnWin(activity: Activity, onAdDismissed: () -> Unit) {
        levelWinsSinceLastInterstitial++

        // Frequency capping: only show every 2nd win
        if (levelWinsSinceLastInterstitial < INTERSTITIAL_WIN_INTERVAL) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    levelWinsSinceLastInterstitial = 0
                    loadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            // If ad not cached yet, do not delay player - proceed seamlessly
            loadInterstitial(activity.applicationContext)
            onAdDismissed()
        }
    }

    // ==========================================
    // REWARDED ADS (On Defeat / Second Chance)
    // ==========================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                TEST_REWARDED_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                    }
                }
            )
        } catch (_: Exception) {
            isRewardedLoading = false
        }
    }

    fun isRewardedAvailable(): Boolean = rewardedAd != null

    /**
     * Shows rewarded video ad when the player opts in (e.g. Second Chance or Double Rewards on Defeat).
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (rewardGranted) {
                        onUserEarnedReward()
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    onAdClosed()
                }
            }

            ad.show(activity) { _ ->
                rewardGranted = true
            }
        } else {
            // Graceful fallback: grant reward and attempt reload
            loadRewarded(activity.applicationContext)
            onUserEarnedReward()
            onAdClosed()
        }
    }
}

/**
 * Adaptive Google AdMob Banner Composable for the Home Screen.
 * Uses software rendering layer to ensure stability in emulator and cloud environments.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.TEST_BANNER_ID
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x221E293B))
            .border(1.dp, Color(0x22818CF8), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                try {
                    AdView(context).apply {
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                // Silent fallback on offline/emulator testing
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                } catch (_: Exception) {
                    View(context)
                }
            }
        )
    }
}
