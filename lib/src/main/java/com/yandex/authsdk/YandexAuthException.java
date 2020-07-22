package com.yandex.authsdk;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;

public class YandexAuthException extends Exception {

    public static final String CONNECTION_ERROR = "connection.error";

    public static final String SECURITY_ERROR = "security.error";

    public static final String JWT_AUTHORIZATION_ERROR = "jwt.authorization.error";

    public static final String OAUTH_TOKEN_ERROR = "oauth_token.invalid";

    public static final String UNKNOWN_ERROR = "unknown.error";

    @NonNull
    private final String[] errors;

    public YandexAuthException(@NonNull final String error) {
        this(new String[]{error});
    }

    public YandexAuthException(@NonNull final String[] errors) {
        super(Arrays.toString(errors));
        this.errors = errors;
    }

    public YandexAuthException(@NonNull final IOException e) {
        super(CONNECTION_ERROR, e);
        errors = new String[]{CONNECTION_ERROR};
    }

    @NonNull
    public String[] getErrors() {
        return errors;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final YandexAuthException error = (YandexAuthException) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(errors, error.errors);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(errors);
    }
}
