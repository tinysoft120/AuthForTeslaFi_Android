package net.leveugle.teslatokens.p008ui.login;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import net.leveugle.teslatokens.R;
import net.leveugle.teslatokens.data.login.LoginDataSource;
import net.leveugle.teslatokens.data.login.Session;
import net.leveugle.teslatokens.data.login.TeslaLoginLogic;
import net.leveugle.teslatokens.utils.MyLog;
import net.leveugle.teslatokens.utils.TaskRunner;

public class LoginViewActivity extends AppCompatActivity {
    private TeslaLoginLogic logic;
    private LoginDataSource loginDataSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);
        loginDataSource = LoginDataSource.getInstance(this);
        logic = new TeslaLoginLogic();
        WebView webView = findViewById(R.id.login_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(logic.getAuthorizeHttpUrl());
    }

    private void doLogin(final String str) {
        findViewById(R.id.login_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.login_webview).setVisibility(View.INVISIBLE);
        new TaskRunner().executeAsync(() -> {
            Session login = logic.login(str);
            loginDataSource.setLoggedInUser(login);
            return login;
        }, new TaskRunner.Callback<Session>() {

            @Override
            public void onComplete(Session session) {
                if (session == null) {
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

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
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
