package com.ge.clevertapanalytics;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.interfaces.NotificationHandler;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener;
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;

import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class PushTemplateHandler extends android.app.Application  implements CTPushAmpListener, CTPushNotificationListener {

    private CleverTapAPI cleverTapDefaultInstance;

    public FirebaseAnalytics defaultFirebaseAnalytics;
    private static final String TAG = String.format("%s.%s", "CLEVERTAP", PushTemplateHandler.class.getName());
    private static final String WRITE_KEY = GeneralConstants.SEGMENT_WRITE_KEY; //This you will receive under source in segment.
    private static final String CLEVERTAP_KEY = "CleverTap";
    public static boolean sCleverTapSegmentEnabled = false;
    private static final Handler handler = null;

    @Override
    public void onCreate() {
        CleverTapAPI.createNotificationChannel(getApplicationContext(),"ch111","CT-Push","CT-Push", NotificationManager.IMPORTANCE_MAX,true,"sound2.wav");
        setCTInstance();
        setIdentifierForRTUT();
        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        assert cleverTapAPI != null;
        cleverTapAPI.setCTPushAmpListener(this);
        CleverTapAPI.setNotificationHandler((NotificationHandler)new PushTemplateNotificationHandler());


        ActivityLifecycleCallback.register(this);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        cleverTapDefaultInstance.pushFcmRegistrationId(token,true);
                        // Log and toast
                        Log.d("----- FCM token -----", token);
                    }
                });
        setActivitycallbacks();
        super.onCreate();
        Analytics analytics = new Analytics.Builder(getApplicationContext(), WRITE_KEY)
                .logLevel(Analytics.LogLevel.VERBOSE)
                .trackApplicationLifecycleEvents()
                .use(CleverTapIntegration.FACTORY)
                .build();

        analytics.onIntegrationReady(CLEVERTAP_KEY, new Analytics.Callback<CleverTapAPI>()
        {
            @Override
            public void onReady(CleverTapAPI instance) {
                Log.i(TAG, "analytics.onIntegrationReady() called");
                cleverTapIntegrationReady(instance);
            }
        });
        Analytics.setSingletonInstance(analytics);

    }

    private void setIdentifierForRTUT() {
        try {
            defaultFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Log.d("======== CTid ========",Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this)).getCleverTapID());
            defaultFirebaseAnalytics.setUserProperty("ct_objectId", Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this)).getCleverTapID());
        }catch(Exception e)
        {
            Log.d("No CTid exception",e.getLocalizedMessage());
        }
    }


    private void setActivitycallbacks() {
        this.registerActivityLifecycleCallbacks(
                new android.app.Application.ActivityLifecycleCallbacks() {


                    @Override
                    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                        CleverTapAPI.setAppForeground(true);
                        try {
                            CleverTapAPI.getDefaultInstance(PushTemplateHandler.this).pushNotificationClickedEvent(activity.getIntent().getExtras());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                dismissNotification(activity.getIntent(), PushTemplateHandler.this);
                            }
                        } catch (Throwable t) {
                            // Ignore
                        }
                        try {
                            Intent intent = activity.getIntent();
                            Uri data = intent.getData();
                            CleverTapAPI.getDefaultInstance(PushTemplateHandler.this).pushDeepLink(data);
                        } catch (Throwable t) {
                            // Ignore
                        }
                    }

                    @Override
                    public void onActivityStarted(@NonNull Activity activity) {

                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        try {
                            CleverTapAPI.getDefaultInstance(PushTemplateHandler.this).onActivityResumed(activity);
                        } catch (Throwable t) {
                            // Ignore
                        }
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        try {
                            CleverTapAPI.getDefaultInstance(PushTemplateHandler.this).onActivityPaused();
                        } catch (Throwable t) {
                            // Ignore
                        }
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                }
        );
    }



    public CleverTapAPI getCTInstance() {
        return cleverTapDefaultInstance;
    }

    public void setCTInstance() {
        this.cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(this);
    }

    @Override
    public void onPushAmpPayloadReceived(Bundle extras) {

        Log.d("PushTemplateHandler------------", extras.keySet().size()+"");
        CleverTapAPI.createNotification(getApplicationContext(), extras);
    }

    private void cleverTapIntegrationReady(CleverTapAPI instance)
    {
        instance.enablePersonalization();
        sCleverTapSegmentEnabled = true;
        cleverTapDefaultInstance = instance;
    }

    @Override
    public void onNotificationClickedPayloadReceived(HashMap<String, Object> payload) {
        Log.d("PushTemplateHandler---",""+payload);

        //CleverTapAPI.getDefaultInstance(this).pushNotificationClickedEvent(payload);
    }

    public static void dismissNotification( Intent intent, Context applicationContext){
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String actionId = extras.getString("actionId");
            if (actionId != null) {
                boolean autoCancel = extras.getBoolean("autoCancel", true);
                int notificationId = extras.getInt("notificationId", -1);
                if (autoCancel && notificationId > -1) {
                    NotificationManager notifyMgr =
                            (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    notifyMgr.cancel(notificationId);                }

            }
        }
    }
}
