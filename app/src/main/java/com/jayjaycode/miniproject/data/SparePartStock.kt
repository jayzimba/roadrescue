package com.jayjaycode.miniproject.data

object SparePartStock {
    fun isUnlimited(listedQuantity: Int?): Boolean = listedQuantity == null

    fun committedFromOrders(orders: List<PartOrder>): Map<String, Int> {
        val totals = mutableMapOf<String, Int>()
        orders.filter { it.status != OrderStatus.CANCELLED }.forEach { order ->
            order.items.forEach { line ->
                totals[line.partId] = (totals[line.partId] ?: 0) + line.quantity
            }
        }
        return totals
    }

    fun availableQuantity(listedQuantity: Int?, committedQuantity: Int): Int? {
        if (listedQuantity == null) return null
        return (listedQuantity - committedQuantity).coerceAtLeast(0)
    }

    fun canFulfill(listedQuantity: Int?, committedQuantity: Int, requestedQuantity: Int): Boolean {
        if (requestedQuantity < 1) return false
        val available = availableQuantity(listedQuantity, committedQuantity) ?: return true
        return requestedQuantity <= available
    }

    fun maxSelectable(listedQuantity: Int?, committedQuantity: Int): Int? {
        return availableQuantity(listedQuantity, committedQuantity)
    }

    fun availabilityLabel(listedQuantity: Int?, committedQuantity: Int, inStock: Boolean): String {
        if (!inStock) return "Out of stock"
        if (isUnlimited(listedQuantity)) return "In stock"
        val available = availableQuantity(listedQuantity, committedQuantity) ?: 0
        return when {
            available <= 0 -> "Out of stock"
            available == 1 -> "1 available"
            else -> "$available available"
        }
    }

    fun isPurchasable(listedQuantity: Int?, committedQuantity: Int, inStock: Boolean): Boolean {
        if (!inStock) return false
        if (isUnlimited(listedQuantity)) return true
        return (availableQuantity(listedQuantity, committedQuantity) ?: 0) > 0
    }
}
