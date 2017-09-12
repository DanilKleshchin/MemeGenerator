package com.kleshchin.danil.memegenerator;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemeRepository.OnMemeListReceiveListener {

    private List<Meme> memes_ = new ArrayList<>();
    private MemeAdapter adapter_ = new MemeAdapter(this, memes_);
    private SwipeRefreshLayout refreshLayout_;
    private SearchView searchView_;
    TextView toolbarTitle_;
    @Nullable
    private Handler handler_ = new Handler();

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
        toolbarTitle_ = (TextView) findViewById(R.id.toolbar_search_title);
        searchView_ = (SearchView) findViewById(R.id.search_view_meme);
        searchView_.setOnQueryTextListener(new OnQueryTextSearchListener());
        searchView_.setOnSearchClickListener(new OnSearchViewClickListener());
        searchView_.setOnCloseListener(new OnCloseSearchViewListener());
        EditText searchEditText = (EditText)
                searchView_.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    private class OnCloseSearchViewListener implements SearchView.OnCloseListener {
        @Override
        public boolean onClose() {
            toolbarTitle_.setVisibility(View.VISIBLE);
            String emptyQuery = "";
            adapter_.searchByQuery(emptyQuery);
            return false;
        }
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

    private class OnSearchViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            toolbarTitle_.setVisibility(View.GONE);
        }
    }

    private class OnQueryTextSearchListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView_.clearFocus();
            adapter_.searchByQuery(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(final @NonNull String newText) {
            if (newText.length() > 2) {
                if (handler_ != null) {
                    handler_.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter_.searchByQuery(newText);
                        }
                    }, 2000);
                }
            }
            return true;
        }
    }
}
