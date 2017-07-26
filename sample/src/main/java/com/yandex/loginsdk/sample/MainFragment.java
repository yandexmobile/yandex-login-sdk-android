package com.yandex.loginsdk.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdk;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.Arrays;

public class MainFragment extends Fragment {

    public static final String CLIENT_ID = "fcdddf83a97843ae80815c1c9247015b";
    private static final int REQUEST_LOGIN_SDK = 1;

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

    @Nullable
    private String jwt;

    public MainFragment() {
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final View loginButton = view.findViewById(R.id.login);
        loginButton.setOnClickListener(v ->
                startActivityForResult(sdk.createLoginIntent(getActivity(), null), REQUEST_LOGIN_SDK));
        final View jwtButton = view.findViewById(R.id.jwt);
        jwtButton.setOnClickListener(v -> getJwt());

        tokenLabel = (TextView) view.findViewById(R.id.status_label);
        jwtLabel = (TextView) view.findViewById(R.id.jwt_label);
        jwtContainer = view.findViewById(R.id.jwt_container);

        sdk = YaLoginSdk.get(new LoginSdkConfig(getContext(), true));

        if (token != null) {
            onTokenReceived(token);
        }
        if (jwt != null) {
            onJwtReceived(jwt);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final boolean handled = sdk.onActivityResult(
                resultCode,
                data,
                this::onTokenReceived,
                error -> tokenLabel.setText(error.getMessage())
        );

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onTokenReceived(@NonNull Token token) {
        this.token = token;
        tokenLabel.setText(token.toString());
        jwtContainer.setVisibility(View.VISIBLE);
    }

    private void onJwtReceived(@NonNull String jwt) {
        this.jwt = jwt;
        jwtLabel.setText(jwt);
    }

    private void getJwt() {
        final DialogFragment dialog = new ProgressDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), ProgressDialogFragment.TAG);

        assert token != null;

        new Thread(() -> {
            try {
                final String jwt = sdk.getJwt(token.token());
                getActivity().runOnUiThread(() -> {
                    onJwtReceived(jwt);
                    dismissProgress();
                });
            } catch (YaLoginSdkError e) {
                getActivity().runOnUiThread(() -> {
                    jwtLabel.setText(Arrays.toString(e.getErrors()));
                    dismissProgress();
                });
            }

        }).start();
    }

    private void dismissProgress() {
        final Fragment dialogFragment = getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        if (dialogFragment != null) {
            ((DialogFragment) dialogFragment).dismiss();
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        private static final String TAG = ProgressDialogFragment.class.getCanonicalName();

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Waiting");
            dialog.setCancelable(false);
            return dialog;
        }
    }
}
