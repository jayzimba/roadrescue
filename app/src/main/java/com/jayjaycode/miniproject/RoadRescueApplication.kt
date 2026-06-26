package com.jayjaycode.miniproject

import android.app.Application
import com.google.firebase.FirebaseApp

class RoadRescueApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
