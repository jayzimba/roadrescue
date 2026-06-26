package com.jayjaycode.miniproject

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.jayjaycode.miniproject.util.MapsConfig

class RoadRescueApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val mapsApiKey = MapsConfig.getApiKey(this)
        if (mapsApiKey.isNotBlank() && !Places.isInitialized()) {
            Places.initialize(applicationContext, mapsApiKey)
        }
    }
}
