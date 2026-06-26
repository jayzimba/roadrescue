package com.jayjaycode.miniproject.data

object SparePartCategories {
    const val OTHER = "Other"

    val all: List<String> = listOf(
        OTHER,
        "Engine & exhaust",
        "Transmission & drivetrain",
        "Brakes",
        "Suspension & steering",
        "Electrical & electronics",
        "Body & exterior",
        "Interior",
        "Cooling system",
        "Fuel system",
        "Filters & fluids",
        "Tyres & wheels",
        "Lights & bulbs",
        "Mirrors & glass",
        "Belts & hoses",
        "Batteries",
        "Air conditioning",
        "Sensors & modules",
        "Gaskets & seals",
        "Tools & accessories",
    )

    val standard: List<String> = all.filter { it != OTHER }

    fun isCustomCategory(category: String): Boolean =
        category.isNotBlank() && category !in standard

    fun resolveCategory(selected: String, customOther: String): String =
        if (selected == OTHER) customOther.trim() else selected.trim()

    fun matchesFilter(partCategory: String, filter: String?): Boolean {
        if (filter == null) return true
        return if (filter == OTHER) {
            isCustomCategory(partCategory)
        } else {
            partCategory.equals(filter, ignoreCase = true)
        }
    }
}
