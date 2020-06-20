package com.example.honeyimhome

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class HomeSharedPreferences {
    fun saveHomeLocationToMyPref(
        context: Context,
        locationInfo: LocationInfo?
    ) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json: String = gson.toJson(locationInfo)
        editor.putString("homeLocation", json)
        editor.apply()
    }

    fun saveCurrentLocationToMyPref(
        context: Context,
        locationInfo: LocationInfo?
    ) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json: String = gson.toJson(locationInfo)
        editor.putString("curLocation", json)
        editor.apply()
    }

    fun savePhoneNumberToMYPref(context: Context, phone: String){
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString("phone", phone).apply()
    }

    fun getPhoneNumberFromMyPref(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        return sharedPreferences.getString("phone", "")

    }

    fun getHomeLocationFromMyPref(context: Context): LocationInfo? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        val gson = Gson()
        val json = sharedPreferences.getString("homeLocation", null) ?: return null
        val type: Type = object : TypeToken<LocationInfo?>() {}.type
        return gson.fromJson(json, type)
    }

    fun getCurrentLocationFromMyPref(context: Context): LocationInfo? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "shared preferences",
            MODE_PRIVATE
        )
        val gson = Gson()
        val json = sharedPreferences.getString("curLocation", null) ?: return null
        val type: Type = object : TypeToken<LocationInfo?>() {}.type
        return gson.fromJson(json, type)
    }

}