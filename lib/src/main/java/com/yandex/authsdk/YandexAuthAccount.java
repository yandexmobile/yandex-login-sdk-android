package com.yandex.authsdk;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class YandexAuthAccount {

    private final long uid;

    @NonNull
    private final String primaryDisplayName;

    @Nullable
    private final String secondaryDisplayName;

    private final boolean isAvatarEmpty;

    @Nullable
    private final String avatarUrl;

    public YandexAuthAccount(
            final long uid,
            @NonNull final String primaryDisplayName,
            @Nullable final String secondaryDisplayName,
            final boolean isAvatarEmpty,
            @Nullable final String avatarUrl
    ) {
        this.uid = uid;
        this.primaryDisplayName = primaryDisplayName;
        this.secondaryDisplayName = secondaryDisplayName;
        this.isAvatarEmpty = isAvatarEmpty;
        this.avatarUrl = avatarUrl;
    }

    public long getUid() {
        return uid;
    }

    @NonNull
    public String getPrimaryDisplayName() {
        return primaryDisplayName;
    }

    @Nullable
    public String getSecondaryDisplayName() {
        return secondaryDisplayName;
    }

    public boolean isAvatarEmpty() {
        return isAvatarEmpty;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final YandexAuthAccount that = (YandexAuthAccount) o;

        if (uid != that.uid) {
            return false;
        }
        if (isAvatarEmpty != that.isAvatarEmpty) {
            return false;
        }
        if (!primaryDisplayName.equals(that.primaryDisplayName)) {
            return false;
        }
        if (secondaryDisplayName != null ? !secondaryDisplayName.equals(that.secondaryDisplayName) : that.secondaryDisplayName != null) {
            return false;
        }
        return avatarUrl != null ? avatarUrl.equals(that.avatarUrl) : that.avatarUrl == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (uid ^ (uid >>> 32));
        result = 31 * result + primaryDisplayName.hashCode();
        result = 31 * result + (secondaryDisplayName != null ? secondaryDisplayName.hashCode() : 0);
        result = 31 * result + (isAvatarEmpty ? 1 : 0);
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        return result;
    }

}
