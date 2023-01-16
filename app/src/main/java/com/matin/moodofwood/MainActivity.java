package com.matin.moodofwood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.VolumeShaper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.color.DynamicColors;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBarWeb;
//    ProgressDialog progressDialog;
    RelativeLayout relativeLayout;
    Button btnNoInternetConnection;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean doubleBackToExitPressedOnce = false;

    private static final String TEL_PREFIX = "tel:";
    private static final String WHATSAPP_PREFIX = "whatsapp://";
    private static final String MAIL_PREFIX = "mailto:";
    private static final String FB_PREFIX = "fb://";
    private static final String TWITTER_POSTFIX = "MoodofWood1";
    private static final String INSTA_POSTFIX = "/moodofwood_/";
    private static final String LINKEDIN_POSTFIX = "/company/mood-of-wood/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.myWebView);
        WebSettings webSettings = webView.getSettings();
        progressBarWeb = (ProgressBar) findViewById(R.id.progressBar);
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Loading Please Wait");
        btnNoInternetConnection = (Button) findViewById(R.id.btnNoConnection);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setColorSchemeColors(Color.BLUE,Color.YELLOW,Color.GREEN);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        if(savedInstanceState == null) {
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setLoadsImagesAutomatically(true);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setSafeBrowsingEnabled(false);
            webSettings.setUseWideViewPort(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setSupportZoom(false);
            webSettings.setDefaultTextEncodingName("utf-8");
            webSettings.setAllowFileAccess(true);
            webSettings.setSaveFormData(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.clearCache(false);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            webView.clearHistory();
//            webSettings.setUserAgentString("; wv");

            checkConnection();
        }

        webView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(webView.getScrollY() == 0){
                    swipeRefreshLayout.setEnabled(true);
                }else{
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                                request.setMimeType(s3);
                                String cookies = CookieManager.getInstance().getCookie(s);
                                request.addRequestHeader("cookie",cookies);
                                request.addRequestHeader("User-Agent", s1);
                                request.setDescription("Downloading File....");
                                request.setTitle(URLUtil.guessFileName(s,s2,s3));
                                request.allowScanningByMediaScanner();

                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(s,s2,s3));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);
                                Toast.makeText(MainActivity.this, "Downloading File...",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                super.onReceivedError(view, errorCode, description, failingUrl);
                view.loadUrl("about:blank");
            }

            @Override
            public void onPageFinished(WebView view, String url){
                swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String Url){
                if(Url.startsWith(TEL_PREFIX)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(Url));
                    startActivity(intent);
                    return true;
                }
                if (Url.startsWith(MAIL_PREFIX) || Url.startsWith(WHATSAPP_PREFIX) || Url.startsWith(FB_PREFIX) || Url.endsWith(TWITTER_POSTFIX)
                        || Url.endsWith(LINKEDIN_POSTFIX) || Url.endsWith(INSTA_POSTFIX)){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(Url));
                    startActivity(intent);
                    return true;
                }
                view.loadUrl(Url);
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result){
                Log.e("alert triggered", message);
                return false;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress){
                progressBarWeb.setVisibility(View.VISIBLE);
                progressBarWeb.setProgress(newProgress);
                setTitle("Loading...");
//                progressDialog.show();
                if(newProgress == 100){
                    progressBarWeb.setVisibility(View.GONE);
                    setTitle(view.getTitle());
//                    progressDialog.dismiss();
                }
                super.onProgressChanged(view, newProgress);
                if(newProgress == 0){
                    progressBarWeb.setVisibility(View.GONE);
                    checkConnection();
                }
            }
        });

        btnNoInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNoInternetConnection.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnNoInternetConnection.setEnabled(true);
                    }
                },5000);
                checkConnection();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }
        else{
            if(doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Tap again to exit the store", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    public void checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        String webUrl = "https://moodofwood.in";
        if(wifi.isConnected()){
            webView.loadUrl (webUrl);
            webView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
        else if(mobileNetwork.isConnected()){
            webView.loadUrl (webUrl);
            webView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
        else{
            webView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            progressBarWeb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Check your internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_previous:
                onBackPressed();
                break;
            case R.id.nav_next:
                if(webView.canGoForward()){
                    webView.goForward();
                }
                break;
            case R.id.nav_reload:
                checkConnection();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}