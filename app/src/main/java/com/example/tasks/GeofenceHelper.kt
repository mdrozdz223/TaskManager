package com.example.tasks

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.tasks.model.Task
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceHelper(private val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(task: Task) {
        if (task.latitude == null || task.longitude == null) return

        val geofence = Geofence.Builder()
            .setRequestId(task.title)
            .setCircularRegion(task.latitude, task.longitude, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
    }

    fun removeGeofence(taskTitle: String) {
        geofencingClient.removeGeofences(listOf(taskTitle))
    }
}