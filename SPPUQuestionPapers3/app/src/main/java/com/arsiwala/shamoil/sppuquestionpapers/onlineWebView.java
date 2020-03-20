package com.arsiwala.shamoil.sppuquestionpapers;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.Objects;

import static android.content.Context.DOWNLOAD_SERVICE;

public class onlineWebView extends Fragment implements RewardedVideoAdListener {

    private ProgressBar mProgressBar;
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;
    private WebView webView;
    private EditText editText;
    private String[] splitLink;
    private DownloadManager.Request request;
    private DownloadManager downloadManager;
    private String subject_name, link, TAG = "SQP_LOGS_onlineWebView", user_filename;
    private Boolean watchedAd = false, bool_setDesktopView = false, isSyllabus = true, isError = false;


    public onlineWebView() {
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_online_web_view, container, false);
//        androidx.appcompat.app.AlertDialog.Builder(  , R.style.AlertDialogTheme);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
        Bundle args = this.getArguments();

        assert args != null;
        subject_name = args.getString("subject_name");
        link = args.getString("subject_link");
        isSyllabus = args.getBoolean("isSyllabus", false);

        mProgressBar = rootView.findViewById(R.id.pb);
        webView = rootView.findViewById(R.id.id_onlineWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            //RETURN TRUE MATLAB IT WON'T LOAD, RETURN FALSE MATLAB IT WILL LOAD
            public boolean shouldOverrideUrlLoading(WebView wView, String url) {
                Log.d(TAG, "Auto loading: " + url);
                return !customShouldOverloadUrl(url);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, "Error occurred - " + description + ". URL = " + failingUrl);
                webView.setVisibility(View.INVISIBLE);
                isError = true;

                new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle("Web page not available")
                        .setCancelable(false)
                        .setMessage(description)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                load_webpage();
                            }
                        })
                        .show();
            }

            @Override
            public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
                if(!isError) {
                    webView.setVisibility(View.VISIBLE);
                }
                isError = false;
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                Log.d(TAG, "SSL Error occurred - " + error.toString());
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case android.net.http.SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case android.net.http.SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case android.net.http.SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case android.net.http.SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                mProgressBar.setProgress(progress);
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == MotionEvent.ACTION_UP
                        && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        loadRewardedVideoAd();

        mInterstitialAd = new InterstitialAd(Objects.requireNonNull(getContext()));
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // Test Ads
        mInterstitialAd.setAdUnitId("ca-app-pub-4429552861546129/8356998874"); // Real Ads
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        load_webpage();

        Log.i(TAG, "setDesktopView getting value from Firebase");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("setDesktopView");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bool_setDesktopView = dataSnapshot.getValue(Boolean.class);
                Log.i(TAG, "setDesktopView got value " + bool_setDesktopView);
                if(bool_setDesktopView)
                    setDesktopMode(webView, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bool_setDesktopView = false;
//                setDesktopMode(webView, false);
            }
        });

        return rootView;
    }


    private boolean customShouldOverloadUrl(String url) {
        Uri uri = Uri.parse(url);
        if (Objects.equals(uri.getQueryParameter("export"), "download") || Objects.equals(uri.getQueryParameter("e"), "download")) {
            ((MainActivity) Objects.requireNonNull(getActivity())).alertMessage("To Download", "1. Click on the 3 dot menu on top right corner." +
                    "\n\n2. Select \"Open in new window\"");
            Log.i(TAG, "Download button pressed pointlessly");
            return false;
        }

        if (!(url.equals("about:blank")) && Objects.equals(uri.getHost(), "drive.google.com")) {
            if (url.contains("/file/d/")) {
                downloadAndShareAlert(url);
                return isSyllabus;
            }
            return true;
        } else if (!(url.equals("about:blank")) && url.contains("export-anonymous")) {
            Toast.makeText(getContext(), "Download failed", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Bulk download button pressed pointlessly");
            return false;

        } else {
            Log.d(TAG, "link not supported");
            return false;
        }
    }

    private void load_ad() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            requestNewInterstitial();
            download_paper_2();
        } else {
            Log.d(TAG, "Ad not loaded. Paper not downloaded");
            Toast.makeText(getContext(), "Ad not loaded. Download not started", Toast.LENGTH_SHORT).show();
        }
    }

    private void share_paper(String url) {
        final ProgressDialog progressDialogForShare = new ProgressDialog(getContext());
        progressDialogForShare.setMessage("Generating URL...");
        progressDialogForShare.setCancelable(false);
        progressDialogForShare.setIcon(R.mipmap.app_logo_transparent);
        progressDialogForShare.show();

        url = "https://sppuquespaper.page.link/?link=" + url + "&apn=" + Objects.requireNonNull(getContext()).getPackageName() + "&amv=24&ofl=https://play.google.com/store/apps/details?id=com.arsiwala.shamoil.sppuquestionpapers";

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(url))
                .buildShortDynamicLink()
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = Objects.requireNonNull(task.getResult()).getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            progressDialogForShare.dismiss();
                            Log.d(TAG, "setDynamicLink:onComplete: " + shortLink.toString() + "  " + flowchartLink.toString());
                            ((MainActivity) Objects.requireNonNull(getActivity())).share_message("Open with _SPPU Question Paper_ App \n\n*Title :* " + subject_name + (!(user_filename.equals("")) ? ("\n*File name :* " + user_filename) : "") + "\n*Link :* " + shortLink.toString());
                        } else {
                            Log.d(TAG, "setDynamicLink:onComplete: failed to generate short link");
                            Toast.makeText(getContext(), "Task failed", Toast.LENGTH_SHORT).show();
                            progressDialogForShare.dismiss();
                        }
                    }
                });
    }

    private void downloadAndShareAlert(final String url) {
//        final Uri my_uri = Uri.parse(url);
        splitLink = url.split("/");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.edittext_alertdialog_layout, null);
        dialogBuilder.setView(dialogView);

        editText = dialogView.findViewById(R.id.id_file_name);
        dialogBuilder.setTitle("File name :");
        dialogBuilder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                user_filename = editText.getText().toString();
                if (user_filename.equals("")) {
                    Toast.makeText(getContext(), "File name can't be empty", Toast.LENGTH_LONG).show();
                    downloadAndShareAlert(url);
                } else {
                    dialog.dismiss();
                    Uri uri = Uri.parse("https://" + splitLink[2] + "/uc?authuser=0&id=" + splitLink[5] + "&export=download");
                    downloadManager = (DownloadManager) Objects.requireNonNull(getActivity()).getSystemService(DOWNLOAD_SERVICE);
                    request = new DownloadManager.Request(uri);
                    load_ad();
                }
            }
        });
        dialogBuilder.setNegativeButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user_filename = editText.getText().toString();
                dialog.dismiss();
                share_paper(url);
            }
        });
        dialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.create().show();
    }

    private void download_paper_2() {
        request.setTitle(user_filename); //name of downloads
        request.setDescription("Downloading Paper...");
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);
        request.setDestinationInExternalFilesDir(getContext(), "/downloaded_papers/", "." + user_filename + ".pdf"); //name in directory
        downloadManager.enqueue(request);

        Log.d(TAG, "Download started - " + user_filename);
        Toast.makeText(getContext(), "Downloading " + user_filename, Toast.LENGTH_SHORT).show();
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-4429552861546129/3635757879",
                new AdRequest.Builder()
                        .addTestDevice("FB868D2E89162749B361D2FC8C0E7B73")
                        .addTestDevice("87AE4EBEC30DE492057F7A76C6AB89DE")
                        .build());
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("FB868D2E89162749B361D2FC8C0E7B73")
                .addTestDevice("87AE4EBEC30DE492057F7A76C6AB89DE")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void setDesktopMode(WebView wv, boolean enabled) {
        String newUserAgent = wv.getSettings().getUserAgentString();
        if (enabled) {
            try {
                String ua = wv.getSettings().getUserAgentString();
                String androidOSString = wv.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
                newUserAgent = wv.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        wv.getSettings().setUserAgentString(newUserAgent);
        wv.getSettings().setUseWideViewPort(enabled);
        wv.getSettings().setLoadWithOverviewMode(enabled);
        load_webpage();
    }

    private void load_webpage() {
        if (customShouldOverloadUrl(link)) {
            Log.d(TAG, "Manual loading: " + link);
            webView.loadUrl(link);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(TAG, "onRewardedVideoAdClosed() is called");
        if (!watchedAd) {
            Log.d(TAG, "Ad Closed before Reward");
            new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle("Error")
                    .setCancelable(false)
                    .setMessage("To start the download, you will have to watch the whole video ad. It also helps the developer to keep the app updated.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    })
                    .show();
        }
        watchedAd = false;
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        watchedAd = true;
        Log.d(TAG, "onRewarded() is called");
        download_paper_2();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {
        Log.d(TAG, "onRewardedVideoCompleted() is called");
    }
}
