package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "ARAF_AdManager"

        // Production Ad Unit IDs provided by the app owner
        const val ADMOB_APP_ID = "ca-apppub-8630539981886637~6183841293"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8630539981886637/9659519047"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-8630539981886637/1892709668"

        // Frequency Cap Rules:
        // Show every 2nd eligible trigger, minimum 60 seconds interval
        private const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    // Frequency capping counters
    private var eligibleTriggerCount = 0
    private var lastInterstitialShownTime = 0L

    init {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: $initializationStatus")
                loadInterstitialAd()
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks frequency capping and displays interstitial ad if eligible:
     * - Every 2nd trigger
     * - Minimum 60-second interval
     */
    fun showInterstitialIfEligible(activity: Activity, onAdClosed: () -> Unit = {}) {
        eligibleTriggerCount++
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastInterstitialShownTime

        val isEverySecondTrigger = (eligibleTriggerCount % 2 == 0)
        val isIntervalSatisfied = (timeSinceLast >= INTERSTITIAL_MIN_INTERVAL_MS)

        if (isEverySecondTrigger && isIntervalSatisfied && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    loadInterstitialAd()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial showing")
                }
            }
            interstitialAd?.show(activity)
        } else {
            // Not eligible or ad not loaded, continue immediately
            onAdClosed()
            if (interstitialAd == null) {
                loadInterstitialAd()
            }
        }
    }

    fun loadRewardedAd() {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Shows rewarded ad for extra hints.
     * If offline or ad unavailable, callback provides friendly fallback.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdUnavailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    onAdUnavailable()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            loadRewardedAd()
            onAdUnavailable()
        }
    }

    val isRewardedAdReady: Boolean
        get() = rewardedAd != null
}
