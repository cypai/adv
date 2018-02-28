package com.pipai.adv

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.valueOfOrDefault
import java.util.Properties

enum class AspectRatio {
    AR_4_3,
    AR_16_10,
    AR_16_9,
}

enum class ScreenResolution(val width: Int, val height: Int, val aspectRatio: AspectRatio, val description: String?,
                            val tileSize: Int, val tileOffset: Int) {
    // 4:3 Resolutions
    RES_1024_768(1024, 768, AspectRatio.AR_4_3, null, 32, 10),
    RES_1280_960(1280, 960, AspectRatio.AR_4_3, null, 32, 10),

    // 16:10 Resolutions (rare but should be supported, why not)
    RES_1280_800(1280, 800, AspectRatio.AR_16_10, null, 32, 10),

    // 16:9 Resolutions
    RES_1280_720(1280, 720, AspectRatio.AR_16_9, "720p", 32, 10),
    RES_1920_1080(1920, 1080, AspectRatio.AR_16_9, "1080p", 32, 10);

    fun toDebugString(): String {
        return "ScreenResolution[${width}x${height}, ${aspectRatio}, ${description}, TileSize: ${tileSize}]"
    }
}

private val DEFAULT_RESOLUTION = ScreenResolution.RES_1024_768

class AdvConfig(val configFile: FileHandle) {

    private val logger = getLogger()

    var resolution: ScreenResolution

    init {
        if (configFile.exists()) {
            val properties = Properties()
            configFile.reader().use { properties.load(it) }

            resolution = valueOfOrDefault(properties["resolution"] as String, DEFAULT_RESOLUTION)
        } else {
            resolution = DEFAULT_RESOLUTION
        }
    }

    fun writeToFile() {
        val properties = Properties()
        properties.put("resolution", resolution.toString())

        configFile.writer(false, "UTF-8").use { properties.store(it, "ADV Config") }
    }
}
