package com.example.honeyimhome

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class LocalSendSmsBroadcastReceiver (var context: Context?) : BroadcastReceiver() {
    val PHONE_KEY = "phone"
    val CONTENT_KEY = "content"
    private val channelId = "CHANNEL_ID_FOR_NONIMPORTANT_NOTIFICATIONS"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                "android.permission.SEND_SMS"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("LocalSendSms", "error! does not have sms permissions")
        }
        val phone = intent?.getStringExtra(PHONE_KEY)
        val content = intent?.getStringExtra(CONTENT_KEY)
        if (phone == null || content == null) {
            Log.i("LocalSendSms", "error! can't find phone number or content")
        }

        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phone, null, content, null, null)

    }


    fun fireNotification(msg: String) {
        createChannelIfNotExists()
        actualFire(msg)
    }


    private fun createChannelIfNotExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notificationChannels.forEach { channel ->
                if (channel.id == channelId) {
                    return
                }
            }

            // Create the NotificationChannel
            val name = "non-important"
            val descriptionText = "channel forr non important notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun actualFire(msg: String) {

        val intentToOpenBlue = Intent(context, MainActivity::class.java)
        intentToOpenBlue.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pending = PendingIntent.getActivity(context, 123, intentToOpenBlue, 0)

        var notification: Notification = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .build()


        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(123, notification)

    }
}