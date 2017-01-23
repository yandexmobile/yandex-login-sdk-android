package com.yandex.yaloginsdk;

public class YaLoginSdkConstants {

    public static int VERSION = 1; // TODO move to gradle?

    static int LOGIN_REQUEST_CODE = 312; // TODO choose number?

    public static class AmConstants {

        public static final String FINGERPRINT = "5D224274D9377C35DA777AD934C65C8CCA6E7A20";

        public static final String ACTION_YA_SDK_LOGIN = "com.yandex.auth.action.YA_SDK_LOGIN";

        public static final String META_SDK_VERSION = "com.yandex.auth.LOGIN_SDK_VERSION";

        public static final String EXTRA_OAUTH_TOKEN = "com.yandex.auth.EXTRA_OAUTH_TOKEN";

        public static final String EXTRA_OAUTH_TOKEN_TYPE = "com.yandex.auth.EXTRA_OAUTH_TOKEN_TYPE";

        public static final String EXTRA_OAUTH_TOKEN_EXPIRES = "com.yandex.auth.OAUTH_TOKEN_EXPIRES";

        public static final String EXTRA_CLIENT_ID = "com.yandex.auth.CLIENT_ID";

        public static final String EXTRA_SCOPES = "com.yandex.auth.SCOPES";
    }

    static class Extra {

        public static final String TOKEN = "com.yandex.yaloginsdk.TOKEN";

        public static final String ERROR = "com.yandex.yaloginsdk.ERROR";
    }

    static class State {

        static final String STATE_LOGIN_TYPE = "com.yandex.yaloginsdk.STATE_LOGIN_TYPE";
    }
}
