package com.ge.clevertapanalytics;

import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.clevertap.android.sdk.CleverTapAPI;
//import com.clevertap.android.sdk.pushnotification.LaunchPendingIntentFactory;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
//import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
//import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
//import com.clevertap.android.xps.CTXiaomiMessageHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
//import com.xiaomi.mipush.sdk.MiPushMessage;

import java.util.Map;
import java.util.Random;

public class MYFCM extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token1) {
        super.onNewToken(token1);
        Log.d("MY_TOKEN", "Refreshed token: " + token1);

    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("CT data", "CT json: " + new Gson().toJson(message));
        Bundle extras = new Bundle();
        if (!message.getData().isEmpty()) {
            for (Map.Entry<String, String> entry : message.getData().entrySet()) {
                extras.putString(entry.getKey(), entry.getValue());
            }
        }
        NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);
        boolean flag = info.fromCleverTap;

        if (!flag) {
// if payload from Firebse
            Log.d("FCM data", "FCM data: " + new Gson().toJson(message));   // to print payload
            Log.d("img", "img: " + message.getNotification().getImageUrl());
            Log.d("title", "title = [" + message.getNotification().getTitle() + "]");
            Log.d("message", "message = [" + message.getNotification().getBody() + "]");

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = new Random().nextInt(60000);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ch111")

                    .setSmallIcon(R.drawable.btn_ripple_background)  //a resource for your custom small icon
                    .setColor(Color.YELLOW) //small ic6on bg color
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setAutoCancel(true);  //dismisses the notification on click

            notificationManager.notify(notificationId, notificationBuilder.build());
        } else {
            renderCustom(extras, message);
            //testNewRender();
        }
    }

    void renderCustom(Bundle extras, RemoteMessage message) {
        Log.d("TAG", "from ct");
        // if payload from clevertap
        CleverTapAPI.getDefaultInstance(this).pushNotificationViewedEvent(extras);
        Log.d("CT data", "CT raw: " + message);
        Log.d("CT data", "CT json: " + new Gson().toJson(message));   // to print payload
        Log.d("EXTRAS", "EXTRAS: " + extras);

        CleverTapAPI.processPushNotification(getApplicationContext(), extras);
        //boolean status=new CTFcmMessageHandler().createNotification(getApplicationContext(), message);

        Map<String, Object> myMap = BundleToMap.bundleToMap(extras);

        CleverTapAPI.getDefaultInstance(this).pushEvent("Payload Received", myMap);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //  custom rendering
        int notificationId = new Random().nextInt(60000);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        // intent.setData(Uri.parse(extras.getString("wzrk_dl")));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ch111")

                .setSmallIcon(R.drawable.ic_launcher_foreground)  //a resource for your custom small icon
                .setColor(Color.YELLOW)
                .setContentTitle(extras.getString("nt"))
                .setContentText(extras.getString("nm"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void testNewRender() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ch111")
                .setSmallIcon(R.drawable.pt_open_app)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.common_full_open_on_phone))
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(this.getResources(), R.drawable.common_full_open_on_phone))
                        .bigLargeIcon(null))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

}