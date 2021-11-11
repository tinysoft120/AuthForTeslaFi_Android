package net.leveugle.teslatokens.ui.mainactivity;

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
import net.leveugle.teslatokens.BuildConfig;
import net.leveugle.teslatokens.R;
import net.leveugle.teslatokens.data.Result;
import net.leveugle.teslatokens.data.login.LoginDataSource;
import net.leveugle.teslatokens.data.login.LoginResponseListener;
import net.leveugle.teslatokens.data.login.Session;
import net.leveugle.teslatokens.ui.login.LoginActivity;
import net.leveugle.teslatokens.utils.MyLog;

public class MainActivity extends AppCompatActivity {
    private static final int MINIMUM_DELAY_BEFORE_REFRESH_SECONDS = 0;
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
            MyLog.d(MainActivity.TAG, "refreshButton onClick");
            if (new Date().getTime() / 1000 < ((long) (loginDataSource.getSession().createdAt))) {
                MyLog.d(MainActivity.TAG, "refreshButton onClick too early");
                Toast.makeText(MainActivity.this, R.string.main_refresh_too_early, Toast.LENGTH_LONG).show();
            }
        });
        refreshButton.setEnabled(false);
        loginDataSource = LoginDataSource.getInstance(this);
        findViewById(R.id.main_purchase_button).setOnClickListener(view -> MyLog.d(MainActivity.TAG, "purchaseButton onClick"));
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
            MyLog.d(MainActivity.TAG, "onTouch");
            if (motionEvent.getAction() == 1) {
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                    if (motionEvent.getRawX() >= ((float) (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width()))) {
                        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                                .setPrimaryClip(ClipData.newPlainText("net.leveugle.teslatokens.tokens", str));
                        Toast.makeText(getApplicationContext(), "Copied into clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else if (motionEvent.getRawX() <= ((float) (editText.getLeft() + editText.getCompoundDrawables()[0].getBounds().width()))) {
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("net.leveugle.teslatokens.tokens", str));
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
                MyLog.e(MainActivity.TAG, "refreshToken onError", error.getError());
                Toast.makeText(MainActivity.this, R.string.main_refresh_error, Toast.LENGTH_LONG).show();
                refreshButton.setEnabled(true);
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(Result.Success<Session> success) {
                MyLog.d(MainActivity.TAG, "refreshToken onResponse");
                populateFields();
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
