package com.eventlocator.eventlocatororganizers.utilities

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = intent!!.getIntExtra("notificationID", 0)
        val notification = intent.getParcelableExtra<Notification>("notification")
        if (notification!=null){
            notificationManager.notify(notificationID,notification)
        }
    }
}