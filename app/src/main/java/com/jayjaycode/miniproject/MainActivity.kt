package com.jayjaycode.miniproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jayjaycode.miniproject.ui.RoadRescueRoot
import com.jayjaycode.miniproject.ui.theme.RoadRescueTheme
import com.jayjaycode.miniproject.util.NotificationDeepLink
import com.jayjaycode.miniproject.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private var notificationDeepLink by mutableStateOf<NotificationDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationDeepLink = readNotificationDeepLink(intent)
        setContent {
            RoadRescueTheme {
                RoadRescueRoot(
                    notificationDeepLink = notificationDeepLink,
                    onNotificationHandled = { notificationDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationDeepLink = readNotificationDeepLink(intent)
    }

    private fun readNotificationDeepLink(intent: Intent?): NotificationDeepLink? {
        val type = intent?.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE) ?: return null
        val requestId = intent.getStringExtra(NotificationHelper.EXTRA_REQUEST_ID)
        val orderId = intent.getStringExtra(NotificationHelper.EXTRA_ORDER_ID)
        val bookingId = intent.getStringExtra(NotificationHelper.EXTRA_BOOKING_ID)
        val hasTarget = requestId != null || orderId != null || bookingId != null
        val dashboardOnly = type in setOf("open_job", "new_part_order", "new_service_booking", "bid_lost")
        if (!hasTarget && !dashboardOnly) return null
        return NotificationDeepLink(
            type = type,
            requestId = requestId,
            orderId = orderId,
            bookingId = bookingId,
        )
    }
}
