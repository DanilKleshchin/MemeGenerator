package com.kleshchin.danil.memegenerator.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class InformationLoader extends AsyncTaskLoader<String> {
    @NonNull
    private String url_;

    public InformationLoader(@NonNull Context context, @NonNull String url) {
        super(context);
        url_ = url;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public String loadData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url_)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String loadInBackground() {
        return loadData();
    }
}