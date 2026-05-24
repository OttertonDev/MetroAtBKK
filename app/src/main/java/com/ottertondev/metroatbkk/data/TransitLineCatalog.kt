package com.ottertondev.metroatbkk.data

import java.util.Locale

data class TransitLineRoute(
    val lineKind: TransitLineKind,
    val stationNames: List<String>
)

object TransitLineCatalog {
    val routes = listOf(
        TransitLineRoute(
            TransitLineKind.BTS_SUKHUMVIT,
            listOf(
                "Kheha", "Sai Luat", "Phraek Sa", "Srinagarindra", "Pak Nam",
                "Royal Thai Naval Academy", "Chang Erawan", "Pu Chao", "Samrong",
                "Bearing", "Bang Na", "Udom Suk", "Punnawithi", "Bang Chak", "On Nut",
                "Phra Khanong", "Ekkamai", "Thong Lo", "Phrom Phong", "Asok", "Nana",
                "Phloen Chit", "Chit Lom", "Siam", "Ratchathewi", "Phaya Thai",
                "Victory Monument", "Sanam Pao", "Ari", "Saphan Khwai", "Mo Chit",
                "Ha Yeak Lat Phrao", "Phahon Yothin 24", "Ratchayothin", "Sena Nikhom",
                "Kasetsart University", "Royal Forest Department", "Bang Bua",
                "11th Infantry Regiment", "Wat Phrasri Mahathat", "Phahonyothin 59",
                "Sai Yud", "Saphan Mai", "Bhumibol Adulyadej Hospital",
                "Royal Thai Air Force Museum", "Yaek Kor Por Aor", "Khu Khot"
            )
        ),
        TransitLineRoute(
            TransitLineKind.BTS_SILOM,
            listOf(
                "National Stadium", "Siam", "Ratchadamri", "Sala Daeng", "Chong Nonsi",
                "Saint Louis", "Surasak", "Saphan Taksin", "Krung Thon Buri",
                "Wongwian Yai", "Pho Nimit", "Talat Phlu", "Wutthakat", "Bang Wa"
            )
        ),
        TransitLineRoute(
            TransitLineKind.BTS_GOLD,
            listOf("Krung Thon Buri", "Charoen Nakhon", "Khlong San")
        ),
        TransitLineRoute(
            TransitLineKind.MRT_BLUE,
            listOf(
                "Lak Song", "Bang Khae", "Phasi Charoen", "Phetkasem 48", "Bang Wa",
                "Bang Phai", "Tha Phra", "Itsaraphap", "Sanam Chai", "Sam Yot",
                "Wat Mangkon", "Hua Lamphong", "Sam Yan", "Si Lom", "Lumphini",
                "Khlong Toei", "Queen Sirikit National Convention Centre", "Sukhumvit",
                "Phetchaburi", "Phra Ram 9", "Thailand Cultural Centre", "Huai Khwang",
                "Sutthisan", "Ratchadaphisek", "Lat Phrao", "Phahon Yothin",
                "Chatuchak Park", "Kamphaeng Phet", "Bang Sue", "Tao Poon", "Bang Pho",
                "Bang O", "Bang Phlat", "Sirindhorn", "Bang Yi Khan", "Bang Khun Non",
                "Fai Chai", "Charan 13", "Tha Phra"
            )
        ),
        TransitLineRoute(
            TransitLineKind.MRT_PURPLE,
            listOf(
                "Khlong Bang Phai", "Talad Bang Yai", "Sam Yeak Bang Yai", "Bang Phlu",
                "Bang Rak Yai", "Bang Rak Noi Tha It", "Sai Ma", "Phra Nang Klao Bridge",
                "Yaek Nonthaburi 1", "Bang Krasor", "Nonthaburi Civic Center",
                "Ministry of Public Health", "Yeak Tiwanon", "Wong Sawang", "Bang Son",
                "Tao Poon"
            )
        ),
        TransitLineRoute(
            TransitLineKind.MRT_YELLOW,
            listOf(
                "Lat Phrao", "Phawana", "Chok Chai 4", "Lat Phrao 71", "Lat Phrao 83",
                "Mahat Thai", "Lat Phrao 101", "Bang Kapi", "Yaek Lam Sali",
                "Si Kritha", "Hua Mak", "Kalantan", "Si Nut", "Srinagarindra 38",
                "Suan Luang Rama IX", "Si Udom", "Si Iam", "Si La Salle", "Si Bearing",
                "Si Dan", "Si Thepha", "Thipphawan", "Samrong"
            )
        ),
        TransitLineRoute(
            TransitLineKind.MRT_PINK,
            listOf(
                "Nonthaburi Civic Center", "Khae Rai", "Sanambin Nam", "Samakkhi",
                "Royal Irrigation Department", "Pak Kret", "Pak Kret Bypass",
                "Chaeng Watthana-Pak Kret 28", "Si Rat", "Muang Thong Thani",
                "Chaeng Watthana 14", "Chaloem Phrakiat Government Center",
                "National Telecom", "Lak Si", "Phranakhon Rajabhat",
                "Wat Phra Sri Mahathat", "Ram Inthra 3", "Lat Pla Khao",
                "Ram Inthra Kor Mor 4", "Maiyalap", "Vacharaphol", "Ram Inthra Kor Mor 6",
                "Khu Bon", "Ram Inthra Kor Mor 9", "Outer Ring Road-Ram Inthra",
                "Nopparat", "Bang Chan", "Setthabutbamphen", "Min Buri Market", "Min Buri"
            )
        ),
        TransitLineRoute(
            TransitLineKind.MRT_PINK,
            listOf("Muang Thong Thani", "Impact Muang Thong Thani", "Lake Muang Thong Thani")
        ),
        TransitLineRoute(
            TransitLineKind.AIRPORT_RAIL_LINK,
            listOf(
                "Phaya Thai", "Ratchaprarop", "Makkasan", "Ramkhamhaeng", "Hua Mak",
                "Ban Thap Chang", "Lat Krabang", "Suvarnabhumi Airport"
            )
        ),
        TransitLineRoute(
            TransitLineKind.BRT,
            listOf(
                "Sathorn", "Arkan Songkhro", "Technic Krungthep", "Thanon Chan",
                "Nararam 3", "Wat Dan", "Wat Pariwat", "Wat Dokmai", "Rama IX Bridge",
                "Charoenrat", "Rama III Bridge", "Ratchapruek"
            )
        ),
        TransitLineRoute(
            TransitLineKind.SRT_DARK_RED,
            listOf(
                "Krung Thep Aphiwat Central Terminal Station", "Chatuchak", "Wat Samian Nari",
                "Bang Khen", "Thung Song Hong", "Lak Si", "Kan Kheha", "Don Mueang",
                "Lak Hok", "Rangsit"
            )
        ),
        TransitLineRoute(
            TransitLineKind.SRT_LIGHT_RED,
            listOf(
                "Krung Thep Aphiwat Central Terminal Station", "Bang Son", "Bang Bamru",
                "Taling Chan"
            )
        )
    )

