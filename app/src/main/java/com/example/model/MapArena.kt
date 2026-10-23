package com.example.model

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

enum class PlatformType {
    ANCIENT_ROCK,
    LALIBELA_STONE,
    CASTLE_BRICK,
    WOODEN_BRIDGE,
    FLOATING_ISLAND
}

data class ArenaPlatform(
    val id: Int,
    val bounds: Rect,
    val isOneWay: Boolean = false,
    val type: PlatformType = PlatformType.ANCIENT_ROCK
)

data class SpawnPoint(val x: Float, val y: Float)

enum class MapArena(
    val title: String,
    val amharicName: String,
    val description: String,
    val worldWidth: Float,
    val worldHeight: Float,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val ambientColor: Color
) {
    SIMIEN_PEAKS(
        title = "Simien Mountain Peaks",
        amharicName = "የሰሜን ተራሮች",
        description = "Dramatic jagged Ethiopian mountain cliffs, high altitude winds, and suspended floating rock bridges.",
        worldWidth = 2400f,
        worldHeight = 1400f,
        skyColorTop = Color(0xFF1E3A8A),
        skyColorBottom = Color(0xFFFDE68A),
        ambientColor = Color(0xFF10B981)
    ),
    LALIBELA_RUINS(
        title = "Lalibela Rock Ruins",
        amharicName = "የላሊበላ ፍርስራሾች",
        description = "Monolithic volcanic rock-hewn monolithic structures with subterranean trenches and ancient cross pillars.",
        worldWidth = 2400f,
        worldHeight = 1400f,
        skyColorTop = Color(0xFF451A03),
        skyColorBottom = Color(0xFFB45309),
        ambientColor = Color(0xFFF59E0B)
    ),
    FASIL_FORTRESS(
        title = "Fasil Ghebbi Fortress",
        amharicName = "ፋሲል ግቢ ምሽግ",
        description = "17th-century medieval imperial stone castles, arched ramparts, and high watchtowers.",
        worldWidth = 2400f,
        worldHeight = 1400f,
        skyColorTop = Color(0xFF0F172A),
        skyColorBottom = Color(0xFF334155),
        ambientColor = Color(0xFF38BDF8)
    );

    fun getPlatforms(): List<ArenaPlatform> {
        return when (this) {
            SIMIEN_PEAKS -> listOf(
                // Solid ground
                ArenaPlatform(1, Rect(100f, 1250f, 2300f, 1380f), isOneWay = false, PlatformType.ANCIENT_ROCK),
                // Left mountain cliff
                ArenaPlatform(2, Rect(100f, 850f, 550f, 1250f), isOneWay = false, PlatformType.ANCIENT_ROCK),
                // Right mountain cliff
                ArenaPlatform(3, Rect(1850f, 850f, 2300f, 1250f), isOneWay = false, PlatformType.ANCIENT_ROCK),
                // Mid floating platforms
                ArenaPlatform(4, Rect(400f, 650f, 900f, 700f), isOneWay = true, PlatformType.WOODEN_BRIDGE),
                ArenaPlatform(5, Rect(1500f, 650f, 2000f, 700f), isOneWay = true, PlatformType.WOODEN_BRIDGE),
                ArenaPlatform(6, Rect(950f, 800f, 1450f, 850f), isOneWay = true, PlatformType.FLOATING_ISLAND),
                ArenaPlatform(7, Rect(800f, 420f, 1600f, 470f), isOneWay = true, PlatformType.FLOATING_ISLAND),
                // High sniper lookout perch
                ArenaPlatform(8, Rect(1050f, 200f, 1350f, 250f), isOneWay = true, PlatformType.ANCIENT_ROCK)
            )
            LALIBELA_RUINS -> listOf(
                // Underground foundation
                ArenaPlatform(1, Rect(100f, 1250f, 2300f, 1380f), isOneWay = false, PlatformType.LALIBELA_STONE),
                // Central cross monolithic structure
                ArenaPlatform(2, Rect(900f, 700f, 1500f, 1250f), isOneWay = false, PlatformType.LALIBELA_STONE),
                // Cross arms
                ArenaPlatform(3, Rect(700f, 850f, 900f, 920f), isOneWay = true, PlatformType.LALIBELA_STONE),
                ArenaPlatform(4, Rect(1500f, 850f, 1700f, 920f), isOneWay = true, PlatformType.LALIBELA_STONE),
                // Left sanctuary terrace
                ArenaPlatform(5, Rect(250f, 900f, 650f, 950f), isOneWay = true, PlatformType.LALIBELA_STONE),
                ArenaPlatform(6, Rect(200f, 550f, 550f, 600f), isOneWay = true, PlatformType.LALIBELA_STONE),
                // Right sanctuary terrace
                ArenaPlatform(7, Rect(1750f, 900f, 2150f, 950f), isOneWay = true, PlatformType.LALIBELA_STONE),
                ArenaPlatform(8, Rect(1850f, 550f, 2200f, 600f), isOneWay = true, PlatformType.LALIBELA_STONE),
                // Top high arch
                ArenaPlatform(9, Rect(850f, 350f, 1550f, 400f), isOneWay = true, PlatformType.LALIBELA_STONE)
            )
            FASIL_FORTRESS -> listOf(
                ArenaPlatform(1, Rect(100f, 1250f, 2300f, 1380f), isOneWay = false, PlatformType.CASTLE_BRICK),
                // Left tower
                ArenaPlatform(2, Rect(200f, 600f, 600f, 1250f), isOneWay = false, PlatformType.CASTLE_BRICK),
                ArenaPlatform(3, Rect(150f, 550f, 650f, 600f), isOneWay = true, PlatformType.CASTLE_BRICK),
                // Right tower
                ArenaPlatform(4, Rect(1800f, 600f, 2200f, 1250f), isOneWay = false, PlatformType.CASTLE_BRICK),
                ArenaPlatform(5, Rect(1750f, 550f, 2250f, 600f), isOneWay = true, PlatformType.CASTLE_BRICK),
                // Central royal parapet
                ArenaPlatform(6, Rect(900f, 900f, 1500f, 960f), isOneWay = true, PlatformType.CASTLE_BRICK),
                ArenaPlatform(7, Rect(750f, 620f, 1650f, 680f), isOneWay = true, PlatformType.CASTLE_BRICK),
                ArenaPlatform(8, Rect(1000f, 320f, 1400f, 370f), isOneWay = true, PlatformType.CASTLE_BRICK)
            )
        }
    }

    fun getSpawnPoints(): List<SpawnPoint> {
        return listOf(
            SpawnPoint(350f, 500f),
            SpawnPoint(2050f, 500f),
            SpawnPoint(1200f, 750f),
            SpawnPoint(600f, 1150f),
            SpawnPoint(1800f, 1150f),
            SpawnPoint(1200f, 300f)
        )
    }

    fun getPickupSpawns(): List<SpawnPoint> {
        return listOf(
            SpawnPoint(1200f, 150f),
            SpawnPoint(450f, 600f),
            SpawnPoint(1950f, 600f),
            SpawnPoint(1200f, 850f),
            SpawnPoint(300f, 1200f),
            SpawnPoint(2100f, 1200f)
        )
    }
}
