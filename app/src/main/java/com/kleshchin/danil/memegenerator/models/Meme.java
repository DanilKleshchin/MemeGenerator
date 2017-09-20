package com.kleshchin.danil.memegenerator.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class Meme implements Serializable {
    public String name;
    public long id;
    public String url;
    public int width;
    public int height;

    public Meme() {

    }

    public Meme(long id, int width, int height, @NonNull String name, @NonNull String url) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.url = url;
    }
}
