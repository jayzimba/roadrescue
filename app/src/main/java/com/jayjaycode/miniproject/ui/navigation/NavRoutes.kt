package com.jayjaycode.miniproject.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val REQUEST_FORM = "request_form/{type}"
    const val BIDDING = "bidding"
    const val ACTIVE_JOB = "active_job"
    const val MARKETPLACE = "marketplace"
    const val SERVICE_BOOKING = "service_booking"
    const val REQUEST_HISTORY = "request_history"

    fun requestForm(type: String) = "request_form/$type"
}
