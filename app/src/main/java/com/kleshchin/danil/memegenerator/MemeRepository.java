package com.kleshchin.danil.memegenerator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.List;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
class MemeRepository implements LoaderManager.LoaderCallbacks<Cursor>,
        MemeApiInterface.OnLoadMemeInformationListener {

    static final String CONNECTION_ERROR = "CONNECTION ERROR";

    private OnMemeListReceiveListener listener_;

    private MemeRepository() {

    }

    @Override
    public void onLoadMemeInformation(@Nullable List<Meme> memes) {
        listener_.onMemeListReceive(memes);
    }

    @Override
    public void onLoadError(@NonNull String message) {
        listener_.onErrorReceive(message);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void setMemeListener(@NonNull Context context, @NonNull LoaderManager loaderManager, @NonNull OnMemeListReceiveListener listener) {
        listener_ = listener;
        MemeApiInterface apiInterface = new MemeApiInterface(context);
        apiInterface.loadMemeData(loaderManager, this);
    }

    void setOnRefreshMemeListener(@NonNull Context context, @NonNull LoaderManager loaderManager, @NonNull OnMemeListReceiveListener listener) {
        //loaderManager.restartLoader();
        listener_ = listener;
        MemeApiInterface apiInterface = new MemeApiInterface(context);
        apiInterface.loadMemeOnRefresh(loaderManager, this);
    }

    static MemeRepository getInstance() {
        return MemeRepositoryHolder.instance;
    }

    private static class MemeRepositoryHolder {
        private static final MemeRepository instance = new MemeRepository();
    }

    interface OnMemeListReceiveListener {
        void onMemeListReceive(@Nullable List<Meme> memes);
        void onErrorReceive(@NonNull String message);
    }
}
