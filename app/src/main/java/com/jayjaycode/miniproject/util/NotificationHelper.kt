package com.jayjaycode.miniproject.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jayjaycode.miniproject.MainActivity
import com.jayjaycode.miniproject.R

object NotificationHelper {

    const val CHANNEL_BIDS = "bids"
    const val CHANNEL_ORDERS = "orders"

    const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    const val EXTRA_REQUEST_ID = "request_id"
    const val EXTRA_ORDER_ID = "order_id"
    const val EXTRA_BOOKING_ID = "booking_id"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BIDS,
                "Bids & rescue jobs",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "New bids, accepted jobs, and open rescue requests"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ORDERS,
                "Orders & bookings",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Part orders and service booking updates"
            },
        )
    }

    fun showPushNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        type: String,
        channelId: String,
        requestId: String? = null,
        orderId: String? = null,
        bookingId: String? = null,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            requestId?.let { putExtra(EXTRA_REQUEST_ID, it) }
            orderId?.let { putExtra(EXTRA_ORDER_ID, it) }
            bookingId?.let { putExtra(EXTRA_BOOKING_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
