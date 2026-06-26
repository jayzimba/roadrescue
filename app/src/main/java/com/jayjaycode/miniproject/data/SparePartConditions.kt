package com.jayjaycode.miniproject.data

object SparePartConditions {
    val all: List<String> = listOf(
        "New",
        "Used - Excellent",
        "Used - Good",
        "Used - Fair",
        "Refurbished",
        "For parts only",
    )

    val default: String = all.first()

    fun matchesFilter(partCondition: String, filter: String?): Boolean {
        if (filter == null) return true
        return partCondition.equals(filter, ignoreCase = true)
    }
}
