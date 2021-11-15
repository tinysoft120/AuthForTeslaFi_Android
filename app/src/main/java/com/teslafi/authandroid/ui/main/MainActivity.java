package com.teslafi.authandroid.ui.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

import com.teslafi.authandroid.R;
import com.teslafi.authandroid.data.Result;
import com.teslafi.authandroid.data.login.LoginDataSource;
import com.teslafi.authandroid.data.login.LoginResponseListener;
import com.teslafi.authandroid.data.login.Session;
import com.teslafi.authandroid.ui.login.LoginActivity;
import com.teslafi.authandroid.utils.MyLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar loadingProgressBar;
    private LoginDataSource loginDataSource;
    private TextView tvRefreshToken;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MyLog.d(TAG, "onCreate");
        setContentView(R.layout.main_activity);
        loginDataSource = LoginDataSource.getInstance(this);

        tvRefreshToken = findViewById(R.id.tv_refresh_token);

        Button btnLinkAccount = findViewById(R.id.btnLinkExisting);
        btnLinkAccount.setOnClickListener(view -> actionLinkAccount());
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(view -> actionCreateAccount());
        Button btnCopyToken = findViewById(R.id.btnCopyToken);
        btnCopyToken.setOnClickListener(view -> actionCopyRefreshToken());
        loadingProgressBar = findViewById(R.id.loading);

        if (!loginDataSource.isValidToken()) {
            logout();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginDataSource.getSession() == null) {
            MyLog.d(TAG, "onResume session null");
            startActivity(new Intent(this, LoginActivity.class));
            setResult(RESULT_OK);
            finish();
            return;
        }
        updateToken();
    }

    private void updateToken() {
        Session session = loginDataSource.getSession();
        MyLog.i(TAG, "updateToken(): "+session.refreshToken);
        tvRefreshToken.setText(session.refreshToken);
        //((TextView) findViewById(R.id.layout_copy_tokens_owner_api_token))
        //        .setHelperText(getString(R.string.copy_tokens_dialog_owner_at_expire,
        //                DateUtils.formatDateTime(this, Long.parseLong((session.createdAt + session.expiresIn) + "000"), 21)));
    }

    private void logout() {
        MyLog.i(TAG, "logout");
        loginDataSource.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void actionLinkAccount() {
        Intent i = new Intent(this, MainWebViewActivity.class);
        i.putExtra("signup", false);
        i.putExtra("refresh_token", tvRefreshToken.getText().toString());
        startActivityForResult(i, 101);
    }

    private void actionCreateAccount() {
        Intent i = new Intent(this, MainWebViewActivity.class);
        i.putExtra("signup", true);
        i.putExtra("refresh_token", tvRefreshToken.getText().toString());
        startActivityForResult(i, 102);
    }

    private void actionCopyRefreshToken() {
        Session session = loginDataSource.getSession();
        String token = session.refreshToken;
        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("com.teslafi.authandroid.tokens", token));
        Toast.makeText(getApplicationContext(), "Copied into clipboard", Toast.LENGTH_SHORT).show();
    }
}
