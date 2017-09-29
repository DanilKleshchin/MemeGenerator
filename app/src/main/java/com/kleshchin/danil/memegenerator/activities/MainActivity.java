package com.kleshchin.danil.memegenerator.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.models.Meme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;

    private Uri imageUri_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_GALLERY_REQUEST:
                receiveImageGalleryRequest(data);
                break;
            case TAKE_PHOTO_REQUEST:
                receiveTakePhotoRequest();
                break;
            default:
                break;
        }
    }

    private void receiveImageGalleryRequest(@Nullable Intent data) {
        if (data == null) {
            return;
        }
        Uri imageUri = data.getData();
        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            long id = System.currentTimeMillis();
            String memeName = imageUri.getLastPathSegment();
            Meme meme = new Meme(id, bitmap.getWidth(), bitmap.getHeight(),
                    memeName, imageUri.toString());
            Intent intent = MemeRedactorActivity.newIntent(this, meme);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.cant_open_photo, Toast.LENGTH_LONG).show();
        }
    }

    private void receiveTakePhotoRequest() {
        String memeIconPath = getRealPathFromURI(imageUri_);
        if (memeIconPath != null) {
            Intent intent = MemeRedactorActivity.newIntent(this, memeIconPath);
            startActivity(intent);
        }
    }

    private void bindViews() {
        Button btnGallery = (Button) findViewById(R.id.btn_gallery);
        Button btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        Button btnInternet = (Button) findViewById(R.id.btn_server);
        btnGallery.setOnClickListener(new OnButtonClickListener());
        btnTakePhoto.setOnClickListener(new OnButtonClickListener());
        btnInternet.setOnClickListener(new OnButtonClickListener());
    }

    @Nullable
    private String getRealPathFromURI(Uri contentUri) {
        String result = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    private class OnButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_gallery:
                    chooseFromGallery();
                    break;
                case R.id.btn_take_photo:
                    takePhoto();
                    break;
                case R.id.btn_server:
                    startMemeListFromApiActivity();
                    break;
                default:
                    break;
            }
        }

        private void takePhoto() {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri_ = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri_);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        }

        private void chooseFromGallery() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            try {
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String pictureDirectoryPath = pictureDirectory.getPath();
                Uri data = Uri.parse(pictureDirectoryPath);
                String imagePattern = "image/*";
                intent.setDataAndType(data, imagePattern);
                startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private void startMemeListFromApiActivity() {
            Intent intent = new Intent(MainActivity.this, MemeListFromApiActivity.class);
            startActivity(intent);
        }
    }
}
