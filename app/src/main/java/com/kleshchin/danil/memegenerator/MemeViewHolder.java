package com.kleshchin.danil.memegenerator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kleshchin.danil.imageloader.ImageLoader;
import com.kleshchin.danil.memegenerator.models.Meme;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
class MemeViewHolder extends RecyclerView.ViewHolder {

    private TextView memeName_;
    private ImageView memeIcon_;

    MemeViewHolder(View itemView) {
        super(itemView);
        memeName_ = (TextView) itemView.findViewById(R.id.meme_name);
        memeIcon_ = (ImageView) itemView.findViewById(R.id.meme_icon);
    }

    void setData(@NonNull Context context, @NonNull Meme meme) {
        memeName_.setText(meme.name);
        ImageLoader.ImageLoaderBuilder builder = new ImageLoader.ImageLoaderBuilder(context);
        if (isPermissionGrated(context)) {
            return;
        }

        builder.load(meme.url)
                .into(memeIcon_)
                .placeholder(R.mipmap.ic_meme_placeholder)
                .build();
    }

    private boolean isPermissionGrated(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
    }
}
