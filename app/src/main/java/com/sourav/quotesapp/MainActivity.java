package com.sourav.quotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private String[][] quotes;

    // AdMob Variables
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private int actionCount = 0;
    private static final int AD_SHOW_INTERVAL = 5; // Show ad every 5 swipes/actions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob Initialized");
        });

        // 2. Banner Ad Setup
        mAdView = findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        // 3. Load Initial Interstitial Ad
        loadInterstitialAd();

        // UI Binding
        viewPager = findViewById(R.id.viewPager);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        ImageButton btnNext = findViewById(R.id.btnNext);
        FloatingActionButton btnShare = findViewById(R.id.btnShare);

        // Data & Adapter Setup
        quotes = QuotesData.getQuotes();
        QuoteAdapter adapter = new QuoteAdapter(quotes);
        viewPager.setAdapter(adapter);

        // Premium Page Transformer
        viewPager.setPageTransformer(new DepthPageTransformer());

        // Click Listeners
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < quotes.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnShare.setOnClickListener(v -> shareQuote());

        // Ad logic on page change (handles both button clicks and manual swipes)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                handleAdLogic();
            }
        });
    }

    private void shareQuote() {
        int currentPos = viewPager.getCurrentItem();
        String shareBody = "\"" + quotes[currentPos][0] + "\"\n\n- " + quotes[currentPos][1] + "\n\nShared via Quotes App";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Inspirational Quote");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "Interstitial Loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        Log.e(TAG, "Interstitial Failed to Load: " + loadAdError.getMessage());
                    }
                });
    }

    private void handleAdLogic() {
        actionCount++;
        if (actionCount >= AD_SHOW_INTERVAL) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
                actionCount = 0;
                loadInterstitialAd(); // Load next ad
            } else {
                loadInterstitialAd(); // Retry loading if not available
            }
        }
    }

    // Lifecycle methods for Banner Ad
    @Override
    protected void onPause() {
        if (mAdView != null) mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) mAdView.resume();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) mAdView.destroy();
        super.onDestroy();
    }

    // Custom Page Transformer for Smooth Swiping
    public static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;
        @Override
        public void transformPage(@NonNull android.view.View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) {
                view.setAlpha(0f);
            } else if (position <= 0) {
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) {
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else {
                view.setAlpha(0f);
            }
        }
    }
}
