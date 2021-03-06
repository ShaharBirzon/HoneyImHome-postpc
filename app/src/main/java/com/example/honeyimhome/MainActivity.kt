package com.example.honeyimhome

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val LOCATION_REQUEST_CODE = 123
    val SMS_REQUEST_CODE = 124
    private var locationTracker: LocationTracker? = null
    private val sp: HomeSharedPreferences = HomeSharedPreferences()
    private var homeLoc: LocationInfo? = null
    var broadcastReceiver: BroadcastReceiver? = null
    private var inputPhone: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val locationManager: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationTracker = LocationTracker(this, locationManager)
        homeLoc = sp.getHomeLocationFromMyPref(this)
        if (homeLoc == null) {
            var homeLoc = tv_home_location
            homeLoc.visibility = View.GONE
            var clearHome = btn_clear_home
            clearHome.visibility = View.GONE
        }
        else{
            setHomeLocTextView(homeLoc)
            btn_clear_home.visibility = View.VISIBLE
        }

        broadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if(action == "new_location"){
                    if(intent.getBooleanExtra("accuracy_less_than_50", false)){
                        btn_set_as_home.visibility = View.VISIBLE
                    }
                    else{
                        btn_set_as_home.visibility = View.GONE
                    }
                    if(!intent.getBooleanExtra("to_stop",false)){
                        setCurrentLocTextViews(locationTracker!!.curLocationInfo)
//                        if (context != null) {
//                            sp.saveCurrentLocationToMyPref(context, locationTracker!!.curLocationInfo)
//                        }
                    }
                }
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter("new_location"))

        inputPhone = sp.getPhoneNumberFromMyPref(this).toString()
        if (inputPhone != ""){
            btn_test_sms.visibility = View.VISIBLE
            et_phone_num.hint = inputPhone
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
        }
    }

    private fun setHomeLocTextView(locInfo: LocationInfo?){
        val homeLocTv : TextView = tv_home_location
        tv_home_location.visibility = View.VISIBLE
        val long = locInfo?.longitude ?: 0.0
        val lat = locInfo?.latitude ?: 0.0
        val acc = locInfo?.accuracy ?: 0f
        val homeLocStr : String =  "Your home location -\nLongitude: $long \n" +
                "Latitude: $lat \n" +
                "Accuracy: $acc"
        homeLocTv.text = homeLocStr
    }


    fun startTrackingOnClick(view: View){
        val hasLocationPermission = ActivityCompat.checkSelfPermission(this,
            "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED

        if(hasLocationPermission){
            val startTrackingButton: Button = btn_start
            val stopTrackingButton: Button = btn_stop
            startTrackingButton.visibility = View.INVISIBLE
            stopTrackingButton.visibility = View.VISIBLE
            locationTracker?.startTracking()
            setCurrentLocTextViews(locationTracker!!.curLocationInfo)

        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf("android.permission.ACCESS_FINE_LOCATION"), LOCATION_REQUEST_CODE)
        }
    }

    fun stopTrackingOnClick(view: View){
        val startTrackingButton: Button = btn_start
        val stopTrackingButton: Button = btn_stop
        startTrackingButton.visibility = View.VISIBLE
        stopTrackingButton.visibility = View.INVISIBLE
        locationTracker?.stopTracking()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(permissions[0] == "android.permission.SEND_SMS"){
                et_phone_num.visibility = View.VISIBLE
                btn_save_phone.visibility = View.VISIBLE
            }
            else{
                locationTracker?.startTracking()
                btn_start.visibility = View.INVISIBLE
                btn_stop.visibility = View.VISIBLE
            }

        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_FINE_LOCATION")){
                val dialog = AlertDialog.Builder(this)
                dialog.setMessage("Sorry, we need your permission to continue")

                dialog.setPositiveButton(
                    "OK"
                ) { dialog, which ->
                }
                val explainDialog = dialog.create()
                explainDialog.show()
            }
        }
    }

    fun setAsHomeOnClick(view: View){
        val curLoc : LocationInfo = locationTracker?.curLocationInfo ?: LocationInfo(0.0,0.0,0f)
        sp.saveHomeLocationToMyPref(this, curLoc)
        setHomeLocTextView(curLoc)
        btn_clear_home.visibility = View.VISIBLE
        tv_home_location.visibility = View.VISIBLE
    }

    fun setCurrentLocTextViews(curLoc: LocationInfo){
        val curLongitude: String = "Longitude: ${curLoc.longitude}"
        tv_longtitude.text = curLongitude
        val curLat: String = "Latitude: ${curLoc.latitude}"
        tv_latitude.text =  curLat
        val curAccuracy: String = "Accuracy: ${curLoc.accuracy}"
        tv_accuracy.text = curAccuracy
    }

    fun clearHomeOnclick(view: View){
        sp.saveHomeLocationToMyPref(this, null)
        btn_clear_home.visibility = View.GONE
        tv_home_location.visibility = View.INVISIBLE
    }

    fun setSmsOnCLick(view: View){
        val hasLocationPermission = ActivityCompat.checkSelfPermission(this,
            "android.permission.SEND_SMS") == PackageManager.PERMISSION_GRANTED

        if(hasLocationPermission){
            et_phone_num.visibility = View.VISIBLE
            btn_save_phone.visibility = View.VISIBLE
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf("android.permission.SEND_SMS"), SMS_REQUEST_CODE)
        }
    }

    fun saveSmsNumOnClick(view: View){
        inputPhone = et_phone_num.text.toString()
        sp.savePhoneNumberToMYPref(this, inputPhone)
        et_phone_num.visibility = View.GONE
        btn_save_phone.visibility = View.GONE
        Toast.makeText(this, "phone number saved", Toast.LENGTH_SHORT).show()
        if (inputPhone != ""){
            btn_test_sms.visibility = View.VISIBLE
        }
    }

    fun testSmsOnClick(view: View){
        var intent = Intent("send_sms")
        intent.putExtra("phone", inputPhone)
        intent.putExtra("content", "Honey I'm Sending a Test Message!")
        sendBroadcast(intent)
    }


}
