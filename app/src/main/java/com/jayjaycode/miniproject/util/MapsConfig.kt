package com.jayjaycode.miniproject.util

import android.content.Context
import android.content.pm.PackageManager

object MapsConfig {
    fun getApiKey(context: Context): String {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
        )
        return appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
    }
}
