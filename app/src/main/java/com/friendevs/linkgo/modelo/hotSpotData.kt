package com.friendevs.linkgo.model

data class Hotspot(
    val id: Int,
    val name: String,
    val fotos: Int,
    val url: String
) {
    companion object {
        val hotspotList = listOf(
            Hotspot(
                1,
                "Parque Central",
                12,
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb"
            ),
            Hotspot(
                2,
                "Café Aroma",
                8,
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93"
            ),
            Hotspot(
                3,
                "Centro Comercial Nova",
                20,
                "https://images.unsplash.com/photo-1519567241046-7f570eee3ce6"
            ),
            Hotspot(
                4,
                "Universidad Nacional",
                35,
                "https://images.unsplash.com/photo-1523240795612-9a054b0db644"
            ),
            Hotspot(
                5,
                "Plaza Mayor",
                18,
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee"
            ),
            Hotspot(
                6,
                "Universidad Javeriana",
                310,
                "https://images.unsplash.com/photo-1562774053-701939374585"
            ),
            Hotspot(
                7,
                "Universidad Distrital",
                35,
                "https://images.unsplash.com/photo-1580582932707-520aed937b7b"
            )
        )
    }
}