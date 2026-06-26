package com.jayjaycode.miniproject.data

data class MarketplacePartFilters(
    val category: String? = null,
    val condition: String? = null,
    val make: String? = null,
    val model: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val inStockOnly: Boolean = false,
) {
    val activeCount: Int
        get() = listOfNotNull(category, condition, make, model, paymentMethod).size +
            if (inStockOnly) 1 else 0

    val isActive: Boolean
        get() = activeCount > 0
}

object MarketplacePartFilterMatcher {
    fun matches(part: SparePart, query: String, filters: MarketplacePartFilters): Boolean {
        val matchesText = query.isBlank() || part.matchesSearchQuery(query)
        val matchesFilters = matchesAdvancedFilters(part, filters)
        return matchesText && matchesFilters
    }

    private fun matchesAdvancedFilters(part: SparePart, filters: MarketplacePartFilters): Boolean {
        if (!SparePartCategories.matchesFilter(part.category, filters.category)) return false
        if (!SparePartConditions.matchesFilter(part.condition, filters.condition)) return false
        if (filters.make != null && !part.isCompatibleWith(filters.make, filters.model)) return false
        if (filters.paymentMethod != null && filters.paymentMethod !in part.paymentMethods) return false
        if (filters.inStockOnly && !part.inStock) return false
        return true
    }

    private fun SparePart.matchesSearchQuery(query: String): Boolean {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return true
        val haystack = buildList {
            add(name)
            add(category)
            add(condition)
            add(seller)
            addAll(compatibleVehicles.map { it.displayLabel() })
        }
        return haystack.any { it.lowercase().contains(needle) }
    }
}

fun SparePart.isCompatibleWith(filterMake: String, filterModel: String?): Boolean {
    if (compatibleVehicles.isEmpty()) return false
    return compatibleVehicles.any { it.covers(filterMake, filterModel) }
}

fun VehicleCompatibility.covers(filterMake: String, filterModel: String?): Boolean {
    if (isUniversal) return true
    if (make != VehicleCompatibility.ALL_MAKES && !make.equals(filterMake, ignoreCase = true)) return false
    if (filterModel.isNullOrBlank()) return true
    if (model == VehicleCompatibility.ALL_MODELS) return true
    return model.equals(filterModel, ignoreCase = true)
}
