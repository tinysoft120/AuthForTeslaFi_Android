package com.teslafi.authandroid.ui.login;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teslafi.authandroid.BuildConfig;
import com.teslafi.authandroid.R;
import com.teslafi.authandroid.data.TokenRegion;
import com.teslafi.authandroid.data.login.LoginDataSource;
import com.teslafi.authandroid.data.login.Session;
import com.teslafi.authandroid.data.login.TeslaLoginLogic;
import com.teslafi.authandroid.utils.MyLog;
import com.teslafi.authandroid.utils.TaskRunner;

public class LoginViewActivity extends AppCompatActivity {
    private static final String TAG = LoginViewActivity.class.getSimpleName();

    private TeslaLoginLogic logic;
    private LoginDataSource loginDataSource;
    private TokenRegion region;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);
        loginDataSource = LoginDataSource.getInstance(this);
        logic = new TeslaLoginLogic();
        region = getIntent().getIntExtra("region", 0) == 0 ? TokenRegion.GLOBAL : TokenRegion.CHINA;
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        WebView webView = findViewById(R.id.login_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        clearCookies(this);
        webView.clearCache(true);
        webView.clearHistory();
        webView.loadUrl(logic.getAuthorizeHttpUrl(region));
    }

    private void doLogin(final String code) {
        findViewById(R.id.login_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.login_webview).setVisibility(View.INVISIBLE);
        new TaskRunner().executeAsync(() -> {
            Session login = logic.login(code);
            loginDataSource.setLoggedInUser(login);
            return login;
        }, new TaskRunner.Callback<Session>() {

            @Override
            public void onComplete(Session result) {
                if (result == null) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception exc) {
                MyLog.e("LoginViewActivity", "error on login call", exc);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
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
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e(TAG, "shouldOverrideUrlLoading: url=" + request.getUrl().toString() );
            String error = request.getUrl().getQueryParameter("error");
            if (error != null && !error.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
            }
            String loginHint = request.getUrl().getQueryParameter("login_hint");
            if (loginHint != null && !"".equals(loginHint)) {
                logic.teslaEnv = request.getUrl().getHost();
            }
            String code = request.getUrl().getQueryParameter("code");
            if (code == null || "".equals(code)) {
                return false;
            }
            doLogin(code);
            return false;
        }
    }
}
