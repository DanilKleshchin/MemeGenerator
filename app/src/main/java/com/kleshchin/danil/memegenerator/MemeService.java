package com.kleshchin.danil.memegenerator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kleshchin.danil.memegenerator.activities.MemeListFromApiActivity;
import com.kleshchin.danil.memegenerator.models.Meme;

import java.util.List;

/**
 * Created by Danil Kleshchin on 19.09.2017.
 */
public class MemeService extends Service implements MemeApiInterface.OnLoadMemeDataAsyncTaskListener {

    private MemeApiInterface memeApiInterface = new MemeApiInterface(this);
    @NonNull
    private Handler handler_ = new Handler();
    @NonNull
    private Runnable runnable_ = new Runnable() {
        @Override
        public void run() {
            new Thread() {
                @Override
                public void run() {
                    memeApiInterface.loadMemeAsyncTask(MemeService.this);
                    handler_.postDelayed(runnable_, 30000);
                }
            }.start();
        }
    };

    @Nullable
    public IBinder onBind(final Intent intent) {
        handler_.post(runnable_);
        return new Binder();
    }

    public void onDestroy() {
        super.onDestroy();
        handler_.removeCallbacks(runnable_);
    }

    @Override
    public void onLoadMemeAsyncTask(@Nullable List<Meme> memes) {
        if (memes == null) {
            return;
        }
        String message = "Memes from the server have been updated!";
        String title = "Notification!";
        createAndShowNotification(title, title, message);
    }

    @Override
    public void onLoadMemeErrorAsyncTask(@NonNull String message) {
        String title = "Error!";
        createAndShowNotification(title, title, message);
    }

    private Notification.Builder createAndShowNotification(@NonNull String ticker, @NonNull String title, @NonNull String message) {
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(this, MemeListFromApiActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
        return builder;
    }
}