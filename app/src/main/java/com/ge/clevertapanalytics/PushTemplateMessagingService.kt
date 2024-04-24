package com.ge.clevertapanalytics

import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.lang.Exception

class PushTemplateMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        try {
            Log.d(this.javaClass.name, message.toString())
            CTFcmMessageHandler()
                .createNotification(applicationContext, message)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

        /*Log.d("PushTemplateMessagingService", "CT json: " + Gson().toJson(message))
        val extras = Bundle()
        if (message.data.isNotEmpty()) {
            for ((key, value) in message.data) {
                extras.putString(key, value)
            }
        }
        CleverTapAPI.processPushNotification(applicationContext, extras)
        CleverTapAPI.createNotification(applicationContext,extras)*/
    }

}