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

class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {
    @Nullable
    private String path_;
    @Nullable
    private OnFileDownloadListener listener_;
    @Nullable
    private Context context_;
    @NonNull
    private
    String filePath = "";

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
    protected Bitmap doInBackground(String... stringUrl) {

        if (path_ != null) {
            File file = createDirectory(path_);
            file = new File(file.getPath() + File.separator + createFileNameFromStringUrl(stringUrl[0]));
            if (isNetworkAvailable()) {
                if (file.exists()) {
                    file.delete();
                }
                try {
                    filePath = file.getAbsolutePath();
                    OutputStream outputStream = new FileOutputStream(filePath);
                    downloadFile(stringUrl[0], outputStream);
                    file = new File(filePath);
                    if (file.length() == 0) {
                        file.delete();
                        outputStream = new FileOutputStream(filePath);
                        downloadFile(createClubNameWithoutFC(stringUrl[0]), outputStream);
                    }
                } catch (FileNotFoundException e) {
                    e.getMessage();
                }
            } else {
                if (file.exists()) {
                    filePath = file.getAbsolutePath();
                }
            }
        }
        if (!filePath.equals("")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            return BitmapFactory.decodeFile(filePath, options);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (listener_ != null) {
            listener_.onFileDownload(bitmap, filePath);
        }
    }

    private boolean isNetworkAvailable() {
        if (context_ != null) {
            ConnectivityManager cm = (ConnectivityManager) context_.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null &&
                    networkInfo.isAvailable() &&
                    networkInfo.isConnected();
        }
        return false;
    }

    @NonNull
    private String createClubNameWithoutFC(@NonNull String iconUrl) {
        String extension = ".png";
        try {
            return iconUrl.substring(0, iconUrl.lastIndexOf("-")) + extension;
        } catch (IndexOutOfBoundsException e) {
            return iconUrl;
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
