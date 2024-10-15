package com.ge.clevertapanalytics;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.widget.Toast;


import com.google.android.exoplayer2.util.Log;

import java.util.Objects;
import java.util.Random;

public class PushTemplateRenderer {

    private static PushTemplateRenderer instance;

    public static PushTemplateRenderer getInstance() {
        if (instance == null) {
            return new PushTemplateRenderer();
        } else {
            return instance;
        }
    }

    public void render(Context applicationContext, Bundle extras, PushNotificationListener listener) {

        switch (Objects.requireNonNull(extras.getString("pt_id"))) {
            case "pt_progress_bar":
                //renderProgressBarNotification(applicationContext, extras, listener);
                break;
            case "pt_gif":
                //renderGIFNotification(applicationContext, extras, listener);
                break;
            case "pt_coupon":
                renderCouponNotification(applicationContext, extras, listener);
                //renderTimerWithButtons(applicationContext, extras, listener);
                break;
            default:
                listener.onPushFailed();
                break;
        }

    }



    private NotificationCompat.Builder getNotification(String title, NotificationCompat.Builder builder, RemoteViews collapsed, RemoteViews expanded) {
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(collapsed)
                .setCustomBigContentView(expanded)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void renderCouponNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        try {
            String pushTitle = extras.getString("pt_title");
            String pushMessage = extras.getString("pt_msg");
            String deepLink = extras.getString("pt_dl");
            String discount = extras.getString("pt_discount");
            String disc_title = extras.getString("pt_discount_txt");
            String couponCode = extras.getString("pt_cc");

            if (pushTitle == null || pushMessage == null || couponCode == null) {
                throw new IllegalArgumentException();
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, Objects.requireNonNull(extras.getString("wzrk_cid")));
            int notificationId = new Random().nextInt(60000);


            RemoteViews couponCodeView = new RemoteViews(applicationContext.getPackageName(), R.layout.coupon_code_expanded);
            couponCodeView.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeView.setTextViewText(R.id.notification_body, pushMessage);
            couponCodeView.setTextViewText(R.id.notification_discount, discount);
            couponCodeView.setTextViewText(R.id.notification_coupon, couponCode);
            couponCodeView.setTextViewText(R.id.notification_coupon_text,disc_title);

            RemoteViews couponCodeViewsCollapsed = new RemoteViews(applicationContext.getPackageName(), R.layout.coupon_code_collpsed);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_coupon, couponCode);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.putExtra("coupon",couponCode);
            intent.putExtra("nid",notificationId);
            intent.setAction("Dismiss");

            if (intent.resolveActivity(applicationContext.getPackageManager()) != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                couponCodeView.setOnClickPendingIntent(R.id.clickarea,pendingIntent);
                couponCodeViewsCollapsed.setOnClickPendingIntent(R.id.notification_coupon,pendingIntent);

                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setCustomContentView(couponCodeViewsCollapsed)
                        .setCustomBigContentView(couponCodeView)// Set custom notification layout
                        .setAutoCancel(true);

                if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                notificationManager.notify(notificationId, builder.build());
            }
            else {
                Toast.makeText(applicationContext, "Invalid Page!", Toast.LENGTH_SHORT).show();
            }


        }
        catch (Exception e){
            Log.d("Here is the exception",e.getLocalizedMessage());
        }

    }

    private void renderTimerWithButtons(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        try {
            String pushTitle = extras.getString("pt_title");
            String pushMessage = extras.getString("pt_msg");
            String deepLink = extras.getString("pt_dl");
            String discount = extras.getString("pt_discount");
            String disc_title = extras.getString("pt_discount_txt");
            String couponCode = extras.getString("pt_cc");

            if (pushTitle == null || pushMessage == null || couponCode == null) {
                throw new IllegalArgumentException();
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, Objects.requireNonNull(extras.getString("wzrk_cid")));
            int notificationId = new Random().nextInt(60000);

            RemoteViews couponCodeView = new RemoteViews(applicationContext.getPackageName(), R.layout.timer_with_buttons);
            couponCodeView.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeView.setTextViewText(R.id.offerText, pushMessage);
            couponCodeView.setTextViewText(R.id.countdown, discount);
            couponCodeView.setTextViewText(R.id.subtitle, couponCode);

            RemoteViews couponCodeViewsCollapsed = new RemoteViews(applicationContext.getPackageName(), R.layout.timer_with_buttons_collapsed);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_coupon, couponCode);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.putExtra("coupon",couponCode);
            intent.putExtra("nid",notificationId);
            intent.setAction("Dismiss");

            if (intent.resolveActivity(applicationContext.getPackageManager()) != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                couponCodeView.setOnClickPendingIntent(R.id.shopNowButton,pendingIntent);
                //couponCodeViewsCollapsed.setOnClickPendingIntent(R.id.notification_coupon,pendingIntent);

                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setCustomContentView(couponCodeViewsCollapsed)
                        .setCustomBigContentView(couponCodeView)// Set custom notification layout
                        .setAutoCancel(true);

                if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                notificationManager.notify(notificationId, builder.build());
            }
            else {
                Toast.makeText(applicationContext, "Invalid Page!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Log.d("Here is the exception",e.getLocalizedMessage());
        }

    }

}

