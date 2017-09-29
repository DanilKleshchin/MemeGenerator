package com.kleshchin.danil.memegenerator.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.SparseArray;
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
import android.widget.Toast;

import com.kleshchin.danil.memegenerator.MemeDoneDialog;
import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.models.Meme;
import com.madrapps.pikolo.HSLColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 14.09.2017.
 */
public class MemeRedactorActivity extends AppCompatActivity {
    private static final String KEY_MEME = "Meme";
    private static final String KEY_MEME_PATH = "MemePath";
    private static final String ACTION_PHOTO_IMAGE = "PhotoImage";
    private static final String ACTION_URL_IMAGE = "UrlImage";

    private ImageView memeIcon_;
    @Nullable
    private Meme meme_;
    private List<Pair<EditText, TextView>> addedTextsToMeme_ = new ArrayList<>();
    private SparseArray<EditText> dragPair_ = new SparseArray<>();
    private static int currentTextColor_ = Color.BLACK;
    private TextView colorShower_;
    private ViewGroup rootView_;
    private ViewGroup colorPickerRootView_;
    private ViewGroup memeIconRootView_;
    private int dragTextXDelta_;
    private int dragTextYDelta_;
    private int editTextXDelta_;
    private int editTextYDelta_;

    @NonNull
    static Intent newIntent(@NonNull Context context, @NonNull Meme meme) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        intent.putExtra(KEY_MEME, meme);
        intent.setAction(ACTION_URL_IMAGE);
        return intent;
    }

    @NonNull
    static Intent newIntent(@NonNull Context context, @NonNull String filePath) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        intent.putExtra(KEY_MEME_PATH, filePath);
        intent.setAction(ACTION_PHOTO_IMAGE);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_redactor);
        bindViews();
        Intent intent = getIntent();
        String filePath = getIntent().getStringExtra(KEY_MEME_PATH);
        String action = getIntent().getAction();
        switch (action) {
            case Intent.ACTION_VIEW:
                Uri imageUri = intent.getData();
                if (imageUri != null) {
                    Picasso.with(this).load(imageUri).into(memeIcon_);
                    meme_ = new Meme();
                    meme_.name = imageUri.getLastPathSegment();
                }
                break;
            case ACTION_URL_IMAGE:
                meme_ = (Meme) intent.getSerializableExtra(KEY_MEME);
                if (meme_ != null) {
                    Picasso.with(this).load(Uri.parse(meme_.url)).into(memeIcon_);
                }
                break;
            case ACTION_PHOTO_IMAGE:
                Picasso.with(this).load("file://" + filePath).fit().centerCrop()
                        .memoryPolicy(MemoryPolicy.NO_CACHE).into(memeIcon_);
                meme_ = new Meme();
                meme_.name = Uri.parse(filePath).getLastPathSegment();
                break;
            default:
                break;
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
            case R.id.done:
                if (rootView_.getVisibility() == View.GONE) {
                    rootView_.setVisibility(View.VISIBLE);
                    colorPickerRootView_.setVisibility(View.GONE);
                } else {
                    showDoneDialog();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDoneDialog() {
        Bitmap bitmap = createBitmap();
        if (meme_ != null) {
            MemeDoneDialog doneDialog = new MemeDoneDialog(this, bitmap, meme_.name);
            doneDialog.show();
        }
    }

    @NonNull
    private Bitmap createBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(findViewById(R.id.meme_icon_redactor).getWidth(),
                findViewById(R.id.meme_icon_redactor).getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.meme_icon_redactor_root).draw(canvas);
        return bitmap;
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
        HSLColorPicker colorPicker = (HSLColorPicker) findViewById(R.id.color_picker);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view_meme_redactor);
        bottomNavigationView.setOnNavigationItemSelectedListener(new NavigationItemSelectedListener());
        rootView_ = (ViewGroup) findViewById(R.id.root_view);
        memeIconRootView_ = (ViewGroup) findViewById(R.id.meme_icon_redactor_root);
        colorPickerRootView_ = (ViewGroup) findViewById(R.id.color_picker_root);
        memeIcon_ = (ImageView) findViewById(R.id.meme_icon_redactor);
        colorShower_ = (TextView) findViewById(R.id.color_shower);
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                currentTextColor_ = color;
                colorShower_.setTextColor(color);
            }
        });
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
                case R.id.action_choose_text_color:
                    showChooseColorView();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private void showChooseColorView() {
        rootView_.setVisibility(View.GONE);
        colorPickerRootView_.setVisibility(View.VISIBLE);
    }

    private void addTextToMeme() {
        TextView dragText = new TextView(this);
        dragText.setText("%%");
        dragText.setTextColor(Color.BLACK);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 20;
        layoutParams.topMargin = 20;
        dragText.setLayoutParams(layoutParams);
        dragText.setOnTouchListener(new OnViewTouchListener());
        rootView_.addView(dragText);


        EditText editText = new EditText(this);
        editText.setCursorVisible(false);
        editText.setHint(R.string.tap_here);
        editText.setTextColor(currentTextColor_);
        editText.setTextSize(22);
        editText.setAllCaps(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setBackgroundColor(ContextCompat.getColor(this, R.color.color_transparent));
        RelativeLayout.LayoutParams editTextLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextLayoutParams.leftMargin = 40;
        editTextLayoutParams.topMargin = 20;
        editText.setLayoutParams(editTextLayoutParams);

        addedTextsToMeme_.add(new Pair<>(editText, dragText));
        memeIconRootView_.addView(editText);
        dragPair_.put(dragText.hashCode(), editText);
    }

    private void removeTextFromMeme() {
        if (!addedTextsToMeme_.isEmpty()) {
            Pair<EditText, TextView> lastAddedText = addedTextsToMeme_.get(addedTextsToMeme_.size() - 1);
            memeIconRootView_.removeView(lastAddedText.first);
            rootView_.removeView(lastAddedText.second);
            addedTextsToMeme_.remove(lastAddedText);
        }
    }

    private class OnViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();


            RelativeLayout.LayoutParams dragTextParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            TextView dragText = (TextView) view;
            EditText editText = dragPair_.get(dragText.hashCode());
            if (editText == null) {
                return true;
            }
            RelativeLayout.LayoutParams editTextParams = (RelativeLayout.LayoutParams) editText.getLayoutParams();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    dragTextXDelta_ = X - dragTextParams.leftMargin;
                    dragTextYDelta_ = Y - dragTextParams.topMargin;
                    editTextXDelta_ = X - editTextParams.leftMargin;
                    editTextYDelta_ = Y - editTextParams.topMargin;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    dragTextParams.leftMargin = X - dragTextXDelta_;
                    dragTextParams.topMargin = Y - dragTextYDelta_;
                    view.setLayoutParams(dragTextParams);
                    editTextParams.leftMargin = X - editTextXDelta_;
                    editTextParams.topMargin = Y - editTextYDelta_;
                    editText.setLayoutParams(editTextParams);
                    rootView_.invalidate();
                    return true;
                default:
                    return false;
            }
        }
    }
}
