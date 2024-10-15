package com.ge.clevertapanalytics

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clevertap.android.geofence.CTGeofenceAPI
import com.clevertap.android.geofence.CTGeofenceSettings
import com.clevertap.android.geofence.Logger
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener
import com.clevertap.android.pushtemplates.TemplateRenderer
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.InAppNotificationListener
import com.clevertap.android.sdk.InboxMessageButtonListener
import com.clevertap.android.sdk.InboxMessageListener
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inapp.CTInAppNotification
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.android.sdk.inapp.InAppListener
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), InAppNotificationButtonListener, CTInboxListener,
    InboxMessageListener,
    InboxMessageButtonListener, DisplayUnitListener, CompoundButton.OnCheckedChangeListener,
    CTLocationUpdatesListener, CTGeofenceAPI.OnGeofenceApiInitializedListener,
    CTGeofenceEventsListener,
    PushPermissionResponseListener, InAppListener, InAppNotificationListener {

    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    var defaultFirebaseAnalytics: FirebaseAnalytics? = null
    var eventButton: AppCompatButton? = null
    var profilePushButton: AppCompatButton? = null
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var inAppButton: AppCompatButton? = null
    var pushNotification: AppCompatButton? = null
    var appInboxButton: AppCompatButton? = null
    var getAppInboxMessage: AppCompatButton? = null
    var nativeDisplayButton: AppCompatButton? = null
    var addedToCartButton: AppCompatButton? = null
    var chargedButton: AppCompatButton? = null
    var addCart: AppCompatButton? = null
    var clearCart: AppCompatButton? = null
    var notifyMe: AppCompatButton? = null
    var isPrimerPopupGranted : Boolean = false

    var card_basic: CardView? = null
    var text1: TextView? = null
    var titlem: TextView? = null
    var msg: TextView? = null
    var pushTemplates: AppCompatButton? = null
    val idList = arrayListOf<String>()
    val pushTemplateJsons = HashMap<String, Any>()

    val cartProductList = arrayListOf<String>()
    val executor = Executors.newSingleThreadExecutor()
    var imageOnlineBm: Bitmap? = null
    var imageMainBm: Bitmap? = null
    var imageIconBm: Bitmap? = null

    var imageOnline: AppCompatImageView? = null
    var imageMain: AppCompatImageView? = null
    var imageIcon: AppCompatImageView? = null

    var ll: RelativeLayout? = null
    val handler = Handler(Looper.getMainLooper())

    var edtphone: EditText? = null
    var edtemail: EditText? = null
    var edtid: EditText? = null

    var optin: SwitchCompat? = null
    var offline: SwitchCompat? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        eventButton = findViewById(R.id.eventButton)
        profilePushButton = findViewById(R.id.profilePushButton)
        inAppButton = findViewById(R.id.inAppButton)
        pushNotification = findViewById(R.id.pushButton)
        appInboxButton = findViewById(R.id.appInboxButton)
        getAppInboxMessage = findViewById(R.id.getAppInboxMessage)
        nativeDisplayButton = findViewById(R.id.nativeDisplayButton)
        pushTemplates = findViewById(R.id.pushTemplates)
        addedToCartButton = findViewById(R.id.addToCartButton)
        chargedButton = findViewById(R.id.chargedButton)
        addCart = findViewById(R.id.addCart)
        notifyMe = findViewById(R.id.sendBounded)
        imageIcon = findViewById(R.id.iconimage)
        imageIcon = findViewById(R.id.iconimage)
        imageMain = findViewById(R.id.mainimage)
        ll = findViewById(R.id.innerimage)

        card_basic = findViewById(R.id.card_basic)
        titlem = findViewById(R.id.titlem)
        msg = findViewById(R.id.msg)
        clearCart = findViewById(R.id.clearCart)
        edtemail = findViewById(R.id.editTextTextEmailAddress2)
        edtphone = findViewById(R.id.editTextPhone2)
        edtid = findViewById(R.id.editTextTextPersonName)
        optin = findViewById(R.id.setOptout)
        offline = findViewById(R.id.offline)

        optin!!.setOnCheckedChangeListener(this)
        offline!!.setOnCheckedChangeListener(this)
        cleverTapDefaultInstance = (this.application as PushTemplateHandler).ctInstance
        //checkLocationPermission()
        initializeCleverTapSDK()
        initializeNativeDisplay()
        initialiseAppInBox()
        setPushTemplateJson()
        setFirebaseInstance()

        //setMultiInstanceEnvironment()
        eventButton!!.setOnClickListener {
            //createEvent()
            updateProfileEmail()
            //createFBEvent()
        }
        profilePushButton!!.setOnClickListener {
            userProfilePush()
        }

        inAppButton!!.setOnClickListener {

            cleverTapDefaultInstance!!.pushEvent("ShowInApp")
            //val billaction: HashMap<String, Any> = HashMap<String, Any>()
            //billaction.put("bill_subscribed", "true")
            //billaction.put("bill_type", "phone")

            //cleverTapDefaultInstance!!.pushEvent("bill_subscribed",billaction)
        }

        pushNotification!!.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("ShowPush")
            Analytics.with(applicationContext)

            Analytics.with(applicationContext).track(
                "SEG-ShowPush",
                Properties().putValue("value", "testValue")
                    .putValue("testDate", Date(System.currentTimeMillis()))
            )
            //cleverTapDefaultInstance!!.pushEvent("DateCheck")
        }
        appInboxButton!!.setOnClickListener {

            cleverTapDefaultInstance!!.showAppInbox()
            cleverTapDefaultInstance!!.allInboxMessages
        }
        getAppInboxMessage!!.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("GetInboxMessage")
            //cleverTapDefaultInstance!!.pushEvent("BoundedEvent1")
        }

        nativeDisplayButton!!.setOnClickListener {
            createVW()
            cleverTapDefaultInstance!!.pushEvent("RequestNativeDisplay")
        }
        pushTemplates!!.setOnClickListener {
            val randomId = randomPushTemplateGenerate()
            val templateId = idList[randomId]
            Log.d("Random Id : ", randomId.toString())
            Log.d("Push Template : ", templateId)
            val templateJson: String = pushTemplateJsons[templateId] as String
            val jObject = JSONObject(templateJson)
            Log.d("Push Template Json String: ", templateJson)
            val pushTemplateEventParams = HashMap<String, Any>()
            pushTemplateEventParams["pt_id"] = templateId
            val s = Gson().toJson(jObject)
            Log.d("Push Template Json GSON String: ",s)
            pushTemplateEventParams["pt_json"] = templateJson
            Log.d("Push Template json from map : ",pushTemplateEventParams["pt_json"].toString())
            //cleverTapDefaultInstance!!.pushEvent("PushTemplates")
            cleverTapDefaultInstance!!.pushEvent("PushTemplates")
        }
        addedToCartButton!!.setOnClickListener {
            try {
                val addedToCartAction: HashMap<String, Any> = HashMap<String, Any>()
                val id = randomProductIdGenerate()
                cartProductList.add(id.toString())
                addedToCartAction["ProductID"] = id
                addedToCartAction["tst"] = "NBC"
                addedToCartAction["ProductImage"] =
                    "https://d35fo82fjcw0y8.cloudfront.net/2018/07/26020307/customer-success-clevertap.jpg"
                addedToCartAction["ProductName"] = randomStringByKotlinRandom()
                cleverTapDefaultInstance!!.pushEvent("AddedToCart", addedToCartAction)
                //updateProfileEmail()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.event_trigger_failed, Toast.LENGTH_SHORT).show()
            }
        }
        chargedButton!!.setOnClickListener {
            val charges = hashMapOf<String, Any>("Quantity" to 4, "Total" to 600)
            val items = arrayListOf(
                hashMapOf<String, Any>("ProductName" to "Shoes", "SubQuantity" to 1, "Rate" to 200),
                hashMapOf<String, Any>("ProductName" to "Watch", "SubQuantity" to 2, "Rate" to 100),
                hashMapOf<String, Any>("ProductName" to "Shirt", "SubQuantity" to 1, "Rate" to 300),
            )
            cleverTapDefaultInstance!!.pushChargedEvent(charges, items)
        }

        addCart!!.setOnClickListener {
            try {
            val notifyMeAction: HashMap<String, Any> = HashMap<String, Any>()
            notifyMeAction["id"] = "Test_yeteh"
            notifyMeAction["userId"] = "6"
            cleverTapDefaultInstance!!.pushEvent("Notification Received",notifyMeAction)
            //addMultipleProductIds()

            } catch (e: Exception) {

            }
        }

        clearCart!!.setOnClickListener {
            //clearCart()
            startActivity(Intent(applicationContext, InAppActivity::class.java))
            //createEvent()
        }

        notifyMe!!.setOnClickListener {

            try {
                val notifyMeAction: HashMap<String, Any> = HashMap<String, Any>()
                notifyMeAction["ProductID"] = "P23"
                notifyMeAction["ProductImage"] = "https://d35fo82fjcw0y8.cloudfront.net/2018/07/26020307/customer-success-clevertap.jpg"
                notifyMeAction["ProductName"] = "Soy Milk"
                notifyMeAction["Category"] = "Milk"
                notifyMeAction["Rate"] = 78
                notifyMeAction["OrderDate"] = Date()
                cleverTapDefaultInstance!!.pushEvent("Notify Me", notifyMeAction)
            } catch (e: Exception) {
                Toast.makeText(this, R.string.event_trigger_failed, Toast.LENGTH_SHORT).show()
            }
        }
        startHandler()
    }

    private fun startHandler() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            checkAndRequestPushPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        //initialiseGeofenceSDK()

    }

    @SuppressLint("RestrictedApi")
    private fun checkAndRequestPushPermission() {

                if (null != cleverTapDefaultInstance) {
                    if (cleverTapDefaultInstance!!.isPushPermissionGranted) {
                        setupPushNotifications()
                    } else {
                        val builder = CTLocalInApp.builder()
                            .setInAppType(CTLocalInApp.InAppType.ALERT)
                            .setTitleText("Get Notified")
                            .setMessageText("Enable Notification permission")
                            .followDeviceOrientation(true)
                            .setPositiveBtnText("Allow")
                            .setNegativeBtnText("Cancel")
                            .setFallbackToSettings(true)
                            .build()
                        cleverTapDefaultInstance!!.promptPushPrimer(builder)
                        /*val jsonObject = CTLocalInApp.builder()
                            .setInAppType(InAppType.HALF_INTERSTITIAL)
                            .setTitleText("Get Notified")
                            .setMessageText("Please enable notifications on your device to use Push Notifications.")
                            .followDeviceOrientation(true)
                            .setPositiveBtnText("Allow")
                            .setNegativeBtnText("Cancel")
                            .setBackgroundColor(Constants.WHITE)
                            .setBtnBorderColor(Constants.BLUE)
                            .setTitleTextColor(Constants.BLUE)
                            .setMessageTextColor(Constants.BLACK)
                            .setBtnTextColor(Constants.WHITE)
                            .setImageUrl("https://icons.iconarchive.com/icons/treetog/junior/64/camera-icon.png")
                            .setBtnBackgroundColor(Constants.BLUE)
                            .build()
                        cleverTapDefaultInstance!!.promptPushPrimer(jsonObject)*/
                    }
                }

    }

    private fun setFirebaseInstance() {
        try {
            defaultFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            defaultFirebaseAnalytics!!.setUserProperty("ct_objectId",
                Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this))?.cleverTapID
            );
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Firebase Initialisation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createFBEvent() {

        try {
            val parameters = Bundle()
            parameters.putString("product", "test")
            parameters.putString("rate", "78")

            defaultFirebaseAnalytics!!.logEvent("TestEvent", parameters)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun createVW() {

        try {
            val notifyMeAction: HashMap<String, Any> = HashMap<String, Any>()
            notifyMeAction["VideoName"] = "Cornered Tigers: The 1992 Story - E01"
            notifyMeAction["VideoID"] = "CT000009"

            cleverTapDefaultInstance!!.pushEvent("Video Watched 2", notifyMeAction)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun setMultiInstanceEnvironment() {
        try {
            val clevertapAdditionalInstanceConfig = CleverTapInstanceConfig.createInstance(
                this,
                "TEST-5WK-87Z-666Z",
                "TEST-1ab-305"
            )
            clevertapAdditionalInstanceConfig.setDebugLevel(CleverTapAPI.LogLevel.DEBUG); // default is CleverTapAPI.LogLevel.INFO

            clevertapAdditionalInstanceConfig.isAnalyticsOnly =
                true; // disables the user engagement features of the instance, default is false

            clevertapAdditionalInstanceConfig.useGoogleAdId(false); // enables the collection of the Google ADID by the instance, default is false

            clevertapAdditionalInstanceConfig.enablePersonalization(false); //enables personalization, default is true.

            val clevertapAdditionalInstance: CleverTapAPI =
                CleverTapAPI.instanceWithConfig(this, clevertapAdditionalInstanceConfig)
        } catch (e: java.lang.Exception) {
            Log.d("Exception : ", e.message!!)
        }

    }

    private fun sendBoundedEvents() {
        cleverTapDefaultInstance!!.pushEvent("BoundedEvent2")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEvent2")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEventQualify")
    }

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun randomStringByKotlinRandom() = (1..6)
        .map { kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")

    private fun setPushTemplateJson() {
        try {
            idList.clear()
            idList.add("pt_basic")
            idList.add("pt_zero_bezel")
            idList.add("pt_carousel")
            idList.add("pt_product_display")
            idList.add("pt_input")
            idList.add("pt_timer")

            pushTemplateJsons.clear()
            pushTemplateJsons["pt_basic"] =
                "{\"pt_title\":\"Title\",\"pt_msg\":\"Message\",\"pt_msg_summary\":\"Message line when Notification is expanded\",\"pt_bg\":\"#FFFF00\"}"
            pushTemplateJsons["pt_zero_bezel"] =
                "{\"pt_title\":\"ㅤ\",\"pt_msg\":\"ㅤ\",\"pt_big_img\":\"https://picsum.photos/id/237/200/300.jpg\"}"
            pushTemplateJsons["pt_carousel"] =
                "{\"pt_title\":\"Hello There\",\"pt_msg\":\"This is a test Push\",\"pt_dl1\":\"https://www.google.com/\",\"pt_img1\":\"https://picsum.photos/id/233/400/200.jpg\",\"pt_img2\":\"https://picsum.photos/id/234/400/200.jpg\",\"pt_img3\":\"https://picsum.photos/id/235/400/200.jpg\",\"pt_bg\":\"#9370DB\"}"
            pushTemplateJsons["pt_timer"] =
                "{\"pt_title\":\"Timer\",\"pt_msg\":\"Timer for 1 HR\",\"pt_title_alt\":\"You just missed it!\",\"pt_msg_alt\":\"You just missed a great offer! But there will me more!\",\"pt_timer_end\":1663678529,\"pt_dl1\":\"https://www.google.com/\",\"pt_bg\":\"#303030\",\"pt_title_clr\":\"#F0F0F0\",\"pt_msg_clr\":\"#F0F0F0\",\"pt_big_img\":\"https://picsum.photos/id/237/400/200.jpg\"}"
            pushTemplateJsons["pt_product_display"] =
                "{\"pt_title\":\"Title\",\"pt_msg\":\"Message\",\"pt_img1\":\"https://picsum.photos/id/232/200/200.jpg\",\"pt_img2\":\"https://picsum.photos/id/233/200/200.jpg\",\"pt_img3\":\"https://picsum.photos/id/234/200/200.jpg\",\"pt_bt1\":\"button1\",\"pt_bt2\":\"button2\",\"pt_bt3\":\"button3\",\"pt_st1\":\"smalltext1\",\"pt_st2\":\"smalltext2\",\"pt_st3\":\"smalltext3\",\"pt_dl1\":\"https://www.google.com/\",\"pt_dl2\":\"https://www.yotube.com/\",\"pt_dl3\":\"https://www.google.com/\",\"pt_price1\":\"499\",\"pt_price2\":\"599\",\"pt_price3\":\"399\",\"pt_bg\":\"#FFFF00\",\"pt_product_display_action\":\"Hello world\",\"pt_product_display_action_clr\":\"#00FFFF\"}"
            pushTemplateJsons["pt_input"] =
                "{\"pt_title\":\"Sample title\",\"pt_msg\":\"Sample msg\",\"pt_big_img\":\"https://upload.wikimedia.org/wikipedia/commons/4/43/Aspect_ratio_4_3_example.jpg\",\"pt_input_label\":\"Sample input label\",\"pt_input_feedback\":\"Sample feedback\",\"pt_dl1\":\"deeplink1\",\"pt_event_name\":\"DATA\",\"pt_event_property_data\":\"pt_input_reply\"}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun randomPushTemplateGenerate(): Int {
        return try {
            val randomId = (0..5).random()
            randomId
        } catch (e: Exception) {
            0
        }
    }

    private fun randomProductIdGenerate(): Int {
        return try {
            val randomId = (1..70).random()
            randomId
        } catch (e: Exception) {
            1
        }
    }

    private fun initialiseAppInBox() {
        try {
            cleverTapDefaultInstance!!.ctNotificationInboxListener = this;
            cleverTapDefaultInstance!!.initializeInbox();
        } catch (e: Exception) {

        }

    }

    private fun initializeNativeDisplay() {
        try {
            cleverTapDefaultInstance?.apply {
                setDisplayUnitListener(this@MainActivity)
            }
        } catch (e: Exception) {

        }
    }

    private fun setupPushNotifications() {
        try {
            if (cleverTapDefaultInstance != null) {
                CleverTapAPI.createNotificationChannelGroup(
                    applicationContext,
                    "1234",
                    "CleverTapPush"
                )
                CleverTapAPI.createNotificationChannel(
                    applicationContext,
                    "ch111",
                    "CT-Push",
                    "Test-Notifications",
                    NotificationManager.IMPORTANCE_MAX,
                    "1234",
                    true
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.channel_not_created, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeCleverTapSDK() {

        try {

            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
            cleverTapDefaultInstance!!.enableDeviceNetworkInfoReporting(true);
            cleverTapDefaultInstance!!.setInAppNotificationButtonListener(this);
            cleverTapDefaultInstance!!.enablePersonalization()
            cleverTapDefaultInstance!!.registerPushPermissionNotificationResponseListener(this)
            TemplateRenderer.debugLevel = 3

        } catch (e: Exception) {
            Toast.makeText(this, R.string.sdk_not_initialized, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initialiseGeofenceSDK() {
        try {
            var ctGeofenceSettings = CTGeofenceSettings.Builder()
                .enableBackgroundLocationUpdates(true)//boolean to enable background location updates
                .setLogLevel(Logger.VERBOSE)//Log Level
                .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)//byte value for Location Accuracy
                .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)//byte value for Fetch Mode
                .setGeofenceMonitoringCount(100)//int value for number of Geofences CleverTap can monitor
                .setInterval(10000)//long value for interval in milliseconds
                .setFastestInterval(5000)//long value for fastest interval in milliseconds
                .setSmallestDisplacement(0.3f)//float value for smallest Displacement in meters
                .setGeofenceNotificationResponsiveness(5000)// int value for geofence notification responsiveness in milliseconds
                .build()

            CTGeofenceAPI.getInstance(this).init(ctGeofenceSettings, cleverTapDefaultInstance!!)

            CTGeofenceAPI.getInstance(this)
                .setOnGeofenceApiInitializedListener {
                    Log.d("setOnGeofenceApiInitializedListener", "Geofence API initialised")
                }

            try {
                CTGeofenceAPI.getInstance(this).triggerLocation()
            } catch (e: Exception) {
                Log.e("clevertap Exception.triggerLocation", "=$e")
            }

            CTGeofenceAPI.getInstance(this).setCtLocationUpdatesListener {
                if(it != null) {
                    Log.d("Location updated", "" + it.latitude + " and " + it.longitude)
                }

            }


            CTGeofenceAPI.getInstance(this).setCtGeofenceEventsListener(object : CTGeofenceEventsListener{
                override fun onGeofenceEnteredEvent(geofenceEnteredEventProperties: JSONObject?) {
                    Log.d("Entered Geofence",geofenceEnteredEventProperties.toString())
                }

                override fun onGeofenceExitedEvent(geofenceExitedEventProperties: JSONObject?) {
                    Log.d("Exited Geofence",geofenceExitedEventProperties.toString())
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createEvent() {
        try {
            val prodViewedAction: HashMap<String, Any> = HashMap<String, Any>()
            prodViewedAction["Prod ID"] = 1
            cleverTapDefaultInstance!!.pushEvent("DateCheck", prodViewedAction)
            Toast.makeText(
                applicationContext,
                cleverTapDefaultInstance!!.getProperty("Customer Type") as String,
                Toast.LENGTH_SHORT
            ).show()
            //startActivity( Intent(applicationContext,InAppActivity::class.java))
            //updateProfileEmail()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.event_trigger_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileEmail() {
        try {
            val profileUpdate = HashMap<String, Any>()
            profileUpdate["Name"] = "Jim"
            profileUpdate["Identity"] = edtid!!.text.toString()
            profileUpdate["Email"] = edtemail!!.text.toString()
            profileUpdate["Gender"] = "M"
            profileUpdate["DOB"] = Date()
            profileUpdate["Phone"] = edtphone!!.text.toString()
            val location: Location = Location("GPS")
            //location.latitude = 19.155148
            //location.longitude = 72.867851
            //cleverTapDefaultInstance!!.location = location
            profileUpdate["MSG-whatsapp"] = true
            profileUpdate["MSG-push"] = true
            profileUpdate["MSG-sms"] = true
            profileUpdate["MSG-email"] = true
            cleverTapDefaultInstance!!.onUserLogin(profileUpdate)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show()
        }

    }

    private fun userProfilePush() {
        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Customer Type"] = "SILVER"
        profileUpdate["uptest"] = "Yes"
        profileUpdate["Preferred Language"] = "ENGLISH"
        profileUpdate["17thmay_payment_link"] = "https://eu.adyen.link/PL4DBE2F5FD58A2CEF"
        profileUpdate["may17th_payment_link"] = "https://eu.adyen.link/PL4DBE2F5FD58A2CEF"
        profileUpdate["Email"] = "aditya.waghdhare@clevertap.com"
        profileUpdate["region"] = "India"
        cleverTapDefaultInstance!!.pushProfile(profileUpdate)
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        try {
            Log.d("ButtonClick", payload.toString())
            Toast.makeText(this, "Dismiss", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }

    override fun inboxDidInitialize() {
        try {
            Toast.makeText(this, "Called inboxDidInitialize", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {

        }
    }

    override fun inboxMessagesDidUpdate() {
        try {
            Toast.makeText(this, "Called inboxMessagesDidUpdate", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {

        }

    }

    override fun onInboxButtonClick(payload: HashMap<String, String>?) {

        Log.d("Clicked payload ", payload.toString())
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        try {
            for (i in 0 until units!!.size) {
                val unit = units.get(i)
                prepareDisplayView(unit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {

        for (i in unit.contents) {
            titlem!!.text = i.title
            msg!!.text = i.message

            downloadOnlineImageForImageview(i.media, imageMain!!)
            downloadOnlineImageForImageview(i.icon, imageIcon!!)
            //Notification Viewed Event
            CleverTapAPI.getDefaultInstance(this)!!.pushDisplayUnitViewedEventForID(unit.unitID)
            //Notification Clicked Event
            card_basic!!.setOnClickListener(View.OnClickListener {
                CleverTapAPI.getDefaultInstance(
                    applicationContext
                )!!.pushDisplayUnitClickedEventForID(unit.unitID)
            })
        }

        for (i in unit.customExtras) {
            if (i.key.equals("Bgimage")) {
                ll?.let { downloadOnlineImage(i.value, it) }
            }
            if (i.key.equals("cardone")) {
                val carddata = JSONObject(i.value)
                ll?.let { downloadOnlineImage(carddata.getString("Bgimage"), it) }
            }
        }
    }

    private fun downloadOnlineImageForImageview(url: String, imageV: AppCompatImageView) {
        executor.execute {

            val imageURL = url
            try {
                val `in` = java.net.URL(imageURL).openStream()
                val imageOnline: Bitmap = BitmapFactory.decodeStream(`in`)

                handler.post {
                    imageV.setImageBitmap(imageOnline)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadOnlineImage(url: String, imageV: View) {
        executor.execute {

            // Image URL
            val imageURL = url
            try {
                val `in` = java.net.URL(imageURL).openStream()
                imageOnlineBm = BitmapFactory.decodeStream(`in`)
                handler.post {
                    val d: Drawable = BitmapDrawable(resources, imageOnlineBm)
                    ll!!.background = d
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addMultipleProductIds() {
        cleverTapDefaultInstance!!.addMultiValueForKey(
            "CTS",
            randomProductIdGenerate().toString()
        )
        //cleverTapDefaultInstance!!.addMultiValuesForKey("cart_product_ids",cartProductList)
        //cleverTapDefaultInstance!!.addMultiValueForKey("cart_product_ids", "P01112")
    }

    private fun clearCart() {
        var arr: ArrayList<String> = arrayListOf()
        var list: JSONArray =
            cleverTapDefaultInstance!!.getProperty("cart_product_ids") as JSONArray
        for (l in 0 until list.length()) {
            cleverTapDefaultInstance!!.removeMultiValueForKey(
                "cart_product_ids",
                list.get(l) as String
            )
        }
        //cleverTapDefaultInstance!!.removeMultiValuesForKey("cart_product_ids",arr.addAll(list.))
        Log.d("cart_product_ids---", list.toString())
    }

    /*fun convertToDate(format: String): Date? {
        try {
            fun String.convertToDate(format: String): Date? {
                if (this.isEmpty())
                    return null
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
                val date = formatter.format(LocalDate.parse(this, DateTimeFormatter.ofPattern(format)))
                val dateTime = LocalDate.parse(date, formatter).atStartOfDay(ZoneOffset.UTC).toInstant()
                return Date.from(dateTime)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }*/

    fun convertToDate(format: String, currdate: String): Date? {
        if (currdate.isEmpty())
            return null
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
        val date = formatter.format(LocalDate.parse(currdate, DateTimeFormatter.ofPattern(format)))
        val dateTime = LocalDate.parse(date, formatter).atStartOfDay(ZoneOffset.UTC).toInstant()
        return Date.from(dateTime)
    }


    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.setOptout -> cleverTapDefaultInstance!!.setOptOut(isChecked)
            R.id.offline -> cleverTapDefaultInstance!!.setOffline(isChecked)
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d("MainActivity- onPause", "In-App rendered")
        //CTGeofenceAPI.getInstance(applicationContext).deactivate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            Log.d("MainActivityNewIntent", "-------------------"+intent.dataString)
            if ("com.ge.copycoupon" == intent.action) {
                val message = intent.getStringExtra("coupon")
                if (message != null) {
                    textCopy(this,message)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Button clicked, but no message!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }


    override fun onLocationUpdates(location: Location?) {
        try {
            Log.d(
                "Location Updated : ",
                location!!.latitude.toString() + " , " + location!!.longitude.toString()
            )

        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }


    override fun onGeofenceEnteredEvent(geofenceEnteredEventProperties: JSONObject?) {
        try {
            Log.d(
                "Geofence Entered",
                geofenceEnteredEventProperties!!.getString("gcName") + " : " + geofenceEnteredEventProperties!!.getString(
                    "triggered_lat"
                ) + " , " + geofenceEnteredEventProperties!!.getString("triggered_lng")
            )
        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }

    override fun onGeofenceExitedEvent(geofenceExitedEventProperties: JSONObject?) {
        try {
            Log.d(
                "Geofence Entered",
                geofenceExitedEventProperties!!.getString("gcName") + " : " + geofenceExitedEventProperties!!.getString(
                    "triggered_lat"
                ) + " , " + geofenceExitedEventProperties!!.getString("triggered_lng")
            )
        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }

    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }


    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        checkBackgroundLocation()
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this,
                            "Granted Background Location Permission",
                            Toast.LENGTH_LONG
                        ).show()

                        initialiseGeofenceSDK()
                    }
                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return

            }
        }
    }


    override fun OnGeofenceApiInitialized() {
        try {
            Log.d(
                "clevertap OnGeofenceApiInitialized-",
                "-----OnGeofenceApiInitialized----="
            )
        } catch (e: Exception) {

        }
    }

    override fun onInboxItemClicked(
        message: CTInboxMessage?,
        contentPageIndex: Int,
        buttonIndex: Int
    ) {
        Log.d("--------Message-------",message.toString())
    }

    override fun onPushPermissionResponse(accepted: Boolean) {

        if (accepted) {
            setupPushNotifications()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleverTapDefaultInstance?.unregisterPushPermissionNotificationResponseListener(this)
    }

    override fun inAppNotificationDidClick(
        inAppNotification: CTInAppNotification?,
        formData: Bundle?,
        keyValueMap: java.util.HashMap<String, String>?
    ) {
        Log.d("---- inAppNotificationDidClick ----","")
    }

    override fun inAppNotificationDidDismiss(
        context: Context?,
        inAppNotification: CTInAppNotification?,
        formData: Bundle?
    ) {
        Log.d("---- inAppNotificationDidDismiss ----","")
    }

    override fun inAppNotificationDidShow(
        inAppNotification: CTInAppNotification?,
        formData: Bundle?
    ) {
        Log.d("---- inAppNotificationDidShow ----","")
    }

    override fun beforeShow(extras: MutableMap<String, Any>?): Boolean {
        Log.d("beforeShow", "Inside")
        return true
    }

    override fun onShow(ctInAppNotification: CTInAppNotification?) {
        Log.d("onShow", "Inside")
    }

    override fun onDismissed(
        extras: MutableMap<String, Any>?,
        actionExtras: MutableMap<String, Any>?
    ) {
        Log.d("onDismissed", extras.toString())
        Log.d("onDismissed", actionExtras.toString())
    }


    fun clearSPS()
    {
        val preferences = applicationContext.getSharedPreferences("WizRocket", MODE_PRIVATE)  ?: return
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        CleverTapAPI.setInstances(null)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        Log.e("app", Gson().toJson(cleverTapDefaultInstance?.getCleverTapID {
            Log.e(
                "app",
                cleverTapDefaultInstance?.cleverTapID.toString()
            )
        }))
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

}