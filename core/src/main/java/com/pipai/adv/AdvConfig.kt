package com.pipai.adv

import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.valueOfOrDefault
import java.util.Properties
import org.yaml.snakeyaml.Yaml



enum class AspectRatio {
    AR_4_3,
    AR_16_10,
    AR_16_9,
}

enum class ScreenResolution(val width: Int, val height: Int, val aspectRatio: AspectRatio, val description: String?,
                            val tileSize: Int) {
    // 4:3 Resolutions
    RES_1024_768(1024, 768, AspectRatio.AR_4_3, null, 32),
    RES_1280_960(1280, 960, AspectRatio.AR_4_3, null, 32),

    // 16:10 Resolutions (rare but should be supported, why not)
    RES_1280_800(1280, 800, AspectRatio.AR_16_10, null, 32),

    // 16:9 Resolutions
    RES_1280_720(1280, 720, AspectRatio.AR_16_9, "720p", 32),
    RES_1920_1080(1920, 1080, AspectRatio.AR_16_9, "1080p", 32);

    fun toDebugString(): String {
        return "ScreenResolution[${width}x${height}, ${aspectRatio}, ${description}, TileSize: ${tileSize}]"
    }
}

private val DEFAULT_RESOLUTION = ScreenResolution.RES_1024_768

enum class Control {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    INTERACT
}
class AdvConfig(val configFile: FileHandle) {

    private val logger = getLogger()

    var resolution: ScreenResolution
    var controlKeysMap = emptyMap<Control, List<Input.Keys>>()

    init {

        if (configFile.exists()) {
            val yaml = Yaml()
            val text = configFile.reader().use { it.readText() }
            try {
                val configMap = yaml.load(text) as Map<String, Any>
                var config = configMap["resolution"]
                resolution = valueOfOrDefault(config.toString(), DEFAULT_RESOLUTION)
                config = configMap["controlKeys"]
                if (config is Map<String, String>) //how do I convert Any to the Map I want?
            } catch (e : Exception) {
                resolution = DEFAULT_RESOLUTION
            }
        } else {
            resolution = DEFAULT_RESOLUTION
        }
    }

    fun writeToFile() {
        val yaml = Yaml()
        val configMap = mapOf("resolution" to resolution.toString())
        val output = yaml.dump(configMap)

        configFile.writer(false, "UTF-8").use { it.write(output) }
    }

}
