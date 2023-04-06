package com.ge.clevertapanalytics

import android.content.Intent
import android.net.Uri
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
        sliderView!!.scrollTimeInSec = 3
        sliderView!!.isAutoCycle = true
        sliderView!!.startAutoCycle()
    }

}

