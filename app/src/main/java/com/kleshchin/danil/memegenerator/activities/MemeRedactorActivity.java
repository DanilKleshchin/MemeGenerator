package com.kleshchin.danil.memegenerator.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.models.Meme;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 14.09.2017.
 */
public class MemeRedactorActivity extends AppCompatActivity {
    public static final String KEY_ICON_URL = "Icon_url";

    private ImageView memeIcon_;
    @Nullable
    private Meme meme_;
    private FrameLayout main;
    private int id = 0;
    private List<EditText> editTexts = new ArrayList<EditText>();

    static Intent newIntent(@NonNull Context context, @NonNull Meme meme) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        intent.putExtra(KEY_ICON_URL, meme);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_redactor);
        //main = (FrameLayout) findViewById(R.id.linear_layout);
        meme_ = (Meme) getIntent().getSerializableExtra(KEY_ICON_URL);
        bindViews();
        if (meme_ != null) {
            Picasso.with(this).load(meme_.url).into(memeIcon_);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meme_redactor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                shareMeme();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void shareMeme() {
        if (meme_ == null) {
            return;
        }
        Picasso.with(getApplicationContext()).load(meme_.url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                startActivity(Intent.createChooser(i, getString(R.string.share_image)));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
        /*LinearLayout editTextLayout = new LinearLayout(this);
        editTextLayout.setOrientation(LinearLayout.VERTICAL);
        main.addView(editTextLayout);

        EditText editText1 = new EditText(this);
        editText1.setId(id++);
        editTextLayout.addView(editText1);

        editTexts.add(editText1);

        EditText editText2 = new EditText(this);
        editText2.setId(id++);
        editTextLayout.addView(editText2);

        editTexts.add(editText2);*/

    }

    @Nullable
    public Uri getLocalBitmapUri(@NonNull Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void bindViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_meme_redactor);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        if (meme_ != null) {
            ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(meme_.name);
        }
        memeIcon_ = (ImageView) findViewById(R.id.image_meme_redactor);
    }
}
