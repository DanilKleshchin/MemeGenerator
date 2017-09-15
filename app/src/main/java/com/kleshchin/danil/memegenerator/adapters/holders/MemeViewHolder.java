package com.kleshchin.danil.memegenerator.adapters.holders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.adapters.MemeAdapter;
import com.kleshchin.danil.memegenerator.models.Meme;
import com.squareup.picasso.Picasso;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
public class MemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView memeName_;
    private ImageView memeIcon_;
    @Nullable
    private MemeAdapter.OnRecyclerViewItemClickListener itemClickListener_;
    @Nullable
    private Meme currentMeme_;

    public MemeViewHolder(@NonNull View itemView, @NonNull MemeAdapter.OnRecyclerViewItemClickListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemClickListener_ = listener;
        memeName_ = (TextView) itemView.findViewById(R.id.meme_name);
        memeIcon_ = (ImageView) itemView.findViewById(R.id.meme_icon);
    }

    public void setData(@NonNull Context context, @NonNull Meme meme) {
        currentMeme_ = meme;
        memeName_.setText(meme.name);
        Picasso.with(context)
                .load(meme.url)
                .into(memeIcon_);
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener_ != null) {
            itemClickListener_.onRecyclerViewItemClick(currentMeme_);
        }
    }
}
