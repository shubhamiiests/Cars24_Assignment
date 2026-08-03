package com.cars24.feature.staticbaseline

internal data class StaticCar(
    val name: String,
    val price: String,
    val emi: String,
    val specs: List<String>,
    val badge: String? = null,
    val savings: String? = null,
    val assured: Boolean = false,
    val imageUrl: String? = null,
)

internal data class StaticBanner(
    val title: String,
    val subtitle: String,
    val cta: String,
    val from: Long,
    val to: Long,
)

internal data class StaticValueProp(val title: String, val caption: String)

internal data class StaticFaq(val question: String, val answer: String)

internal data class StaticTenure(
    val label: String,
    val months: String,
    val monthly: String,
    val total: String,
)

internal object StaticHomeData {

    const val CITY = "Gurgaon"
    const val GREETING = "Find your next car"
    const val SEARCH_HINT = "Search Swift, Baleno, i20, Nexon..."

    val quickActions = listOf(
        "Buy car" to "12,400 cars",
        "Sell car" to "Best price",
        "Car loan" to "From 9.7%",
        "Insurance" to "Renew fast",
    )

    val banners = listOf(
        StaticBanner(
            title = "Zero down payment",
            subtitle = "On 2,000+ assured cars this month",
            cta = "Check eligibility",
            from = 0xFF1B2065,
            to = 0xFF5865C4,
        ),
        StaticBanner(
            title = "Sell in a single visit",
            subtitle = "Instant payment, free RC transfer",
            cta = "Get a quote",
            from = 0xFF0B8A6B,
            to = 0xFF3FCFA8,
        ),
        StaticBanner(
            title = "7-day money back",
            subtitle = "Not in love with it? Return it.",
            cta = "How it works",
            from = 0xFFE8890C,
            to = 0xFFFFC46B,
        ),
    )

    val fuelTabs = listOf(
        Triple("All", "all", "412"),
        Triple("Petrol", "petrol", "268"),
        Triple("Diesel", "diesel", "91"),
        Triple("CNG", "cng", "53"),
    )

    val carsByFuel: Map<String, List<StaticCar>> = mapOf(
        "all" to listOf(
            StaticCar("Maruti Swift VXi", "Rs 5.24 L", "Rs 11,400/mo", listOf("2019", "42,150 km", "Petrol", "Manual"), savings = "Save 38k", assured = true,
                imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Hyundai i20 Sportz", "Rs 6.85 L", "Rs 14,820/mo", listOf("2020", "31,900 km", "Petrol", "Manual"), badge = "Popular", assured = true,
                imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Tata Nexon XZ+", "Rs 7.95 L", "Rs 17,190/mo", listOf("2019", "55,600 km", "Diesel", "Manual"),
                imageUrl = "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Maruti WagonR LXi", "Rs 4.60 L", "Rs 9,950/mo", listOf("2021", "28,400 km", "CNG", "Manual"), badge = "Low km",
                imageUrl = "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=640&q=72&auto=format&fit=crop",
            ),
        ),
        "petrol" to listOf(
            StaticCar("Maruti Swift VXi", "Rs 5.24 L", "Rs 11,400/mo", listOf("2019", "42,150 km", "Petrol", "Manual"), savings = "Save 38k", assured = true,
                imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Maruti Baleno Zeta", "Rs 6.40 L", "Rs 13,850/mo", listOf("2020", "36,800 km", "Petrol", "AMT"), assured = true,
                imageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Hyundai Grand i10 Nios", "Rs 5.10 L", "Rs 11,040/mo", listOf("2021", "22,300 km", "Petrol", "Manual"), badge = "Low km",
                imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=640&q=72&auto=format&fit=crop",
            ),
        ),
        "diesel" to listOf(
            StaticCar("Tata Nexon XZ+", "Rs 7.95 L", "Rs 17,190/mo", listOf("2019", "55,600 km", "Diesel", "Manual"),
                imageUrl = "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Ford EcoSport Titanium", "Rs 6.75 L", "Rs 14,600/mo", listOf("2018", "68,200 km", "Diesel", "Manual"), savings = "Save 52k",
                imageUrl = "https://images.unsplash.com/photo-1550355291-bbee04a92027?w=640&q=72&auto=format&fit=crop",
            ),
        ),
        "cng" to listOf(
            StaticCar("Maruti WagonR LXi", "Rs 4.60 L", "Rs 9,950/mo", listOf("2021", "28,400 km", "CNG", "Manual"), badge = "Low km",
                imageUrl = "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=640&q=72&auto=format&fit=crop",
            ),
            StaticCar("Maruti Alto K10 VXi", "Rs 3.85 L", "Rs 8,330/mo", listOf("2022", "19,700 km", "CNG", "Manual"), assured = true,
                imageUrl = "https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?w=640&q=72&auto=format&fit=crop",
            ),
        ),
    )

    val tenures = listOf(
        StaticTenure("36 mo", "36", "Rs 18,240", "Rs 6,56,640"),
        StaticTenure("48 mo", "48", "Rs 14,820", "Rs 7,11,360"),
        StaticTenure("60 mo", "60", "Rs 12,410", "Rs 7,44,600"),
        StaticTenure("72 mo", "72", "Rs 10,780", "Rs 7,76,160"),
    )

    val valueProps = listOf(
        StaticValueProp("140-point", "inspection"),
        StaticValueProp("7-day", "money back"),
        StaticValueProp("Free RC", "transfer"),
        StaticValueProp("1-year", "warranty"),
    )

    val assured = listOf(
        StaticCar("Hyundai Creta SX", "Rs 11.40 L", "Rs 24,650/mo", listOf("2020", "44,100 km", "Petrol"), assured = true,
                imageUrl = "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=640&q=72&auto=format&fit=crop",
            ),
        StaticCar("Hyundai Venue S", "Rs 8.20 L", "Rs 17,740/mo", listOf("2021", "29,600 km", "Petrol"), badge = "New",
                imageUrl = "https://images.unsplash.com/photo-1517672651691-24622a91b550?w=640&q=72&auto=format&fit=crop",
            ),
        StaticCar("Maruti Brezza ZXi", "Rs 9.65 L", "Rs 20,880/mo", listOf("2020", "38,500 km", "Petrol"), assured = true,
                imageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=640&q=72&auto=format&fit=crop",
            ),
        StaticCar("Renault Kwid RXT", "Rs 3.40 L", "Rs 7,360/mo", listOf("2019", "51,200 km", "Petrol"), savings = "Save 24k",
                imageUrl = "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=640&q=72&auto=format&fit=crop",
            ),
    )

    val faqs = listOf(
        StaticFaq(
            "How does the 7-day money back work?",
            "Drive the car for up to 7 days or 350 km. If it is not right for you, return it at any Cars24 hub and we refund the full amount, no questions asked.",
        ),
        StaticFaq(
            "Is the RC transfer really free?",
            "Yes. We handle the paperwork end to end and absorb the transfer fee. You get an SMS from the RTO once it completes, usually within 45 days.",
        ),
        StaticFaq(
            "Can I get a loan without a credit history?",
            "Often yes. We work with 15 lending partners and some approve first-time borrowers against income proof. The EMI above is indicative until a partner quotes you.",
        ),
    )
}
