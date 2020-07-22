package com.yandex.authsdk.internal;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class HostUtil {

    private static final Map<String, String> LANGUAGE_TO_TLD = new HashMap<>();

    static {
        LANGUAGE_TO_TLD.put("be", "by"); // Belarusian
        LANGUAGE_TO_TLD.put("tr", "com.tr"); // Turkish
        LANGUAGE_TO_TLD.put("kk", "kz"); // Kazakh
        LANGUAGE_TO_TLD.put("et", "ru"); // Estonian
        LANGUAGE_TO_TLD.put("hy", "ru"); // Armenian
        LANGUAGE_TO_TLD.put("ka", "ru"); // Georgian
        LANGUAGE_TO_TLD.put("ru", "ru"); // Russian
        LANGUAGE_TO_TLD.put("uk", "ua"); // Ukrainian
    }

    private static final String DEFAULT_TLD = "com";

    /**
     * @param baseHost - host ended with ".ru"
     */
    static String getLocalizedHost(@NonNull final String baseHost, @NonNull final Locale locale) {
        String tld = LANGUAGE_TO_TLD.get(locale.getLanguage());
        if (tld == null) {
            tld = DEFAULT_TLD;
        }

        return baseHost.replaceAll("ru$", tld);
    }
}
