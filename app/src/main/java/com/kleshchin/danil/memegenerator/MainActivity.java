package com.kleshchin.danil.memegenerator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemeRepository.OnMemeListReceiveListener {

    private List<Meme> memes_ = new ArrayList<>();
    private MemeAdapter adapter_ = new MemeAdapter(this, memes_);
    private SwipeRefreshLayout refreshLayout_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        LoaderManager loaderManager = getSupportLoaderManager();
        MemeRepository.getInstance().setMemeListener(this, loaderManager, this);
    }

    @Override
    public void onMemeListReceive(@Nullable List<Meme> memes) {
        refreshLayout_.setRefreshing(false);
        if (memes != null) {
            memes_ = memes;
            adapter_.setMemes(memes_);
            adapter_.notifyDataSetChanged();
        }
    }

    @Override
    public void onErrorReceive(@NonNull String message) {
        refreshLayout_.setRefreshing(false);
        if (message.equals(MemeRepository.CONNECTION_ERROR)) {
            message = getResources().getString(R.string.connection_error);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindViews() {
        refreshLayout_ = (SwipeRefreshLayout) findViewById(R.id.meme_swipe_refresh);
        refreshLayout_.setRefreshing(true);
        refreshLayout_.setOnRefreshListener(new OnSwipeRefreshListener());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.meme_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_recycler_view));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(divider);
        recyclerView.setAdapter(adapter_);
    }

    private class OnSwipeRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refreshLayout_.setRefreshing(true);
            LoaderManager loaderManager = getSupportLoaderManager();
            MemeRepository.getInstance()
                    .setOnRefreshMemeListener(MainActivity.this, loaderManager, MainActivity.this);
        }
    }
}
