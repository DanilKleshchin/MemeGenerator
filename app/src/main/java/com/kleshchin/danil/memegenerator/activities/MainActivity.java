package com.kleshchin.danil.memegenerator.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kleshchin.danil.memegenerator.R;
import com.kleshchin.danil.memegenerator.models.Meme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        switch (requestCode) {
            case IMAGE_GALLERY_REQUEST:
                receiveImageGalleryRequest(data);
                break;
            case TAKE_PHOTO_REQUEST:
                receiveTakePhotoRequest(data);
                break;
            default:
                break;
        }
    }

    private void receiveImageGalleryRequest(@NonNull Intent data) {
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

    private void receiveTakePhotoRequest(@NonNull Intent data) {
        Bitmap memeIcon = (Bitmap) data.getExtras().get("data");
        if (memeIcon == null) {
            return;
        }
        String memeName = String.valueOf(System.currentTimeMillis());
        Intent intent = MemeRedactorActivity.newIntent(this, memeIcon, memeName);
        startActivity(intent);
    }

    private void bindViews() {
        Button btnGallery = (Button) findViewById(R.id.btn_gallery);
        Button btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        Button btnInternet = (Button) findViewById(R.id.btn_internet);
        btnGallery.setOnClickListener(new OnButtonClickListener());
        btnTakePhoto.setOnClickListener(new OnButtonClickListener());
        btnInternet.setOnClickListener(new OnButtonClickListener());
    }

    private class OnButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.btn_gallery:
                    intent = new Intent(Intent.ACTION_PICK);
                    File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = pictureDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);
                    String imagePattern = "image/*";
                    intent.setDataAndType(data, imagePattern);
                    startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
                    break;
                case R.id.btn_take_photo:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, TAKE_PHOTO_REQUEST);
                    break;
                case R.id.btn_internet:
                    intent = new Intent(MainActivity.this, MemeListFromApiActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }

        private void takeScreenshoot() {

        }

        private void chooseFromGallery() {

        }
    }
}
