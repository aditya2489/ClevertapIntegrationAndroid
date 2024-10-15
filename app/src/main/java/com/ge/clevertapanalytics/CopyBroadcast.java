package com.ge.clevertapanalytics;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Log;

import java.util.Objects;

public class CopyBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "com.ge.copycoupon")) {

            context.startActivity(new Intent(context,MainActivity.class));
        }

    }

}
