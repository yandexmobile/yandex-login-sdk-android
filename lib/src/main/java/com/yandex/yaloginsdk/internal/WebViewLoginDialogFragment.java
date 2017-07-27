package com.yandex.yaloginsdk.internal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
                .setNegativeButton(
                        R.string.webview_login_dialog_negative_button,
                        (view, which) -> dismiss()
                )
                .create();
    }

    private void startWebViewActivity() {
        final Intent loginIntent = new Intent(getActivity(), WebViewLoginActivity.class);
        loginIntent.putExtras(getArguments());
        getActivity().startActivityForResult(loginIntent, LoginSdkActivity.LOGIN_REQUEST_CODE);
    }
}
