package com.ge.clevertapanalytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.interfaces.NotificationHandler;
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener;

@SuppressWarnings({"unused"})
public class PushTemplateHandler extends android.app.Application  implements CTPushAmpListener {

    private CleverTapAPI cleverTapDefaultInstance;
    @Override
    public void onCreate() {
        setCTInstance();
        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        assert cleverTapAPI != null;
        cleverTapAPI.setCTPushAmpListener(this);
        CleverTapAPI.setNotificationHandler((NotificationHandler)new PushTemplateNotificationHandler());
        //ActivityLifecycleCallback.register(this);

        setActivitycallbacks();
        super.onCreate();
    }

    private void setActivitycallbacks() {
        this.registerActivityLifecycleCallbacks(
                new android.app.Application.ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                        CleverTapAPI.setAppForeground(true);
                        try {
                            CleverTapAPI.getDefaultInstance(PushTemplateHandler.this).pushNotificationClickedEvent(activity.getIntent().getExtras());
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
    }
}
