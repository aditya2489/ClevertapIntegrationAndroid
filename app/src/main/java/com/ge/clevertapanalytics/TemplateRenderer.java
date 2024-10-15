package com.ge.clevertapanalytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class TemplateRenderer {

    private static TemplateRenderer instance;

    public static TemplateRenderer getInstance() {
        if (instance == null) {
            return new TemplateRenderer();
        } else {
            return instance;
        }
    }

    public void showPushNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        PushTemplateRenderer.getInstance().render(applicationContext, extras, listener);
    }

}
