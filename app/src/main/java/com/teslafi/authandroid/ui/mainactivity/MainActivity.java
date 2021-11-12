package com.teslafi.authandroid.ui.mainactivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import java.util.Date;
import java.util.Locale;
import com.teslafi.authandroid.BuildConfig;
import com.teslafi.authandroid.R;
import com.teslafi.authandroid.data.Result;
import com.teslafi.authandroid.data.login.LoginDataSource;
import com.teslafi.authandroid.data.login.LoginResponseListener;
import com.teslafi.authandroid.data.login.Session;
import com.teslafi.authandroid.ui.login.LoginActivity;
import com.teslafi.authandroid.utils.MyLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ProgressBar loadingProgressBar;
    private LoginDataSource loginDataSource;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MyLog.d(TAG, "onCreate");
        setContentView(R.layout.main_activity);
        refreshButton = findViewById(R.id.main_refresh_button);
        refreshButton.setOnClickListener(view -> {
            MyLog.d(TAG, "refreshButton onClick");
            if (new Date().getTime() / 1000 < ((long) (loginDataSource.getSession().createdAt))) {
                MyLog.d(TAG, "refreshButton onClick too early");
                Toast.makeText(this, R.string.main_refresh_too_early, Toast.LENGTH_LONG).show();
            }
        });
        refreshButton.setEnabled(false);
        loginDataSource = LoginDataSource.getInstance(this);
        findViewById(R.id.main_purchase_button).setOnClickListener(view -> MyLog.d(TAG, "purchaseButton onClick"));
        loadingProgressBar = findViewById(R.id.loading);
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.d(TAG, "onResume appCode:16 appVersion:1.2.1");
        if (loginDataSource.getSession() == null) {
            MyLog.d(TAG, "onResume session null");
            startActivity(new Intent(this, LoginActivity.class));
            setResult(RESULT_OK);
            finish();
            return;
        }
        populateFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_logged_in_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.main_menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateFields() {
        MyLog.i(TAG, "populateFields");
        Session session = loginDataSource.getSession();
        initEditTextWithToken(findViewById(R.id.copy_tokens_owner_api_token), session.accessToken);
        ((TextInputLayout) findViewById(R.id.layout_copy_tokens_owner_api_token))
                .setHelperText(getString(R.string.copy_tokens_dialog_owner_at_expire, DateUtils.formatDateTime(this, Long.parseLong((session.createdAt + session.expiresIn) + "000"), 21)));
        initEditTextWithToken(findViewById(R.id.copy_tokens_sso_refresh_token), session.refreshToken);
    }

    private void logout() {
        MyLog.i(TAG, "logout");
        loginDataSource.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEditTextWithToken(final EditText editText, final String str) {
        editText.setText(str);
        editText.setInputType(0);
        editText.setOnTouchListener((view, motionEvent) -> {
            MyLog.d(TAG, "onTouch");
            if (motionEvent.getAction() == 1) {
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                    if (motionEvent.getRawX() >= ((float) (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width()))) {
                        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                                .setPrimaryClip(ClipData.newPlainText("com.teslafi.authandroid.tokens", str));
                        Toast.makeText(getApplicationContext(), "Copied into clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else if (motionEvent.getRawX() <= ((float) (editText.getLeft() + editText.getCompoundDrawables()[0].getBounds().width()))) {
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("com.teslafi.authandroid.tokens", str));
                    Toast.makeText(getApplicationContext(), "Copied into clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            return false;
        });
    }

    private void refreshToken() {
        MyLog.d(TAG, "refreshToken");
        if ((new Date().getTime() / 1000) < loginDataSource.getSession().createdAt) {
            MyLog.d(TAG, "refreshToken too early");
            Toast.makeText(this, R.string.main_refresh_too_early, Toast.LENGTH_LONG).show();
            return;
        }
        refreshButton.setEnabled(false);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loginDataSource.refreshSession(new LoginResponseListener() {

            @Override
            public void onError(Result.Error error) {
                MyLog.e(TAG, "refreshToken onError", error.getError());
                Toast.makeText(MainActivity.this, R.string.main_refresh_error, Toast.LENGTH_LONG).show();
                refreshButton.setEnabled(true);
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(Result.Success<Session> success) {
                MyLog.d(TAG, "refreshToken onResponse");
                populateFields();
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
