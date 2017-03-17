package com.yandex.yaloginsdk.internal.strategy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.ActivityStarter;
import com.yandex.yaloginsdk.internal.WebViewLoginDialogFragment;

import java.util.Set;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.internal.strategy.LoginType.WEBVIEW;

class WebViewLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get() {
        return new WebViewLoginStrategy();
    }

    @Override
    public void login(
            @NonNull final ActivityStarter activityStarter,
            @NonNull final LoginSdkConfig config,
            @NonNull final Set<String> scopes
    ) {
        final WebViewLoginDialogFragment dialog = new WebViewLoginDialogFragment();
        dialog.setArguments(extras(scopes, config));
        activityStarter.showDialogFragment(dialog);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return WEBVIEW;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public Token tryExtractToken(@NonNull Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull Intent data) {
            return (YaLoginSdkError) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
