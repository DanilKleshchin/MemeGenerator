package com.kleshchin.danil.memegenerator.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.adapters.holders.MemeViewHolder;
import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 08.09.2017.
 */
public class MemeAdapter extends RecyclerView.Adapter<MemeViewHolder> {
    @Nullable
    private static OnRecyclerViewItemClickListener itemClickListener_;
    @NonNull
    private List<Meme> memes_ = new ArrayList<>();
    @NonNull
    private List<Meme> memesCopy_;
    @NonNull
    private Context context_;

    public MemeAdapter(@NonNull Context context, @NonNull List<Meme> memes) {
        context_ = context;
        memesCopy_ = new ArrayList<>(memes);
        memes_.clear();
        memes_.addAll(memes);
    }

    @Override
    public MemeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_meme_item, parent, false);
        if (itemClickListener_ != null) {
            return new MemeViewHolder(v, itemClickListener_);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MemeViewHolder holder, int position) {
        holder.setData(context_, memes_.get(position));
    }

    public void setItemClickListener(@NonNull OnRecyclerViewItemClickListener listener) {
        itemClickListener_ = listener;
    }

    @Override
    public int getItemCount() {
        return memes_.size();
    }

    public void setMemes(@NonNull List<Meme> memes) {
        memesCopy_.clear();
        memes_.clear();
        memesCopy_.addAll(memes);
        memes_.addAll(memes);
    }



    public void searchByQuery(@NonNull String query) {
        memes_.clear();
        if (query.isEmpty()) {
            memes_.addAll(memesCopy_);
        }
        query = query.toLowerCase();
        for (Meme meme : memesCopy_) {
            if (meme.name.toLowerCase().contains(query)) {
                memes_.add(meme);
            }
        }
        notifyDataSetChanged();
    }

    public interface OnRecyclerViewItemClickListener {
        void onRecyclerViewItemClick(@Nullable Meme meme);
    }
}
