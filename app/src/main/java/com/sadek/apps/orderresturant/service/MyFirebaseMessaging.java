package com.sadek.apps.orderresturant.service;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sadek.apps.orderresturant.MainActivity;
import com.sadek.apps.orderresturant.OrderStatusActivity;
import com.sadek.apps.orderresturant.R;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.helper.NotificationHelper;

import java.net.URI;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mahmoud Sadek on 8/16/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData()!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   //only working when api is 26 or higher
                sendNotificationApi26(remoteMessage);
            } else
                sendNotification(remoteMessage);
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private void sendNotificationApi26(RemoteMessage remoteMessage) {

        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        // check to notification and go to  order list
        PendingIntent pendingIntent;
        NotificationHelper notificationHelper;
        Notification.Builder builder;

        if (Common.currentUser !=null) {
            Intent intent = new Intent(this, OrderStatusActivity.class);
            intent.putExtra("userPhone", Common.currentUser.getPhone());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            notificationHelper = new NotificationHelper(this);
            builder = notificationHelper.getFoodOrderNotificationChannel(title, message, pendingIntent, defaultSoundUri);


            // get random id  for notifiction to show all notification
            notificationHelper.getManager().notify(new Random().nextInt(), builder.build());
        }else {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            notificationHelper = new NotificationHelper(this);
            builder = notificationHelper.getFoodOrderNotificationChannel(title, message, defaultSoundUri);


            // get random id  for notifiction to show all notification
            notificationHelper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());
    }

}

