package com.kleshchin.danil.memegenerator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.kleshchin.danil.memegenerator.models.Meme;
import com.kleshchin.danil.memegenerator.utilities.DBHelper;
import com.kleshchin.danil.memegenerator.utilities.ModelHelper;

import java.util.List;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class MemeRepository implements LoaderManager.LoaderCallbacks<Cursor>,
        MemeApiInterface.OnLoadMemeInformationListener {

    public static final String CONNECTION_ERROR = "CONNECTION ERROR";

    @Nullable
    private OnMemeListReceiveListener memeListener_;

    private MemeRepository() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MemeContentProvider.TABLE_MEME_CODE:
                return new CursorLoader(MemeApplication.getInstance(), MemeContentProvider.createUrlForTable(DBHelper.MEME_TABLE),
                        null, null, null, DBHelper.KEY_ID);
            default:
                throw new IllegalArgumentException("no id handed");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case MemeContentProvider.TABLE_MEME_CODE:
                if (memeListener_ != null) {
                    memeListener_.onMemeListReceive(ModelHelper.createMemeFromCursor(data));
                }
                break;
            default:
                throw new IllegalArgumentException("no id handed");
        }
    }

    @Override
    public void onReceiveMemeInformation(@Nullable List<Meme> memes) {
        if (memes == null) {
            return;
        }
        if (memeListener_ != null) {
            memeListener_.onMemeListReceive(memes);
        }
        ContentResolver resolver = MemeApplication.getInstance().getContentResolver();
        resolver.bulkInsert(MemeContentProvider.createUrlForTable(DBHelper.MEME_TABLE),
                DBHelper.createMemeContentValues(memes));
    }

    @Override
    public void onReceiveError(@NonNull String message) {
        if (memeListener_ != null) {
            memeListener_.onErrorReceive(message);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setMemeListener(@NonNull Context context, @NonNull LoaderManager loaderManager, @NonNull OnMemeListReceiveListener listener) {
        memeListener_ = listener;
        if (checkNetworkAvailable(context)) {
            MemeApiInterface apiInterface = new MemeApiInterface(context);
            apiInterface.loadMemeData(loaderManager, this);
            return;
        }
        loaderManager.initLoader(MemeContentProvider.TABLE_MEME_CODE, null, this);
    }

    public void setMemeListenerOnRefresh(@NonNull Context context, @NonNull LoaderManager loaderManager, @NonNull OnMemeListReceiveListener listener) {
        memeListener_ = listener;
        if (checkNetworkAvailable(context)) {
            MemeApiInterface apiInterface = new MemeApiInterface(context);
            apiInterface.loadMemeDataOnRefresh(loaderManager, this);
            return;
        }
        loaderManager.initLoader(MemeContentProvider.TABLE_MEME_CODE, null, this);
    }

    private boolean checkNetworkAvailable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null &&
                networkInfo.isAvailable() &&
                networkInfo.isConnected();
    }

    public void releaseOnMemeListReceiveListener() {
        memeListener_ = null;
    }

    public static MemeRepository getInstance() {
        return MemeRepositoryHolder.instance;
    }

    private static class MemeRepositoryHolder {
        private static final MemeRepository instance = new MemeRepository();
    }

    public interface OnMemeListReceiveListener {
        void onMemeListReceive(@Nullable List<Meme> memes);
        void onErrorReceive(@NonNull String message);
    }
}
