package com.kleshchin.danil.memegenerator;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kleshchin.danil.memegenerator.utilities.DBHelper;

/**
 * Created by Danil Kleshchin on 13.09.2017.
 */

public class MemeContentProvider extends ContentProvider {

    private static DBHelper dbHelper_;
    private static SQLiteDatabase database_;
    private static final UriMatcher URI_MATCHER;

    public final static int TABLE_MEME_CODE = 0;
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".db";

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, DBHelper.MEME_TABLE, TABLE_MEME_CODE);
    }

    public static Uri createUrlForTable(@NonNull String table) {
        return Uri.parse("content://" + AUTHORITY + "/" + table);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            dbHelper_ = new DBHelper(context);
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        database_ = dbHelper_.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case TABLE_MEME_CODE:
                builder.setTables(DBHelper.MEME_TABLE);
                break;
            default:
                throw new IllegalArgumentException("url not recognized!");
        }
        cursor = builder.query(database_, projection, selection, selectionArgs, null, null, sortOrder, null);
        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".item";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = "";
        switch (URI_MATCHER.match(uri)) {
            case TABLE_MEME_CODE:
                table = DBHelper.MEME_TABLE;
                break;
            default:
                throw new IllegalArgumentException("url not recognized!");
        }
        long result = dbHelper_.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return ContentUris.withAppendedId(uri, result);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        database_ = dbHelper_.getWritableDatabase();
        String table = "";
        switch (URI_MATCHER.match(uri)) {
            case TABLE_MEME_CODE:
                table = DBHelper.MEME_TABLE;
                break;
            default:
                throw new IllegalArgumentException("url not recognized!");
        }
        return database_.delete(table, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return -1;
    }
}
