package com.kleshchin.danil.memegenerator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kleshchin.danil.memegenerator.models.Meme;
import com.squareup.picasso.Picasso;

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
        Picasso.with(context)
                .load(meme.url)
                .into(memeIcon_);
    }
}
