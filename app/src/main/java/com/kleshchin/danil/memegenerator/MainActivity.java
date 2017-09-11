package com.kleshchin.danil.memegenerator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView_;
    private List<Meme> memes_;
    private MemeAdapter adapter_ = new MemeAdapter(this, memes_);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView_ = (RecyclerView) findViewById(R.id.meme_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_.setLayoutManager(layoutManager);

    }
}
