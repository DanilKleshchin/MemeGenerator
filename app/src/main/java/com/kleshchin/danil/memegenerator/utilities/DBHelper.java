package com.kleshchin.danil.memegenerator.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.List;

/**
 * Created by Danil Kleshchin on 13.09.2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MemeInfo";

    public static final String MEME_TABLE = "Meme";
    public static final String KEY_ID = "_id";
    static final String KEY_MEME_NAME = "Name";
    static final String KEY_ICON_URL = "Icon_Url";
    static final String KEY_WIDTH = "Icon_Width";
    static final String KEY_HEIGHT = "Icon_Height";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String BRACKET_RIGHT_SEP = ")";
    private static final String BRACKET_LEFT_SEP = "(";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MEME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_CLUB_TABLE);
    }

    private static final String CREATE_MEME_TABLE = "" +
            "CREATE TABLE " + MEME_TABLE + BRACKET_LEFT_SEP +
            KEY_ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
            KEY_MEME_NAME + TEXT_TYPE + COMMA_SEP +
            KEY_ICON_URL + TEXT_TYPE + COMMA_SEP +
            KEY_WIDTH + INT_TYPE + COMMA_SEP +
            KEY_HEIGHT + INT_TYPE + BRACKET_RIGHT_SEP;

    private static final String DROP_CLUB_TABLE = "DROP TABLE IF EXISTS " + MEME_TABLE;

    @NonNull
    public static ContentValues[] createMemeContentValues(@NonNull List<Meme> memes) {
        ContentValues[] contentValuesArray = new ContentValues[memes.size()];
        for (int i = 0; i < memes.size(); ++i) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_ID, memes.get(i).id);
            contentValues.put(DBHelper.KEY_MEME_NAME, memes.get(i).name);
            contentValues.put(DBHelper.KEY_ICON_URL, memes.get(i).url);
            contentValues.put(DBHelper.KEY_WIDTH, memes.get(i).width);
            contentValues.put(DBHelper.KEY_HEIGHT, memes.get(i).height);
            contentValuesArray[i] = contentValues;
        }
        return contentValuesArray;
    }

}
