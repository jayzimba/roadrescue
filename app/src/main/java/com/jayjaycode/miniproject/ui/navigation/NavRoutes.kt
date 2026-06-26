package com.jayjaycode.miniproject.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val REQUEST_FORM = "request_form/{type}"
    const val BIDDING = "bidding"
    const val ACTIVE_JOB = "active_job"
    const val MARKETPLACE = "marketplace"
    const val SERVICE_BOOKING = "service_booking"
    const val REQUEST_HISTORY = "request_history"
    const val REQUEST_DETAIL = "request_detail/{requestId}"
    const val PROFILE = "profile"
    const val REGISTER_BUSINESS = "register_business"
    const val PROVIDER_DASHBOARD = "provider_dashboard"
    const val ADD_PART_LISTING = "add_part_listing"
    const val ADD_SERVICE_LISTING = "add_service_listing"
    const val PART_LISTING_DETAIL = "part_listing/{partId}"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val MY_ORDERS = "my_orders"
    const val PART_ORDER_DETAIL = "part_order/{orderId}"
    const val SERVICE_BOOKING_DETAIL = "service_booking_order/{bookingId}"

    fun requestForm(type: String) = "request_form/$type"

    fun partListing(partId: String) = "part_listing/$partId"

    fun partOrderDetail(orderId: String) = "part_order/$orderId"

    fun serviceBookingDetail(bookingId: String) = "service_booking_order/$bookingId"

    fun requestDetail(requestId: String) = "request_detail/$requestId"
}
