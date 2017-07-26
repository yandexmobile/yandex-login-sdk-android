package com.yandex.yaloginsdk.internal;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.yandex.yaloginsdk.R;

public class WebViewLoginDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.webview_login_dialog_title)
                .setMessage(R.string.webview_login_dialog_message)
                .setPositiveButton(
                        R.string.webview_login_dialog_positive_button,
                        (view, which) -> startWebViewActivity()
                )
                .setNegativeButton(R.string.webview_login_dialog_negative_button, null)
                .create();
    }

    private void startWebViewActivity() {
        final Intent loginIntent = new Intent(getActivity(), WebViewLoginActivity.class);
        loginIntent.putExtras(getArguments());
        getActivity().startActivityForResult(loginIntent, LoginSdkActivity.LOGIN_REQUEST_CODE);
    }
}
