package com.kleshchin.danil.memegenerator;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Danil Kleshchin on 15.09.2017.
 */
public class MemeDoneDialog extends Dialog {

    private Button btnShare_;
    private Button btnSave_;
    private Button btnShareAndSave_;
    private Bitmap meme_;
    private String memeName_;

    public MemeDoneDialog(@NonNull Context context, @NonNull Bitmap meme, @NonNull String memeName) {
        super(context);
        meme_ = meme;
        memeName_ = memeName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_done_meme);
        bindViews();
    }

    private void bindViews() {
        btnSave_ = (Button) findViewById(R.id.save_meme);
        btnShare_ = (Button) findViewById(R.id.share_meme);
        btnShareAndSave_ = (Button) findViewById(R.id.share_and_save_meme);
        OnButtonClickListener buttonClickListener = new OnButtonClickListener();
        btnSave_.setOnClickListener(buttonClickListener);
        btnShare_.setOnClickListener(buttonClickListener);
        btnShareAndSave_.setOnClickListener(buttonClickListener);
    }

    private class OnButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.save_meme:
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        meme_.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        File file = new File(Environment.getExternalStorageDirectory() + File.separator + memeName_ + ".png");
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(byteArrayOutputStream.toByteArray());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.share_meme:
                    String bitmapPath = MediaStore.Images.Media.
                            insertImage(getContext().getContentResolver(), meme_, memeName_, null);
                    Uri bitmapUri = Uri.parse(bitmapPath);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    getContext().startActivity(Intent.createChooser(intent, "Share meme"));
                    break;
                case R.id.share_and_save_meme:
                    break;
                default:
                    break;
            }
            MemeDoneDialog.this.dismiss();
        }
    }
}
