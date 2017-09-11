package com.kleshchin.danil.imageloader;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.LruCache;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
class Cache {
    private static Cache instance_;
    private static LruCache<String, Object> lruCache_;

    private Cache(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int maxKb = activityManager.getMemoryClass() * 1024;
        int limitKb = maxKb / 8;
        lruCache_ = new LruCache<>(limitKb);
    }

    static Cache getInstance(@NonNull Context context) {
        if (instance_ == null) {
            instance_ = new Cache(context);
        }
        return instance_;
    }

    LruCache<String, Object> getLruCache() {
        return lruCache_;
    }
}
