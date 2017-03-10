package com.yandex.yaloginsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class ActivityStarter {

    @Nullable
    private final Fragment fragment;

    @Nullable
    private final Activity activity;

    public ActivityStarter(@Nullable final Fragment fragment) {
        this.fragment = fragment;
        this.activity = null;
    }

    public ActivityStarter(@Nullable final Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public void startActivityForResult(@NonNull final Intent intent, final int requestCode) {
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            throw new IllegalStateException("Either activity or fragment should be set!");
        }
    }
}
