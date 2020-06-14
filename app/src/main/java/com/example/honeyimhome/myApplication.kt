package com.example.honeyimhome

import android.app.Application
import android.content.Context
import android.content.IntentFilter

class myApplication: Application() {

//    companion object{
//        var mainContext : Context? = null
//    }

    override fun onCreate() {
        super.onCreate()
        val broadcastReceiver = LocalSendSmsBroadcastReceiver(this) //todo check context
        registerReceiver(broadcastReceiver, IntentFilter("send_sms"))
    }
}