package com.kleshchin.danil.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.kleshchin.danil.imageloader.ImageLoader.createFileNameFromStringUrl;

/**
 * Created by Danil Kleshchin on 11.09.2017.
 */
class ImageDownloader extends AsyncTask<String, Integer, Void> {
    @Nullable
    private String path_;
    @Nullable
    private OnFileDownloadListener listener_;
    @Nullable
    private Context context_;
    @NonNull
    private
    String filePath = "";
    private Bitmap bitmap_;

    ImageDownloader(@NonNull Context context) {
        context_ = context;
    }

    void setListener(@Nullable OnFileDownloadListener listener) {
        listener_ = listener;
    }

    void setBasePathToDownloadDir(@Nullable String path) {
        path_ = path;
    }

    @Override
    protected Void doInBackground(String... stringUrl) {
        if (path_ == null) {
            return null;
        }
        File file = createDirectory(path_);
        file = new File(file.getPath() + File.separator + createFileNameFromStringUrl(stringUrl[0]));
        filePath = file.getAbsolutePath();

        if (!checkNetworkAvailable()) {
            return null;
        }

        if (!file.exists() || file.length() == 0) {
            file.delete();
            try {
                OutputStream outputStream = new FileOutputStream(filePath);
                downloadFile(stringUrl[0], outputStream);
            } catch (FileNotFoundException e) {
                e.getMessage();
            }
        }
        loadImageIntoView();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (listener_ != null) {
            listener_.onFileDownload(bitmap_, filePath);
        }
    }

    @NonNull
    private File createDirectory(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private boolean checkNetworkAvailable() {
        if (context_ != null) {
            ConnectivityManager cm = (ConnectivityManager) context_.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null &&
                    networkInfo.isAvailable() &&
                    networkInfo.isConnected();
        }
        return false;
    }

    private void downloadFile(@NonNull String strUrl, @NonNull OutputStream outputStream) {
        try {
            HttpURLConnection connection = getRedirectedConnection(strUrl);
            if (connection != null) {
                InputStream inputStream = connection.getInputStream();
                int bytesRead;
                byte[] buffer = new byte[8192];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    private void loadImageIntoView() {
        if (!filePath.equals("")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            bitmap_ = BitmapFactory.decodeFile(filePath, options);
        }
    }

    @Nullable
    private HttpURLConnection getRedirectedConnection(@NonNull String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                return connection;
            }
            if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_MOVED_PERM) {
                return getRedirectedConnection(connection.getHeaderField("Location"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    interface OnFileDownloadListener {
        void onFileDownload(@Nullable Bitmap bitmap, @Nullable String filePath);
    }
}
