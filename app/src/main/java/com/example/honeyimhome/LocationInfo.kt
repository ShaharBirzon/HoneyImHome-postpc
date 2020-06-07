package com.example.honeyimhome

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import java.util.jar.Manifest


data class LocationInfo(var latitude: String, var longitude: String, var accuracy: String){}

class LocationTracker(var context: Context, var locationManager: LocationManager){
    val intent = Intent("new_location")
    var curLocationInfo: LocationInfo = LocationInfo("0", "0", "0")
    var locationListener: LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            val longitude: Double = location?.longitude ?: 0.0
            val latitude: Double = location?.latitude ?: 0.0
            val accuracy: Float = location?.accuracy ?: 0f
            curLocationInfo.longitude = longitude.toBigDecimal().toPlainString()
            curLocationInfo.latitude = latitude.toBigDecimal().toPlainString()
            curLocationInfo.accuracy = accuracy.toBigDecimal().toPlainString()


            if(accuracy<50){
                intent.putExtra("accuracy_less_than_50", true)
            }
            else{
                intent.putExtra("accuracy_less_than_50", false)
            }

            context.sendBroadcast(intent)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(provider: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(provider: String?) {
            TODO("Not yet implemented")
        }

    }

    fun startTracking(){
        if (ActivityCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, locationListener);
        }
        val intent = Intent("start")
        context.sendBroadcast(intent)
    }

    fun stopTracking(){
        intent.putExtra("to_stop", true)
//        val intent = Intent("stop")
//        context.sendBroadcast(intent)
    }
}

