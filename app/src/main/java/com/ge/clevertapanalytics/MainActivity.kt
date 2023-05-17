package com.ge.clevertapanalytics

//import com.clevertap.android.pushtemplates.TemplateRenderer

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clevertap.android.pushtemplates.TemplateRenderer
import com.clevertap.android.sdk.*
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.segment.analytics.Analytics
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import com.segment.analytics.Properties;


class MainActivity : AppCompatActivity(), InAppNotificationButtonListener, CTInboxListener,
InboxMessageListener,
    InboxMessageButtonListener, DisplayUnitListener ,CompoundButton.OnCheckedChangeListener,PushPermissionResponseListener{
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
    var bounded: AppCompatButton? = null

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

    var ll : RelativeLayout? = null
    val handler = Handler(Looper.getMainLooper())

    var edtphone : EditText? = null
    var edtemail : EditText? = null
    var edtid : EditText? = null

    var optin : SwitchCompat? = null
    var offline : SwitchCompat? = null

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
        bounded = findViewById(R.id.sendBounded)
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
        cleverTapDefaultInstance?.registerPushPermissionNotificationResponseListener(this)
        getPushPermission()
        initializeCleverTapSDK()
        initializeNativeDisplay()
        initialiseAppInBox()
        //setupPushNotifications()
        setPushTemplateJson()
        //setMultiInstanceEnvironment()
        eventButton!!.setOnClickListener {
            //createEvent()
            updateProfileEmail()
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
                "SEG-ShowPush", Properties().putValue("value", "testValue").putValue("testDate", Date(System.currentTimeMillis()))
            )
            //cleverTapDefaultInstance!!.pushEvent("DateCheck")
        }
        appInboxButton!!.setOnClickListener(View.OnClickListener {

            cleverTapDefaultInstance!!.showAppInbox()
            cleverTapDefaultInstance!!.allInboxMessages
        })
        getAppInboxMessage!!.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("GetInboxMessage")
            //cleverTapDefaultInstance!!.pushEvent("BoundedEvent1")
        }

        nativeDisplayButton!!.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("RequestNativeDisplay")
        }
        pushTemplates!!.setOnClickListener {
            //val randomId = randomPushTemplateGenerate()
            //val templateId = idList[randomId]
            //Log.d("Random Id : ", randomId.toString())
            //Log.d("Push Template : ", templateId)
            //val templateJson: String = pushTemplateJsons[templateId] as String
            //val jObject: JSONObject = JSONObject(templateJson)
            //Log.d("Push Template Json String: ", templateJson)
            //val pushTemplateEventParams = HashMap<String, Any>()
            //pushTemplateEventParams["pt_id"] = templateId
            //val s = Gson().toJson(jObject)
            //Log.d("Push Template Json GSON String: ",s)
            //pushTemplateEventParams["pt_json"] = templateJson
            //Log.d("Push Template json from map : ",pushTemplateEventParams["pt_json"].toString())
            cleverTapDefaultInstance!!.pushEvent("PushTemplates")
        }

        addedToCartButton!!.setOnClickListener {
            try {
                val addedToCartAction: HashMap<String, Any> = HashMap<String, Any>()
                var id = randomProductIdGenerate()
                cartProductList.add(id.toString())
                addedToCartAction.put("ProductID", id)
                addedToCartAction.put(
                    "ProductImage",
                    "https://d35fo82fjcw0y8.cloudfront.net/2018/07/26020307/customer-success-clevertap.jpg"
                )
                addedToCartAction.put("ProductName", randomStringByKotlinRandom())
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
            addMultipleProductIds()
        }

        clearCart!!.setOnClickListener {
            //clearCart()
            startActivity( Intent(applicationContext,InAppActivity::class.java))
            //createEvent()
        }

        bounded!!.setOnClickListener {
            sendBoundedEvents()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun getPushPermission() {
        try {
            if(!cleverTapDefaultInstance!!.isPushPermissionGranted){
                cleverTapDefaultInstance!!.promptForPushPermission(true)
                val builder = CTLocalInApp.builder()
                    .setInAppType(CTLocalInApp.InAppType.HALF_INTERSTITIAL)
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
                    .setBtnBackgroundColor(Constants.BLUE)
                    .build()
                cleverTapDefaultInstance!!.promptPushPrimer(builder)
            }
            else{
                setupPushNotifications()
            }
        }
        catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }


    private fun setMultiInstanceEnvironment() {
        try{
            val clevertapAdditionalInstanceConfig = CleverTapInstanceConfig.createInstance(
                this,
                "TEST-5WK-87Z-666Z",
                "TEST-1ab-305"
            )
            clevertapAdditionalInstanceConfig.setDebugLevel(CleverTapAPI.LogLevel.DEBUG); // default is CleverTapAPI.LogLevel.INFO

            clevertapAdditionalInstanceConfig.isAnalyticsOnly = true; // disables the user engagement features of the instance, default is false

            clevertapAdditionalInstanceConfig.useGoogleAdId(false); // enables the collection of the Google ADID by the instance, default is false

            clevertapAdditionalInstanceConfig.enablePersonalization(false); //enables personalization, default is true.

            val clevertapAdditionalInstance: CleverTapAPI =
                CleverTapAPI.instanceWithConfig(this,clevertapAdditionalInstanceConfig)
        }
        catch (e:java.lang.Exception){
            Log.d("Exception : ",e.message!!)
        }

    }

    private fun sendBoundedEvents() {
        cleverTapDefaultInstance!!.pushEvent("BoundedEvent2")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEvent2")
        //cleverTapDefaultInstance!!.pushEvent("BoundedEventQualify")
    }

    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    fun randomStringByKotlinRandom() = (1..6)
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
        } catch (e: java.lang.Exception) {
            0
        }
    }

    private fun randomProductIdGenerate(): Int {
        return try {
            val randomId = (1..70).random()
            randomId
        } catch (e: java.lang.Exception) {
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
            TemplateRenderer.debugLevel = 3

        } catch (e: Exception) {
            Toast.makeText(this, R.string.sdk_not_initialized, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createEvent() {
        try {
            val prodViewedAction: HashMap<String, Any> = HashMap<String, Any>()
            prodViewedAction.put("Prod ID", 1)

            cleverTapDefaultInstance!!.pushEvent("DateCheck", prodViewedAction)

            Toast.makeText(applicationContext, cleverTapDefaultInstance!!.getProperty("Customer Type") as String, Toast.LENGTH_SHORT).show()
            //startActivity( Intent(applicationContext,InAppActivity::class.java))
            //updateProfileEmail()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.event_trigger_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileEmail() {
        try {
            val profileUpdate = HashMap<String, Any>()
            profileUpdate["Name"] = "Aditya"
            profileUpdate["Identity"] = edtid!!.text.toString()
            profileUpdate["Email"] = edtemail!!.text.toString()
            profileUpdate["Gender"] = "M"
            profileUpdate["DOB"] = Date()
            profileUpdate["Phone"] = edtphone!!.text.toString()
            val location: Location = Location("GPS")
            //location.latitude = 19.155148
            //location.longitude = 72.867851
            //cleverTapDefaultInstance!!.location = location
            profileUpdate.put("MSG-whatsapp", true)
            profileUpdate.put("MSG-push", true)
            profileUpdate.put("MSG-sms", true)
            profileUpdate.put("MSG-email", true)
            cleverTapDefaultInstance!!.onUserLogin(profileUpdate)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show()
        }

    }

    private fun userProfilePush() {
        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Customer Type"] = "Silver"
        profileUpdate["Preferred Language"] = "English"
        //profileUpdate["Email"] = "aditya.waghdhare@clevertap.com"
        profileUpdate["region"] = "India"
        cleverTapDefaultInstance!!.pushProfile(profileUpdate)
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        try {
            Toast.makeText(this, "Dismiss", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {

        }
    }

    override fun inboxDidInitialize() {
        try {
            Toast.makeText(this, "Called inboxDidInitialize", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {

        }
    }

    override fun inboxMessagesDidUpdate() {
        try {
            Toast.makeText(this, "Called inboxMessagesDidUpdate", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {

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

        for(i in unit.customExtras){
            if(i.key.equals("Bgimage")){
                ll?.let { downloadOnlineImage(i.value, it) }
            }
            if(i.key.equals("cardone")){

                val carddata = JSONObject(i.value)
                Toast.makeText(this,carddata.getString("messagebody"),Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadOnlineImageForImageview(url : String, imageV : AppCompatImageView){
        executor.execute {

            // Image URL
            val imageURL = url

            // Tries to get the image and post it in the ImageView
            // with the help of Handler
            try {
                val `in` = java.net.URL(imageURL).openStream()
                var imageOnline : Bitmap = BitmapFactory.decodeStream(`in`)

                // Only for making changes in UI
                handler.post {
                    imageV.setImageBitmap(imageOnline)
                }
            }

            // If the URL does not point to
            // image or any other kind of failure
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun downloadOnlineImage(url : String, imageV : View){
        executor.execute {

            // Image URL
            val imageURL = url

            // Tries to get the image and post it in the ImageView
            // with the help of Handler
            try {
                val `in` = java.net.URL(imageURL).openStream()
                imageOnlineBm = BitmapFactory.decodeStream(`in`)

                // Only for making changes in UI
                handler.post {
                    val d: Drawable = BitmapDrawable(resources, imageOnlineBm)
                    ll!!.background = d
                }
            }

            // If the URL doesnot point to
            // image or any other kind of failure
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addMultipleProductIds() {
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

    /*un convertToDate(format: String): Date? {
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

    fun convertToDate(format: String,currdate : String): Date? {
        if (currdate.isEmpty())
            return null
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
        val date = formatter.format(LocalDate.parse(currdate, DateTimeFormatter.ofPattern(format)))
        val dateTime = LocalDate.parse(date, formatter).atStartOfDay(ZoneOffset.UTC).toInstant()
        return Date.from(dateTime)
    }

    override fun onInboxItemClicked(message: CTInboxMessage?) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.setOptout -> cleverTapDefaultInstance!!.setOptOut(isChecked)
            R.id.offline -> cleverTapDefaultInstance!!.setOffline(isChecked)
        }
    }



    override fun onPause() {
        super.onPause()
        Log.d("MainActivity- onPause","In-App rendered")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.i("MainActivity", "onPushPermissionResponse :  InApp---> response() called accepted=$accepted")
        if (accepted) {
            setupPushNotifications()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleverTapDefaultInstance?.unregisterPushPermissionNotificationResponseListener(this)
    }

}