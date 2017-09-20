package com.kleshchin.danil.memegenerator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.kleshchin.danil.memegenerator.models.Meme;
import com.kleshchin.danil.memegenerator.utilities.InformationLoader;
import com.kleshchin.danil.memegenerator.utilities.ModelHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.kleshchin.danil.memegenerator.MemeRepository.CONNECTION_ERROR;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
class MemeApiInterface implements LoaderManager.LoaderCallbacks {


    private static final String KEY_URL = "URL";
    private final static int KEY_MEME = 6;

    @Nullable
    private OnLoadMemeInformationListener loadMemeInformationListener_;
    @Nullable
    private OnLoadMemeDataAsyncTaskListener asyncTaskListener_;
    @NonNull
    private Context context_;

    MemeApiInterface(@NonNull Context context) {
        context_ = context;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String url = args.getString(KEY_URL);
        if (url != null) {
            return new InformationLoader(context_, url);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case KEY_MEME:
                onLoadMemeInformation((String) data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    void loadMemeData(@NonNull LoaderManager loaderManager,
                      @Nullable OnLoadMemeInformationListener listener) {
        if (listener != null) {
            loadMemeInformationListener_ = listener;
            loaderManager.initLoader(KEY_MEME, setArgs("https://api.imgflip.com/get_memes"), this);
        }
    }

    void loadMemeDataOnRefresh(@NonNull LoaderManager loaderManager,
                               @NonNull OnLoadMemeInformationListener listener) {
        loadMemeInformationListener_ = listener;
        loaderManager.restartLoader(KEY_MEME, setArgs("https://api.imgflip.com/get_memes"), this);
    }

    void loadMemeAsyncTask(@NonNull OnLoadMemeDataAsyncTaskListener listener) {
        asyncTaskListener_ = listener;
        MemeServiceLoader memeServiceLoader = new MemeServiceLoader();
        memeServiceLoader.execute("https://api.imgflip.com/get_memes");
    }

    @NonNull
    private Bundle setArgs(@NonNull String strUrl) {
        Bundle args = new Bundle();
        args.putString(KEY_URL, strUrl);
        return args;
    }

    private void onLoadMemeInformation(@Nullable String strJson) {
        if (loadMemeInformationListener_ == null) {
            return;
        }
        if (strJson == null) {
            loadMemeInformationListener_.onReceiveError(CONNECTION_ERROR);
            return;
        }
        try {
            JSONObject dataJsonObject = new JSONObject(strJson);
            loadMemeInformationListener_.onReceiveMemeInformation(ModelHelper.createMemeFromJson(dataJsonObject));
        } catch (JSONException e) {
            loadMemeInformationListener_.onReceiveError(e.getMessage());
        }
    }

    private void onLoadMemeInformationAsyncTask(@Nullable String strJson) {
        if (asyncTaskListener_ == null) {
            return;
        }
        if (strJson == null) {
            asyncTaskListener_.onLoadMemeErrorAsyncTask(CONNECTION_ERROR);
            return;
        }
        try {
            JSONObject dataJsonObject = new JSONObject(strJson);
            asyncTaskListener_.onLoadMemeAsyncTask(ModelHelper.createMemeFromJson(dataJsonObject));
        } catch (JSONException e) {
            asyncTaskListener_.onLoadMemeErrorAsyncTask(e.getMessage());
        }
    }

    private class MemeServiceLoader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            InformationLoader loader = new InformationLoader(context_, params[0]);
            return loader.loadData();
        }

        @Override
        protected void onPostExecute(@Nullable String s) {
            super.onPostExecute(s);
            onLoadMemeInformationAsyncTask(s);
        }
    }

    interface OnLoadMemeInformationListener {
        void onReceiveMemeInformation(@Nullable List<Meme> memes);
        void onReceiveError(@NonNull String message);
    }

    interface OnLoadMemeDataAsyncTaskListener {
        void onLoadMemeAsyncTask(@Nullable List<Meme> memes);
        void onLoadMemeErrorAsyncTask(@NonNull String message);
    }
}
