package com.ge.clevertapanalytics

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.smarteist.autoimageslider.SliderView
import org.json.JSONObject


class InAppActivity : AppCompatActivity(), InAppNotificationButtonListener, DisplayUnitListener {
    var datas: Uri? = null
    var moveAhead : AppCompatButton? = null
    var cleverTapDefaultInstance : CleverTapAPI? = null
    var sliderView: SliderView? = null
    var sliderDataArrayList = ArrayList<SliderData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app)
        cleverTapDefaultInstance = (this.application as PushTemplateHandler).ctInstance
        cleverTapDefaultInstance!!.setInAppNotificationButtonListener(this);
        moveAhead = findViewById(R.id.moveAhead)
        sliderView = findViewById(R.id.slider);

        moveAhead!!.setOnClickListener {
            sendBoundedEvents()
        }

        renderInApp()
        initializeNativeDisplay()

        if ("Dismiss" == intent.action) {
            var notificationId : Int = intent.getIntExtra("nid", -1)
            if(notificationId != -1) {
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId);
            }
        }

        val message = intent.getStringExtra("coupon")
        if (message != null) {
            textCopy(this,message)
        } else {
            Toast.makeText(this, "Invalid coupon!", Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun initializeNativeDisplay() {
        try {
            cleverTapDefaultInstance?.apply {
                setDisplayUnitListener(this@InAppActivity)
            }
        } catch (e: Exception) {

        }
    }
    private fun sendBoundedEvents() {
        //cleverTapDefaultInstance!!.pushEvent("BoundedEvent1")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEvent2")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEventQualify")
        cleverTapDefaultInstance!!.pushEvent("RequestNativeDisplay")
        //startActivity(Intent(applicationContext,FinalCartActivity::class.java))

    }
    override fun onResume() {
        super.onResume()
        var intent = intent
        datas = intent.data
        Log.v("TestingsDatas", "" + datas)
        //CleverTapAPI.getDefaultInstance(this)!!.resumeInAppNotifications()
    }

    private fun renderInApp() {
        try{
            cleverTapDefaultInstance!!.pushEvent("InAppActivity")
        }
        catch (e : Exception){

        }
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        TODO("Not yet implemented")
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        if (sliderDataArrayList != null) {
            sliderDataArrayList.removeAll(sliderDataArrayList)
        }

        CleverTapAPI.getDefaultInstance(applicationContext)!!.pushDisplayUnitViewedEventForID(
            units!![0].unitID
        )
        for (i in 0 until units!!.size) {
            val unit = units!![i]
            for (j in unit.contents) {
                //getting urls and adding to array list
                //sliderDataArrayList.add(SliderData(j.media))

                //Notification Clicked Event
//                sliderView.setOnClickListener(v -> CleverTapAPI.getDefaultInstance(getApplicationContext()).pushDisplayUnitClickedEventForID(unit.getUnitID()));
            }
            for(k in unit.customExtras){

                    val carddata = JSONObject(k.value)
                    sliderDataArrayList.add(SliderData(carddata.getString("messagebody")))
            }
        }
        //CleverTapAPI.getDefaultInstance(this).pushDisplayUnitViewedEventForID(units.get(1).getUnitID());
        //CleverTapAPI.getDefaultInstance(this).pushDisplayUnitViewedEventForID(units.get(1).getUnitID());
        val adapter = SliderAdapter(this, sliderDataArrayList)
        sliderView!!.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
        sliderView!!.setSliderAdapter(adapter)
        sliderView!!.isAutoCycle = false
        sliderView!!.setOffscreenPageLimit(3)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cleverTapDefaultInstance?.pushNotificationClickedEvent(intent!!.extras)
            dismissNotification(intent!!,this)
            Log.d("MainActivityNewIntent", "-------------------"+intent.dataString)

        }
    }

    private fun textCopy(context: Context, couponCode: String) {
        try {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", couponCode)
            clipboard.setPrimaryClip(clip)
        } catch (e: java.lang.Exception) {
            com.google.android.exoplayer2.util.Log.e(
                "Exception - ",
                "PushTemplateRenderer " + e.localizedMessage
            )
        }
    }

    fun dismissNotification(intent: Intent, applicationContext: Context) {
        val extras = intent.extras
        if (extras != null) {
            val actionId = extras.getString("actionId")
            if (actionId != null) {
                val autoCancel = extras.getBoolean("autoCancel", true)
                val notificationId = extras.getInt("notificationId", -1)
                if (autoCancel && notificationId > -1) {
                    val notifyMgr =
                        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notifyMgr.cancel(notificationId)
                }
            }
        }
    }


}

