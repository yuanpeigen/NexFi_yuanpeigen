package com.nexfi.yuanpeigen.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.nexfi.R;

import java.util.Timer;

/**
 * Created by Mark on 2016/3/9.
 */
public class NotificationService extends Service {
    private Timer timer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
   /*     timer = new Timer();
        context = getApplicationContext();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
            }

        }, 0, 1000);*/

    }

    //创建通知
    public void createInform(int avatar, String username) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this);
        Intent resultIntent = new Intent(this, ChatActivity.class);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), avatar);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentTitle(username)
                .setContentText("您有新消息")
                .setLargeIcon(bitmap)
                .setSmallIcon(avatar)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());
        manager.notify(52, notifyBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createInform(intent.getIntExtra("avatar", R.mipmap.user_head_female_1), intent.getStringExtra("username"));
        return super.onStartCommand(intent, flags, startId);
    }
}
