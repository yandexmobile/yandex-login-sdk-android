package com.yandex.yaloginsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FingerprintExtractor {

    @Nullable
    public String[] getCertificateFingerprint(@NonNull Context context, @NonNull String packageName) {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            final PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            assert info.signatures != null;
            final String[] result = new String[info.signatures.length];
            int i = 0;
            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                result[i++] = toHex(md.digest());
            }
            return result;
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Logger.e(FingerprintExtractor.class.getCanonicalName(), "Error getting fingerprint", e);
            return null;
        }
    }

    @NonNull
    private static String toHex(@NonNull byte[] bytes) {
        final BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
