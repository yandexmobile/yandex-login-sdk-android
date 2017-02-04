package com.yandex.loginsdk.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdk;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "fcdddf83a97843ae80815c1c9247015b";

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView tokenLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView jwtLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private YaLoginSdk sdk;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private View jwtContainer;

    @Nullable
    private Token token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(view -> sdk.login(this, null));
        final View jwtButton = findViewById(R.id.jwt);
        jwtButton.setOnClickListener(view -> getJwt());

        tokenLabel = (TextView) findViewById(R.id.status_label);
        jwtLabel = (TextView) findViewById(R.id.jwt_label);
        jwtContainer = findViewById(R.id.jwt_container);

        LoginSdkConfig config = LoginSdkConfig.builder()
                .clientId(CLIENT_ID)
                .applicationContext(getApplication())
                .build();
        sdk = YaLoginSdk.get(config);

        if (savedInstanceState != null) {
            sdk.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        sdk.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final boolean handled = sdk.onActivityResult(
                requestCode,
                resultCode,
                data,
                token -> {
                    this.token = token;
                    tokenLabel.setText(token.toString());
                    jwtContainer.setVisibility(View.VISIBLE);
                },
                error -> tokenLabel.setText(error.getMessage())
        );

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getJwt() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting");
        dialog.show();

        new AsyncTask<Void, Void, Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    assert token != null;
                    return sdk.getJwtBlocking(token.token());
                } catch (IOException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                dialog.cancel();

                if (o instanceof IOException) {
                    jwtLabel.setText(((IOException) o).getMessage());
                } else {
                    jwtLabel.setText(o.toString());
                }
            }
        }.execute();
    }
}
