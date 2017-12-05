package com.pipai.adv

import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.valueOfOrDefault
import java.util.Properties
import org.yaml.snakeyaml.Yaml
import kotlin.reflect.full.declaredMemberProperties



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

enum class Controls {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    INTERACT
}

class KeyConfig (strKeyConfigsMap: Map<String, List<String>>) {
    val defaultKeyConfigsMap = mapOf(
            Controls.MOVE_UP to listOf(Input.Keys.UP, Input.Keys.W),
            Controls.MOVE_DOWN to listOf(Input.Keys.DOWN, Input.Keys.S),
            Controls.MOVE_LEFT to listOf(Input.Keys.LEFT, Input.Keys.A),
            Controls.MOVE_RIGHT to listOf(Input.Keys.RIGHT, Input.Keys.D),
            Controls.INTERACT to listOf(Input.Keys.Z)
            )
    val keyConfigsMap: Map<Controls, List<Int>> = mapOf()
    init {
        
    }

    fun isValidKey(str: String): Boolean {
        fun <R: Any?> readProperty(instance: Any, propertyName: String): R {
            val clazz = instance.javaClass.kotlin
            @Suppress("UNCHECKED_CAST")
            return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance) as R
        }
        val key: Int = readProperty(Input.Keys(), str)
        return (key is Int) //probably doesn't work.
    }
    fun restoreDefaults () {

    }
}
class AdvConfig(val configFile: FileHandle) {

    private val logger = getLogger()

    var resolution: ScreenResolution
    var KeyConfigsMap = emptyMap<KeyConfig, List<Input.Keys>>()

    init {

        if (configFile.exists()) {
            val yaml = Yaml()
            val text = configFile.reader().use { it.readText() }
            try {
                val configMap = yaml.load(text) as Map<String, Any>
                resolution = valueOfOrDefault(configMap["resolution"].toString(), DEFAULT_RESOLUTION)

                KeyConfigsMap = configMap["controlKeys"] as Map<KeyConfig, List<Input.Keys>> //how do I make sure that the contents of the map are right
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
