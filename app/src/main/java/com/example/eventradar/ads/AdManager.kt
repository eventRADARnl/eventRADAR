package com.example.eventradar.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(private val context: Context) {
    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isShowingAd = false

    // Real Ad Unit IDs
    private val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3726596188291580/7289014410"
    private val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun initialize() {
        MobileAds.initialize(context) {}
        loadAppOpenAd()
        loadInterstitialAd()
    }

    private fun loadAppOpenAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                }
            }
        )
    }

    fun loadInterstitialAd() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity) {
        if (!isShowingAd && appOpenAd != null) {
            appOpenAd?.show(activity)
            appOpenAd = null
            loadAppOpenAd()
        }
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            onAdClosed()
        }
    }
}
