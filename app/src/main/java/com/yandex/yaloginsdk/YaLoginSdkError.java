package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;

public class YaLoginSdkError extends RuntimeException {

    public static final String CONNECTION_ERROR = "connection.error";

    public static final String SECURITY_ERROR = "security.error";

    public static final String JWT_AUTHORIZATION_ERROR = "jwt.authorization.error";

    public static final String IO_ERROR = "io.error";

    @NonNull
    private final String[] errors;

    public YaLoginSdkError(@NonNull String error) {
        this(new String[]{error});
    }

    public YaLoginSdkError(@NonNull String[] errors) {
        super(Arrays.toString(errors));
        this.errors = errors;
    }

    public YaLoginSdkError(@NonNull IOException e) {
        super(IO_ERROR, e);
        errors = new String[]{IO_ERROR};
    }

    @NonNull
    public String[] getErrors() {
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final YaLoginSdkError error = (YaLoginSdkError) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(errors, error.errors);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(errors);
    }
}
