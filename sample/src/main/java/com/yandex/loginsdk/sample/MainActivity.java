package com.yandex.loginsdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.yaloginsdk.Config;
import com.yandex.yaloginsdk.YaLoginSdk;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "57a8133c69d947388a67164dfdbc46d3";

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView label;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private YaLoginSdk sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = (TextView) findViewById(R.id.status_label);
        findViewById(R.id.login).setOnClickListener(view -> sdk.login(this, null));

        Config config = Config.builder()
                .clientId(CLIENT_ID)
                .applicationContext(getApplication())
                .build();
        sdk = YaLoginSdk.get(config);

        if (savedInstanceState != null) {
            sdk.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        sdk.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final boolean handled = sdk.onActivityResult(
                requestCode,
                resultCode,
                data,
                token -> label.setText(token.toString()),
                error -> {
                    label.setText(error.getMessage());
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
