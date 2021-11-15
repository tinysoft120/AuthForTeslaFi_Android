package com.teslafi.authandroid.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.teslafi.authandroid.R;
import com.teslafi.authandroid.data.TokenRegion;
import com.teslafi.authandroid.ui.main.MainActivity;
import com.teslafi.authandroid.utils.MyLog;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        MyLog.i(TAG, "onCreate");
        loadingProgressBar = findViewById(R.id.loading);
        findViewById(R.id.btnLoginTesla).setOnClickListener(view -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            Intent i = new Intent(LoginActivity.this, LoginViewActivity.class);
            i.putExtra("region", TokenRegion.GLOBAL.value);
            startActivityForResult(i, 0);
        });
        findViewById(R.id.btnLoginTeslaCn).setOnClickListener(view -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            Intent i = new Intent(LoginActivity.this, LoginViewActivity.class);
            i.putExtra("region", TokenRegion.CHINA.value);
            startActivityForResult(i, 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i(TAG, "onResume");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            showLoginOkAndFinish();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            loadingProgressBar.setVisibility(View.GONE);
            showLoginFailed(R.string.login_failed);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoginOkAndFinish() {
        Toast.makeText(getApplicationContext(), getString(R.string.succeed), Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        setResult(RESULT_OK);
        finish();
    }

    private void showLoginFailed(Integer num) {
        loadingProgressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), num, Toast.LENGTH_LONG).show();
    }

}
