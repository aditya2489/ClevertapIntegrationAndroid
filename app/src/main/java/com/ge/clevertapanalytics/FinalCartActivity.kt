package com.ge.clevertapanalytics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import java.util.HashMap

class FinalCartActivity : AppCompatActivity(), InAppNotificationButtonListener {
    var cleverTapDefaultInstance : CleverTapAPI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cleverTapDefaultInstance = (this.application as PushTemplateHandler).ctInstance
        setContentView(R.layout.activity_final_cart)
        cleverTapDefaultInstance!!.setInAppNotificationButtonListener(this);
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        TODO("Not yet implemented")
    }
}