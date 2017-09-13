package com.kleshchin.danil.memegenerator.utilities;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kleshchin.danil.memegenerator.models.Meme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class ModelHelper {
    @Nullable
    public static List<Meme> createMemeFromJson(@NonNull JSONObject dataJsonObject) throws JSONException {
        if (!dataJsonObject.getBoolean("success")) {
            return null;
        }
        List<Meme> memes = new ArrayList<>();
        JSONObject data = dataJsonObject.getJSONObject("data");
        JSONArray jsonArray = data.getJSONArray("memes");
        for (int i = 0; i < jsonArray.length(); ++i) {
            Meme meme = new Meme();
            meme.id = jsonArray.getJSONObject(i).getInt("id");
            meme.name = jsonArray.getJSONObject(i).getString("name");
            meme.url = jsonArray.getJSONObject(i).getString("url");
            meme.width = jsonArray.getJSONObject(i).getInt("width");
            meme.height = jsonArray.getJSONObject(i).getInt("height");
            memes.add(meme);
        }
        return memes;
    }

    @Nullable
    public static List<Meme> createMemeFromCursor(@NonNull Cursor data) {
        List<Meme> memes = new ArrayList<>();
        data.moveToFirst();
        while (data.moveToNext()) {
            Meme meme = new Meme();
            meme.id = data.getInt(data.getColumnIndex(DBHelper.KEY_ID));
            meme.name = data.getString(data.getColumnIndex(DBHelper.KEY_MEME_NAME));
            meme.width = data.getInt(data.getColumnIndex(DBHelper.KEY_WIDTH));
            meme.height = data.getInt(data.getColumnIndex(DBHelper.KEY_HEIGHT));
            meme.url = data.getString(data.getColumnIndex(DBHelper.KEY_ICON_URL));
            memes.add(meme);
        }
        return memes;
    }
}
