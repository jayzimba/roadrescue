package com.jayjaycode.miniproject

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jayjaycode.miniproject.data.FcmTokenRepository
import com.jayjaycode.miniproject.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RoadRescueMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            runCatching { FcmTokenRepository.instance.registerCurrentToken() }
                .onFailure { Log.w(TAG, "Failed to save FCM token", it) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return
        val type = message.data["type"] ?: "bid"
        val requestId = message.data["requestId"]
        val orderId = message.data["orderId"]
        val bookingId = message.data["bookingId"]
        val channelId = message.data["channelId"]
            ?: if (type.contains("order") || type.contains("booking")) {
                NotificationHelper.CHANNEL_ORDERS
            } else {
                NotificationHelper.CHANNEL_BIDS
            }

        val notificationId = (requestId ?: orderId ?: bookingId ?: body).hashCode()

        NotificationHelper.showPushNotification(
            context = applicationContext,
            notificationId = notificationId,
            title = title,
            body = body,
            type = type,
            channelId = channelId,
            requestId = requestId,
            orderId = orderId,
            bookingId = bookingId,
        )
    }

    companion object {
        private const val TAG = "RoadRescueFCM"
    }
}
