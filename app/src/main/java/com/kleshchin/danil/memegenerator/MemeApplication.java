package com.kleshchin.danil.memegenerator;

import android.app.Application;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class MemeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        instance_ = this;
    }

    private static MemeApplication instance_;

    public static MemeApplication getInstance() {
        return instance_;
    }
}