    private val routeMembershipByStation = routes
        .flatMap { route -> route.stationNames.map { stationName -> canonicalStationName(stationName) to route.lineKind } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    fun lineKindsFor(nameEn: String, assetName: String): List<TransitLineKind> {
        val canonicalName = canonicalStationName(nameEn)
        val routeMembership = routeMembershipByStation[canonicalName].orEmpty().distinct()
        val normalizedAsset = assetName.trim().lowercase(Locale.US)

        return when (normalizedAsset) {
            "bts.png" -> routeMembership.filter { it.operatorName == "BTS" }
                .ifEmpty { listOf(TransitLineKind.BTS_SUKHUMVIT) }

            "mrt.png" -> routeMembership.filter { it.operatorName == "MRT" }
                .ifEmpty { listOf(TransitLineKind.MRT_BLUE) }

            "airport_link.png" -> when {
                canonicalStationName(nameEn).startsWith("arl ") -> listOf(TransitLineKind.AIRPORT_RAIL_LINK)
                nameEn.startsWith("ARL ", ignoreCase = true) -> listOf(TransitLineKind.AIRPORT_RAIL_LINK)
                else -> routeMembership.filter { it.operatorName == "SRT" }
            }

            "brt.png" -> listOf(TransitLineKind.BRT)
            else -> emptyList()
        }
    }

    fun canonicalStationName(name: String): String {
        return name.trim()
            .replace(Regex("^(BTS|MRT|ARL|SRT|BRT)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*[\\[(].*$"), "")
            .lowercase(Locale.US)
            .replace("khrung thon buri", "krung thon buri")
            .replace("bang bau", "bang bua")
            .replace("bangwa", "bang wa")
            .replace("bangphai", "bang phai")
            .replace("lak song", "lak song")
            .replace("lad phrao", "lat phrao")
            .replace("yeak", "yaek")
            .replace("pakkret", "pak kret")
            .replace("kak si", "lak si")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }
}
