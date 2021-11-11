package net.leveugle.teslatokens.p008ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import net.leveugle.teslatokens.R;
import net.leveugle.teslatokens.p008ui.mainactivity.MainActivity;
import net.leveugle.teslatokens.utils.MyLog;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        MyLog.i(TAG, "onCreate");
        loadingProgressBar = findViewById(R.id.loading);
        findViewById(R.id.login_using_tesla_account).setOnClickListener(view -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            startActivityForResult(new Intent(LoginActivity.this, LoginViewActivity.class), 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i(TAG, "onResume");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            showLoginOkAndFinish();
            return;
        }
        if (resultCode == 0) {
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
