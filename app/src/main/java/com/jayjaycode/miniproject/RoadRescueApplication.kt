package com.jayjaycode.miniproject

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.jayjaycode.miniproject.util.MapsConfig
import com.jayjaycode.miniproject.util.NotificationHelper

class RoadRescueApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        FirebaseApp.initializeApp(this)
        val mapsApiKey = MapsConfig.getApiKey(this)
        if (mapsApiKey.isNotBlank() && !Places.isInitialized()) {
            Places.initialize(applicationContext, mapsApiKey)
        }
    }
}
