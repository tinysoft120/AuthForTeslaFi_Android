package com.teslafi.authandroid.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teslafi.authandroid.BuildConfig;
import com.teslafi.authandroid.R;
import com.teslafi.authandroid.utils.MyLog;

import java.net.URLEncoder;

public class MainWebViewActivity extends AppCompatActivity {
    private static final String TAG = MainWebViewActivity.class.getSimpleName();
    private static final String TELSAFI_LOGIN_URL = "https://www.teslafi.com/userlogin.php";
    private static final String TELSAFI_SIGNUP_URL = "https://www.teslafi.com/signup.php";
    private static final String TEST_URL = "https://www.teslafi.com/postTest.php";

    private WebView webView;
    private ProgressBar loadingProgressBar;
    private String refreshToken;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_web_view);
        boolean isSignup = getIntent().getBooleanExtra("signup", false);
        refreshToken = getIntent().getStringExtra("refresh_token");
        loadingProgressBar = findViewById(R.id.loading);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        webView = findViewById(R.id.main_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        clearCookies(this);
        loadWebView(isSignup);
    }

    @SuppressWarnings("deprecation")
    private void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else if (context != null) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void loadWebView(boolean isSignup) {
        String url;
        if (isSignup) {
            url = TELSAFI_SIGNUP_URL;
        } else {
            url = TELSAFI_LOGIN_URL;
        }
        String refreshToken = this.refreshToken;
        try {
            String postData = "refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8");
            webView.postUrl(url, postData.getBytes());
            loadingProgressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            MyLog.e(TAG, "refresh_token encoding error for " + refreshToken, e);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e(TAG, "onPageStarted: url=" + url );
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished: url=" + url );
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e(TAG, "shouldOverrideUrlLoading: url=" + request.getUrl().toString() );
            return false;
        }
    }
}
