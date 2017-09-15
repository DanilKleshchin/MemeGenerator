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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private BottomNavigationView bottomNavigationView;
    private List<EditText> addedTextsToMeme_ = new ArrayList<>();

    private ViewGroup rootView_;
    private int xDelta_;
    private int yDelta_;

    static Intent newIntent(@NonNull Context context, @NonNull Meme meme) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        intent.putExtra(KEY_ICON_URL, meme);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_redactor);
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
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view_meme_redactor);
        bottomNavigationView.setOnNavigationItemSelectedListener(new NavigationItemSelectedListener());
        rootView_ = (ViewGroup) findViewById(R.id.root);
        memeIcon_ = (ImageView) findViewById(R.id.image_meme_redactor);
    }

    private class NavigationItemSelectedListener implements
            BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_add_text:
                    addTextToMeme();
                    break;
                case R.id.action_clear_last_text:
                    removeTextFromMeme();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private void addTextToMeme() {
        EditText editText = new EditText(this);
        editText.setHint(R.string.tap_here);
        editText.setTextSize(22);
        editText.setAllCaps(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setBackgroundColor(ContextCompat.getColor(this, R.color.color_transparent));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 50;
        layoutParams.topMargin = 50;
        editText.setLayoutParams(layoutParams);
        editText.setOnTouchListener(new OnViewTouchListener());
        addedTextsToMeme_.add(editText);
        rootView_.addView(editText);
    }

    private void removeTextFromMeme() {
        if (!addedTextsToMeme_.isEmpty()) {
            EditText lastAddedText = addedTextsToMeme_.get(addedTextsToMeme_.size() - 1);
            rootView_.removeView(lastAddedText);
            addedTextsToMeme_.remove(lastAddedText);
        }
    }

    private class OnViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    xDelta_ = X - lParams.leftMargin;
                    yDelta_ = Y - lParams.topMargin;
                    return false;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = X - xDelta_;
                    layoutParams.topMargin = Y - yDelta_;
                    view.setLayoutParams(layoutParams);
                    rootView_.invalidate();
                    return true;
                default:
                    return false;
            }
        }
    }
}
