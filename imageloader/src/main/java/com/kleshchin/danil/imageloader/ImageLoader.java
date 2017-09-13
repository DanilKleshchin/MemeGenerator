package com.kleshchin.danil.imageloader;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class ImageLoader implements ImageDownloader.OnFileDownloadListener {

    private static final String KEY_CACHE_DIR = "pathToCacheDir";

    private int placeholderId_;
    @Nullable
    private String url_;
    @Nullable
    private ImageView imageView_;
    @Nullable
    private Context context_;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private static LruCache<String, Object> lruCache_;
    private static Map<ImageView, AsyncTask<String, Integer, Void>> downloadAsyncTasks = new HashMap<>();

    private ImageLoader(@NonNull ImageLoaderBuilder builder) {
        if (builder.imageView_ == null || builder.context_ == null) {
            return;
        }
        context_ = builder.context_;
        imageView_ = builder.imageView_;
        placeholderId_ = builder.placeholderId_;
        url_ = builder.url_;
        imageView_.setImageDrawable(ContextCompat.getDrawable(context_, placeholderId_));
        lruCache_ = Cache.getInstance(context_).getLruCache();
        loadImageIntoView();
    }

    @Override
    public void onFileDownload(@Nullable Bitmap bitmap, @Nullable String filePath) {
        if (imageView_ != null && filePath != null && context_ != null) {
            imageView_.setImageDrawable(ContextCompat.getDrawable(context_, placeholderId_));
            if (bitmap != null) {
                if (!isCached(filePath)) {
                    lruCache_.put(filePath, bitmap);
                }
                imageView_.setImageBitmap(bitmap);
                bitmaps.add(bitmap);
            }
        }
    }

    private void loadImageIntoView() {
        if (imageView_ == null || url_ == null || context_ == null) {
            return;
        }
        checkAvailableMemory(lruCache_, 15 * 1024 * 1024);
        AsyncTask<String, Integer, Void> asyncTask = downloadAsyncTasks.get(imageView_);
        if (asyncTask != null) {
            if (asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                asyncTask.cancel(true);
            }
        }
        String filePath = getBasePathToFile(context_) + File.separator + createFileNameFromStringUrl(url_);
        Bitmap bitmap = (Bitmap) lruCache_.get(filePath);
        if (bitmap != null) {
            onFileDownload(bitmap, filePath);
        } else {
            DownloadImage();
        }
    }

    private void DownloadImage() {
        if (imageView_ == null || url_ == null || context_ == null) {
            return;
        }
        final ImageDownloader task = new ImageDownloader(context_);
        task.setListener(this);
        task.setBasePathToDownloadDir(getBasePathToFile(context_));
        downloadAsyncTasks.put(imageView_, task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_);
    }

    private void checkAvailableMemory(LruCache<String, Object> lruCache, int minBytesMemory) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long availableMemory = maxMemory - usedMemory;
        if (availableMemory <= minBytesMemory) {
            /*int size = bitmaps.size() / 2;
            for (int i = 0; i < size; ++i) {
                bitmaps.get(i).recycle();
            }*/
            lruCache.evictAll();
        }
    }

    private boolean isCached(@NonNull String filePath) {
        return context_ != null && lruCache_.get(filePath) != null;
    }

    @NonNull
    static String createFileNameFromStringUrl(@NonNull String strUrl) {
        return strUrl.substring(strUrl.lastIndexOf("/") + 1, strUrl.length());
    }

    @NonNull
    private static String getBasePathToFile(@NonNull Context context) {
        if (lruCache_.get(KEY_CACHE_DIR) == null) {
            lruCache_.put(KEY_CACHE_DIR,
                    new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Icons")
                            .getAbsolutePath());
        }
        return (String) lruCache_.get(KEY_CACHE_DIR);
    }

    public static class ImageLoaderBuilder {
        private int placeholderId_;
        @Nullable
        private String url_;
        @Nullable
        private ImageView imageView_;
        @Nullable
        private Context context_;

        public ImageLoaderBuilder(@NonNull Context context) {
            context_ = context;
        }

        public ImageLoaderBuilder load(@NonNull String url) {
            url_ = url;
            return this;
        }

        public ImageLoaderBuilder into(@NonNull ImageView imageView) {
            imageView_ = imageView;
            return this;
        }

        public ImageLoaderBuilder placeholder(int resourceId) {
            placeholderId_ = resourceId;
            return this;
        }

        @RequiresPermission(allOf = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        })
        public ImageLoader build() {
            return new ImageLoader(this);
        }
    }
}
