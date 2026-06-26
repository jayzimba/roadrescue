package com.jayjaycode.miniproject.data

import java.util.Calendar

/**
 * Common vehicle makes and models popular in Zambia.
 */
object ZambianVehicleCatalog {

    private val catalog: Map<String, List<String>> = mapOf(
        "Toyota" to listOf(
            "Hilux", "Land Cruiser", "Prado", "Fortuner", "Corolla", "RAV4", "Avanza",
            "Vitz", "Hiace", "Quantum", "Rumion", "Rush", "Yaris", "Camry", "Auris",
            "Starlet", "Tacoma", "Tundra", "4Runner", "Sequoia", "Sienna", "Alphard",
            "C-HR", "Harrier", "Kluger", "Noah", "Voxy", "Wish", "Probox", "Succeed",
            "Dyna", "Coaster", "Lite Ace", "Town Ace", "Fortuner", "Prado", "Fortuner", "Corolla", "RAV4", "Avanza",
            "Vitz", "Hiace", "Quantum", "Rumion", "Rush", "Yaris", "Camry", "Auris",
            "Starlet", "Tacoma", "Tundra", "4Runner", "Sequoia", "Sienna", "Alphard",
            "C-HR", "Harrier", "Kluger", "Noah", "Voxy", "Wish", "Succeed",
            "Dyna", "Coaster", "Lite Ace", "Town Ace", "Fortuner", "Prado", "Fortuner", "Corolla", "RAV4", "Avanza",
            "Vitz", "Hiace", "Quantum", "Rumion", "Rush", "Yaris", "Camry", "Auris",
            "Starlet", "Tacoma", "Tundra", "4Runner", "Sequoia", "Sienna", "Alphard",
            "C-HR", "Harrier", "Kluger", "Noah", "Voxy", "Wish", "Succeed","Mark X"
        ),
        "Nissan" to listOf(
            "Navara", "Patrol", "X-Trail", "NP300 Hardbody", "Almera", "Tiida", "Note",
            "Micra", "Sunny", "Sentra", "Altima", "Maxima", "Murano", "Pathfinder",
            "Armada", "Juke", "Qashqai", "Kicks", "Rogue", "Terrano", "Safari",
            "Caravan", "Urvan", "NV350", "NV200", "Leaf", "Skyline", "GT-R", "370Z",
            "350Z", "Cube", "March", "Wingroad", "AD Van", "Serena",
        ),
        "Lexus" to listOf(
            "RX", "LX", "NX", "ES", "LS", "LC", "GX", "G-Class", "GL-Class", "GLK-Class", "GLC-Class", "GLS-Class", "G-Class", "GL-Class", "GLK-Class", "GLC-Class", "GLS-Class", "G-Class", "GL-Class", "GLK-Class", "GLC-Class", "GLS-Class", "G-Class", "GL-Class", "GLK-Class", "GLC-Class", "GLS-Class",
        ),
        "Honda" to listOf(
            "Fit", "Civic", "Accord", "CR-V", "HR-V", "Pilot", "Odyssey", "Stream",
            "Freed", "City", "Jazz", "Insight", "Passport", "Ridgeline", "Element",
            "BR-V", "WR-V", "Mobilio", "Brio", "Amaze", "Ballade", "Legend",
            "Prelude", "Integra", "S2000", "NSX", "Stepwgn", "Elysion", "Vezel",
            "Shuttle", "Airwave", "Fit Aria", "Crossroad", "ZR-V", "Elevate",
        ),
        "Mitsubishi" to listOf(
            "Pajero", "Pajero Sport", "L200", "Triton", "Outlander", "ASX", "Eclipse Cross",
            "Colt", "Lancer", "Mirage", "Galant", "Montero", "Shogun", "Delica",
            "Fuso Canter", "Fuso Fighter", "Rosa", "Attrage", "Xpander", "Strada",
            "Pajero Mini", "i-MiEV", "3000GT", "GTO", "Diamante", "Chariot", "Space Wagon",
            "Grandis", "RVR", "Colt Plus", "Lancer EX", "Lancer Cedia", "Airtrek", "Endeavor",
            "Raider",
        ),
        "Mazda" to listOf(
            "BT-50", "CX-5", "CX-3", "CX-30", "CX-9", "CX-60", "CX-90", "Mazda2", "Mazda3",
            "Mazda6", "Demio", "Axela", "Atenza", "Premacy", "Biante", "MPV", "Tribute",
            "B-Series", "Proceed", "Familia", "Capella", "RX-7", "RX-8", "MX-5", "MX-30",
            "Bongo", "Scrum", "Carol", "Flair", "Verisa", "Axela Sport", "Roadster",
            "Titan", "E-Series", "CX-8", "CX-50",
        ),
        "Ford" to listOf(
            "Ranger", "Everest", "Raptor", "F-150", "F-250", "F-350", "Focus", "Fiesta",
            "Mondeo", "Fusion", "Mustang", "Explorer", "Expedition", "Escape", "Kuga",
            "EcoSport", "Edge", "Bronco", "Territory", "Transit", "Tourneo", "B-Max",
            "C-Max", "S-Max", "Galaxy", "Ka", "Puma", "Maverick", "Courier", "Ikon",
            "Laser", "Telstar", "Windstar", "Flex", "Aspire",
        ),
        "Chevrolet" to listOf(
            "Spark", "Aveo", "Sonic", "Cruze", "Malibu", "Impala", "Camaro", "Corvette",
            "Trailblazer", "Traverse", "Equinox", "Blazer", "Tahoe", "Suburban", "Colorado",
            "Silverado", "Captiva", "Orlando", "Trax", "Bolt", "Lumina", "Optra", "Kalos",
            "Lacetti", "Nubira", "Epica", "Cobalt", "HHR", "Uplander", "Avalanche",
            "S-10", "Montana", "Onix", "Prisma", "Spin",
        ),
        "BMW" to listOf(
            "1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "6 Series", "7 Series",
            "8 Series", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "XM", "Z3", "Z4",
            "i3", "i4", "i5", "i7", "i8", "iX", "iX1", "iX3", "M2", "M3", "M4", "M5",
            "M6", "M8", "X3 M", "X4 M", "X5 M", "X6 M",
        ),
        "Mercedes-Benz" to listOf(
            "A-Class", "B-Class", "C-Class", "E-Class", "S-Class", "CLA", "CLS", "GLA",
            "GLB", "GLC", "GLE", "GLS", "G-Class", "SL", "SLC", "AMG GT", "V-Class",
            "Vito", "Sprinter", "Citan", "Actros", "Atego", "Axor", "Unimog", "EQA", "EQB",
            "EQC", "EQE", "EQS", "Maybach S-Class", "Maybach GLS", "CLK", "SLK", "R-Class",
            "ML-Class", "GL-Class",
        ),
        "Volkswagen" to listOf(
            "Polo", "Polo Vivo", "Golf", "Jetta", "Passat", "Arteon", "Tiguan", "Touareg",
            "T-Cross", "T-Roc", "Taigo", "Amarok", "Caddy", "Transporter", "Crafter",
            "Kombi", "Beetle", "Scirocco", "CC", "Phaeton", "Touran", "Sharan", "Up!",
            "Fox", "Lupo", "Bora", "Vento", "Virtus", "Nivus", "ID.3", "ID.4", "ID.5",
            "ID. Buzz", "California", "Multivan", "Teramont",
        ),
        "Hyundai" to listOf(
            "i10", "i20", "i30", "Accent", "Elantra", "Sonata", "Azera", "Tucson", "Santa Fe",
            "Palisade", "Creta", "Venue", "Kona", "Bayon", "Staria", "H-1", "H100", "Mighty",
            "Porter", "HD65", "HD72", "HD78", "County", "Terracan", "Galloper", "Trajet",
            "Matrix", "Getz", "Atos", "Excel", "Coupe", "Genesis", "Ioniq", "Ioniq 5",
            "Ioniq 6", "Starex",
        ),
        "Kia" to listOf(
            "Picanto", "Rio", "Pegas", "Cerato", "Forte", "K5", "Optima", "Stinger", "Soul",
            "Seltos", "Sportage", "Sorento", "Telluride", "Carnival", "Sedona", "Mohave",
            "Bongo", "K2700", "K3000", "Pregio", "Carens", "Niro", "EV6", "EV9", "Sonet",
            "Stonic", "XCeed", "ProCeed", "Ceed", "Venga", "Spectra", "Magentis", "Opirus",
            "Pride", "Clarus", "Shuma",
        ),
        "Isuzu" to listOf(
            "D-Max", "KB", "Rodeo", "Trooper", "MU-X", "MUX", "Faster", "Gemini", "Florian",
            "Bighorn", "VehiCROSS", "Amigo", "Ascender", "Axiom", "i-Series", "Hombre",
            "Oasis", "Impulse", "Stylus", "Piazza", "117 Coupe", "Bellett", "Fargo", "Elf",
            "N-Series", "F-Series", "Giga", "Forward", "NPR", "NQR", "NRR", "FRR", "FTR",
            "FVR", "GVR", "CYZ",
        ),
        "Land Rover" to listOf(
            "Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Sport",
            "Range Rover Evoque", "Range Rover Velar", "Freelander", "Freelander 2",
            "Series I", "Series II", "Series III", "Ninety", "One Ten", "Defender 90",
            "Defender 110", "Defender 130", "LR2", "LR3", "LR4", "Classic", "Perentie",
            "Wolf", "Santana", "County", "Hardtop", "Pickup", "Station Wagon", "Camel Trophy",
            "Autobiography", "HSE", "SVAutobiography", "Vogue", "Westminster", "Evoque Convertible",
            "Velar SVR",
        ),
        "Suzuki" to listOf(
            "Swift", "Swift Sport", "Baleno", "Celerio", "Alto", "Ignis", "Vitara", "Grand Vitara",
            "Jimny", "S-Cross", "Ertiga", "XL7", "Carry", "Super Carry", "Every", "Wagon R",
            "Ciaz", "Dzire", "Kizashi", "SX4", "Liana", "Aerio", "Forenza", "Reno", "Samurai",
            "Sidekick", "Escudo", "Cultus", "Splash", "APV", "Landy", "Hustler", "Spacia",
            "Solio", "Palette", "Lapin",
        ),
    )

    val makes: List<String> = catalog.keys.sorted()

    val years: List<String> = run {
        val current = Calendar.getInstance().get(Calendar.YEAR)
        (current downTo 1995).map { it.toString() }
    }

    fun modelsFor(make: String): List<String> = catalog[make]?.sorted() ?: emptyList()
}
