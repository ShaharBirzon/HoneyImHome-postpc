package com.example.honeyimhome

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.TimeUnit


class myApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val broadcastReceiver = LocalSendSmsBroadcastReceiver(this) //todo check context
        registerReceiver(broadcastReceiver, IntentFilter("send_sms"))

        val smsRequest = PeriodicWorkRequest.Builder(
            WorkerTracker::class.java, 15,
            TimeUnit.MINUTES
        ).build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(smsRequest)
    }
}

//class CustomSyncWorker(var context: Context, workerParams: WorkerParameters) :
//    Worker(context, workerParams) {
//    private val sp = HomeSharedPreferences()
//
//    override fun doWork(): Result {
//        Log.i("doWork", "in work")
//        if (ActivityCompat.checkSelfPermission(
//                context, "android.permission.SEND_SMS"
//            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                context,
//                "android.permission.ACCESS_FINE_LOCATION"
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return Result.success()
//        }
//        if (sp.getHomeLocationFromMyPref(context) == null || sp.getPhoneNumberFromMyPref(context) == null ||
//            sp.getPhoneNumberFromMyPref(context) == ""
//        ) {
//            return Result.success()
//        }
//
//        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val locationTracker = LocationTracker(context, locationManager)
//        locationTracker.startTracking()
//
//        if (locationTracker.curLocationInfo.accuracy < 50){
//            val prev = sp.getCurrentLocationFromMyPref(context)
//            val cur = locationTracker.curLocationInfo
//            val ans:FloatArray = floatArrayOf(0f)
//            Location.distanceBetween(prev?.latitude ?: 0.0, prev?.longitude ?: 0.0, cur.latitude, cur.longitude, ans)
//            if (prev == null || ans[0] <= 50){
//                sp.saveCurrentLocationToMyPref(context, cur)
//                return Result.success()
//            }
//            else if (ans[0] > 50){
//                val homeLoc = sp.getHomeLocationFromMyPref(context)
//                Location.distanceBetween(homeLoc?.latitude ?: 0.0, homeLoc?.longitude ?: 0.0, cur.latitude, cur.longitude, ans)
//                if (ans[0] < 50){
//                    val intent = Intent("send_sms")
//                    intent.putExtra("phone", sp.getPhoneNumberFromMyPref(context))
//                    intent.putExtra("content", "Honey I'm Home!")
//                    context.sendBroadcast(intent)
//                    sp.saveCurrentLocationToMyPref(context, cur)
//                    return Result.success()
//                }
//            }
//
//        }
//        return Result.Failure()
//    }
//}

class WorkerTracker(private val context: Context, workerParams: WorkerParameters) :
    ListenableWorker(context, workerParams) {
    private var phoneNumber: String? = ""
    private var homeLocation: LocationInfo? = null
    private var locationTracker : LocationTracker? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var callback: Completer<Result>? = null
    private val sp = HomeSharedPreferences()

    override fun startWork(): ListenableFuture<Result> {
        Log.i("doWork", "in work")
        val future =
            CallbackToFutureAdapter.getFuture<Result> { completer ->
                callback = completer
                null
            }
        placeReceiver()
        if (ActivityCompat.checkSelfPermission(
                context, "android.permission.SEND_SMS"
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback!!.set(Result.success())
        }

        homeLocation = sp.getHomeLocationFromMyPref(context)
        phoneNumber = sp.getPhoneNumberFromMyPref(context)

        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationTracker = LocationTracker(context, locationManager)


        if (phoneNumber == "" || phoneNumber == null || homeLocation == null) {
            callback!!.set(Result.success())
        } else {
            locationTracker?.startTracking()
        }
        return future
    }

    private fun placeReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                if (intent.getBooleanExtra("ToWorker", false)) {
                    val locationInfo: LocationInfo? =
                        locationTracker?.curLocationInfo
                    if (locationInfo?.accuracy!! < 50) {
                        onReceivedBroadcast()
                    }
                }
            }
        }
        context.registerReceiver(broadcastReceiver, IntentFilter("got_location"))
    }

    private fun onReceivedBroadcast() {
        context.unregisterReceiver(broadcastReceiver)
        val currentLocation: LocationInfo =
            locationTracker?.curLocationInfo!!
        val cur_lat = currentLocation.latitude
        val cur_long = currentLocation.longitude
        locationTracker?.stopTracking()
        val prevLocation: LocationInfo? = sp.getCurrentLocationFromMyPref(context)

        if (prevLocation == null) {
            sp.saveCurrentLocationToMyPref(context, currentLocation)
            callback!!.set(Result.success())
            return
        }
        val prev_lat = prevLocation.latitude
        val prev_lon = prevLocation.longitude
        val res = FloatArray(1)
        Location.distanceBetween(prev_lat, prev_lon, cur_lat, cur_long, res)
        sp.saveCurrentLocationToMyPref(context, currentLocation)
        if (prev_lon == -1.0 || res[0] < 50) {
            callback!!.set(Result.success())
            return
        }
        val home_lat = homeLocation!!.latitude
        val home_lon = homeLocation!!.longitude
        Location.distanceBetween(cur_lat, cur_long, home_lat, home_lon, res)
        if (res[0] < 50) {
            val intent = Intent("send_sms")
            intent.putExtra("phone", phoneNumber)
            intent.putExtra("content", "Honey I'm home!")
            context.sendBroadcast(intent)
        }
        callback!!.set(Result.success())
    }

}