package com.kleshchin.danil.memegenerator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.kleshchin.danil.memegenerator.MemeRepository;
import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.adapters.MemeAdapter;
import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MemeListFromApiActivity extends AppCompatActivity implements
        MemeRepository.OnMemeListReceiveListener,
        MemeAdapter.OnRecyclerViewItemClickListener {

    private List<Meme> memes_ = new ArrayList<>();
    private MemeAdapter adapter_ = new MemeAdapter(this, memes_);
    private SwipeRefreshLayout refreshLayout_;
    private SearchView searchView_;
    private RecyclerView recyclerView_;
    TextView toolbarTitle_;
    private static boolean isLinearLayoutRecyclerView_ = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_list_api);
        Stetho.initializeWithDefaults(this);
        bindViews();
        LoaderManager loaderManager = getSupportLoaderManager();
        MemeRepository repository = MemeRepository.getInstance();
        repository.setOnMemeListReceiveListener(this);
        repository.startLoading(this, loaderManager);
        repository.startService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meme_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_layout:
                changeRecyclerViewLayout(item);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MemeRepository.getInstance().stopService();
    }

    @Override
    public void onMemeListReceive(@Nullable List<Meme> memes) {
        refreshLayout_.setRefreshing(false);
        if (memes != null) {
            memes_ = memes;
            sortListMemes(memes_);
            adapter_.setMemes(memes_);
            adapter_.notifyDataSetChanged();
        }
        MemeRepository.getInstance().releaseOnMemeListReceiveListener();
    }

    @Override
    public void onErrorReceive(@NonNull String message) {
        refreshLayout_.setRefreshing(false);
        if (message.equals(MemeRepository.CONNECTION_ERROR)) {
            message = getResources().getString(R.string.connection_error);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        MemeRepository.getInstance().releaseOnMemeListReceiveListener();
    }

    @Override
    public void onRecyclerViewItemClick(@Nullable Meme meme) {
        if (meme != null) {
            Intent intent = MemeRedactorActivity.newIntent(this, meme);
            startActivity(intent);
        }
    }

    private void sortListMemes(@NonNull List<Meme> memes) {
        Collections.sort(memes, new Comparator<Meme>() {
            @Override
            public int compare(Meme o1, Meme o2) {
                if (o2.id > o1.id) {
                    return -1;
                } else if (o2.id == o1.id) {
                    return 0;
                }
                return 1;
            }
        });
    }

    private void changeRecyclerViewLayout(@NonNull MenuItem item) {
        int scrollPosition = 0;
        if (recyclerView_.getLayoutManager() != null) {
            if (isLinearLayoutRecyclerView_) {
                scrollPosition = ((LinearLayoutManager) recyclerView_.getLayoutManager())
                        .findFirstVisibleItemPosition();
            } else {
                scrollPosition = ((GridLayoutManager) recyclerView_.getLayoutManager())
                        .findFirstVisibleItemPosition();
            }
        }
        int gridLayoutSpanCount = 2;
        RecyclerView.LayoutManager layoutManager = isLinearLayoutRecyclerView_ ?
                new GridLayoutManager(this, gridLayoutSpanCount) :
                new LinearLayoutManager(this);
        item.setIcon(isLinearLayoutRecyclerView_ ?
                ContextCompat.getDrawable(this, R.mipmap.ic_view_list) :
                ContextCompat.getDrawable(this, R.mipmap.ic_view_module));
        recyclerView_.setLayoutManager(layoutManager);
        recyclerView_.scrollToPosition(scrollPosition);
        isLinearLayoutRecyclerView_ = !isLinearLayoutRecyclerView_;
    }

    private void bindViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.searching_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        refreshLayout_ = (SwipeRefreshLayout) findViewById(R.id.meme_swipe_refresh);
        refreshLayout_.setRefreshing(true);
        refreshLayout_.setOnRefreshListener(new OnSwipeRefreshListener());
        recyclerView_ = (RecyclerView) findViewById(R.id.meme_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView_.getContext(),
                layoutManager.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_recycler_view));
        recyclerView_.setLayoutManager(layoutManager);
        recyclerView_.addItemDecoration(divider);
        adapter_.setItemClickListener(this);
        recyclerView_.setAdapter(adapter_);
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
            MemeRepository repository = MemeRepository.getInstance();
            repository.setOnMemeListReceiveListener(MemeListFromApiActivity.this);
            repository.refreshLoading(MemeListFromApiActivity.this, loaderManager);
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
            adapter_.searchByQuery(newText);
            return true;
        }
    }
}
