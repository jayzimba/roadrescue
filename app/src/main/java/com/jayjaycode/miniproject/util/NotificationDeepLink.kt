package com.jayjaycode.miniproject.util

data class NotificationDeepLink(
    val type: String,
    val requestId: String? = null,
    val orderId: String? = null,
    val bookingId: String? = null,
)
