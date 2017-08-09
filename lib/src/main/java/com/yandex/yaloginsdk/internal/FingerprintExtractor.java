package com.yandex.yaloginsdk.internal;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.YandexAuthOptions;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FingerprintExtractor {

    private static final String TAG = FingerprintExtractor.class.getSimpleName();

    @Nullable
    public String[] get(@NonNull final String packageName, @NonNull final PackageManager packageManager, @NonNull final YandexAuthOptions options) {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            final PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            final String[] result = new String[info.signatures.length];
            int i = 0;
            for (final Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                result[i++] = toHex(md.digest());
            }
            return result;
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Logger.e(options, TAG, "Error getting fingerprint", e);
            return null;
        }
    }

    @NonNull
    private String toHex(@NonNull final byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
