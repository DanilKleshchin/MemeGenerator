package com.kleshchin.danil.memegenerator.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kleshchin.danil.memegenerator.MemeDoneDialog;
import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.models.Meme;
import com.madrapps.pikolo.HSLColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 14.09.2017.
 */
public class MemeRedactorActivity extends AppCompatActivity {
    public static final String KEY_MEME = "Meme";

    public static final String KEY_MEME_BITMAP = "MemeBitmap";
    public static final String KEY_MEME_NAME = "MemeName";

    private ImageView memeIcon_;
    @Nullable
    private Meme meme_;
    private BottomNavigationView bottomNavigationView_;
    private List<EditText> addedTextsToMeme_ = new ArrayList<>();
    private static int currentTextColor_ = Color.BLACK;

    private TextView colorShower_;
    private ViewGroup rootView_;
    private ViewGroup colorPickerRootView_;
    private ViewGroup memeIconRootView_;
    private int xDelta_;
    private int yDelta_;

    static Intent newIntent(@NonNull Context context, @NonNull Meme meme) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        intent.putExtra(KEY_MEME, meme);
        return intent;
    }

    static Intent newIntent(@NonNull Context context, @NonNull Bitmap memeIcon,
                            @NonNull String memeName) {
        Intent intent = new Intent(context, MemeRedactorActivity.class);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        memeIcon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra(KEY_MEME_BITMAP, byteArray);
        intent.putExtra(KEY_MEME_NAME, memeName);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_redactor);
        byte[] byteArray = getIntent().getByteArrayExtra(KEY_MEME_BITMAP);     //TODO refactor this snippet
        if (byteArray != null) {
            Bitmap memeIcon = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            bindViews();
            memeIcon_.setImageBitmap(memeIcon);
            meme_ = new Meme();
            meme_.name = getIntent().getStringExtra(KEY_MEME_NAME);
        } else {
            meme_ = (Meme) getIntent().getSerializableExtra(KEY_MEME);
            bindViews();
            if (meme_ != null) {
                Picasso.with(this).load(Uri.parse(meme_.url)).into(memeIcon_);
            }
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
                showDoneDialog();
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
        bottomNavigationView_ = (BottomNavigationView) findViewById(R.id.bottom_navigation_view_meme_redactor);
        bottomNavigationView_.setOnNavigationItemSelectedListener(new NavigationItemSelectedListener());
        rootView_ = (ViewGroup) findViewById(R.id.root_view);
        memeIconRootView_ = (ViewGroup) findViewById(R.id.meme_icon_redactor_root);
        colorPickerRootView_ = (ViewGroup) findViewById(R.id.color_picker_root);
        memeIcon_ = (ImageView) findViewById(R.id.meme_icon_redactor);
        colorShower_ = (TextView) findViewById(R.id.color_shower);
        Button btnColorSubmit = (Button) findViewById(R.id.btn_color_submit);
        btnColorSubmit.setOnClickListener(new OnButtonClickListener());
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                currentTextColor_ = color;
                colorShower_.setTextColor(color);
            }
        });
    }

    private class OnButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            rootView_.setVisibility(View.VISIBLE);
            colorPickerRootView_.setVisibility(View.GONE);
        }
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
        EditText editText = new EditText(this);
        editText.setHint(R.string.tap_here);
        editText.setTextColor(currentTextColor_);
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
        memeIconRootView_.addView(editText);
    }

    private void removeTextFromMeme() {
        if (!addedTextsToMeme_.isEmpty()) {
            EditText lastAddedText = addedTextsToMeme_.get(addedTextsToMeme_.size() - 1);
            memeIconRootView_.removeView(lastAddedText);
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
