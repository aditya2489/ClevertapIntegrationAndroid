package com.ge.clevertapanalytics;

import static com.clevertap.android.sdk.Utils.runOnUiThread;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CustomBMRender extends FirebaseMessagingService {


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
        Map<String, Object> myMap = BundleToMap.bundleToMap(extras);
        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (!flag) {

            // Handle payload from Firebase
            Log.d("FCM data", "FCM data: " + new Gson().toJson(message));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = new Random().nextInt(60000);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ch111")
                    .setSmallIcon(R.drawable.ct_volume_on)
                    .setColor(Color.YELLOW)
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setAutoCancel(true)
                    .setOngoing(true);

            notificationManager.notify(notificationId, notificationBuilder.build());

        }  else if (extras.containsKey("isSticky")) {
            // Handle sticky notifications
            Log.d("PT","Payload received "+ new Gson().toJson(message));
            handleNotification( message , extras);

        } else {
            // Handle other scenarios or CleverTap notifications

            new CTFcmMessageHandler().createNotification(getApplicationContext(), message);
            callIntent(extras);
        }
    }

    private void handleNotification(RemoteMessage message, Bundle extras) {

        Log.d("PT","Inside handleNotification ");
        String imageUrl = message.getData().get(CTPNConstants.PN_BACKGROUND_IMAGE);

        GlideApp.with(CustomBMRender.this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>(1500, 750) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap largeIconBitmap, @Nullable Transition<? super Bitmap> transition) {
                        Log.d("PT", "Successfully loaded image.");
                        runOnUiThread(() -> renderNotification( message , extras, largeIconBitmap,true));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if needed
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e("PT", "Failed to load image.");
                        runOnUiThread(() -> renderNotification(message, extras,null,false));
                    }
                });
    }

    private void renderNotification( RemoteMessage message, Bundle extras,Bitmap largeIconBitmap ,boolean hasImage) {

        // Handle Sticky notification with image

        Log.d("PT","Inside renderNotification ");
        String deeplink = message.getData().get(CTPNConstants.PN_DEEPLINK);
        String c2a = "";

        // Parse the actions array
        try {
            JSONArray actionsArray = new JSONArray(message.getData().get(CTPNConstants.PN_ACTIONS));
            if (actionsArray.length() > 0) {
                JSONObject actionObject = actionsArray.getJSONObject(0);
                c2a = actionObject.optString("l", "");
            }
        } catch (Exception e) {
            Log.e("PT", "Failed to parse wzrk_acts: " + e.getMessage());
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(60000);

        Intent dismissIntent = new Intent(CustomBMRender.this, NotificationDismissReceiver.class);
        dismissIntent.putExtra(CTPNConstants.PN_NOTIFICATION_ID, notificationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(CustomBMRender.this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink));
        PendingIntent pendingIntent = PendingIntent.getActivity(CustomBMRender.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(CustomBMRender.this, "ch111")
                .setSmallIcon(R.drawable.btn_ripple_background)
                .setContentTitle(message.getData().get(CTPNConstants.PN_TITLE))
                .setContentText(message.getData().get(CTPNConstants.PN_MESSAGE))
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(0, CTPNConstants.PN_DISMISS_BTN, dismissPendingIntent);

        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent);
            if (!c2a.isEmpty()) {
                notificationBuilder.addAction(0, c2a, pendingIntent);
            }
        }

        if (hasImage) {
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                    .bigPicture(largeIconBitmap)
                    .bigLargeIcon(null);
            notificationBuilder.setStyle(bigPictureStyle);
            notificationBuilder.setLargeIcon(largeIconBitmap);

            Objects.requireNonNull(CleverTapAPI.getDefaultInstance(CustomBMRender.this)).pushNotificationViewedEvent(extras);
            callIntent(extras);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
        Objects.requireNonNull(CleverTapAPI.getDefaultInstance(CustomBMRender.this)).pushNotificationViewedEvent(extras);
        callIntent(extras);
    }

    private void callIntent(Bundle extras)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("myextra", extras);
    }
}
