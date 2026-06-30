package com.example.tasks

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) return

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

            for (geofence in triggeringGeofences) {
                val taskTitle = geofence.requestId
                showNotification(context, taskTitle)
            }
        }
    }

    private fun showNotification(context: Context, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminders"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Jesteś w pobliżu zadania!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}