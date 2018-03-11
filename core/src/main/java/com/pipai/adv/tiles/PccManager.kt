package com.pipai.adv.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.utils.getLogger

class PccManager {

    companion object {
        const val PCC_WIDTH = 32
        const val PCC_HEIGHT = 48
    }

    private val logger = getLogger()

    private val pccTilesets: MutableMap<String, FileTileset> = mutableMapOf()

    fun listPccs(type: String): List<PccMetadata> {
        return Gdx.files.local("assets/binassets/graphics/pccs/$type/").list()
                .map { it.name() }
                .sorted()
                .map { PccMetadata(type, it, null, null) }
    }

    fun loadPccTextures(pccMetadataList: List<PccMetadata>) {
        for (metadata in pccMetadataList) {
            val key = keyFor(metadata)
            if (!pccTilesets.containsKey(key)) {
                val file = Gdx.files.local("assets/binassets/graphics/pccs/${metadata.type}/${metadata.filename}")
                if (file.exists()) {
                    pccTilesets.put(key, FileTileset(file, PCC_WIDTH, PCC_HEIGHT))
                } else {
                    logger.warn("Unable to load PCC ${metadata} because ${file.path()} does not exist")
                }
            }
        }
    }

    fun unloadPccTextures(pccMetadataList: List<PccMetadata>) {
        for (metadata in pccMetadataList) {
            val key = keyFor(metadata)
            if (pccTilesets.containsKey(key)) {
                pccTilesets[key]!!.dispose()
                pccTilesets.remove(key)
            }
        }
    }

    fun pccIsLoaded(metadata: PccMetadata): Boolean = pccTilesets.contains(metadata.toString())

    fun getPccFrame(metadata: PccMetadata, frame: UnitAnimationFrame): TextureRegion {
        val key = keyFor(metadata)
        if (pccTilesets.containsKey(key)) {
            val tileset = pccTilesets.get(key)!!
            return tileset.tile(TilePosition(frame.index, directionToRow(frame.direction)))
        } else {
            throw IllegalArgumentException("PCC for ${metadata} has not yet been loaded")
        }
    }

    private fun directionToRow(direction: Direction): Int {
        return when (direction) {
            Direction.N -> 3
            Direction.S -> 0
            Direction.E -> 2
            Direction.W -> 1
            Direction.NW -> 1
            Direction.SW -> 1
            Direction.NE -> 2
            Direction.SE -> 2
        }
    }

    fun dispose() {
        for (kv in pccTilesets) {
            kv.value.dispose()
        }
    }

    private fun keyFor(metadata: PccMetadata): String = "${metadata.type} ${metadata.filename}"

}

data class PccMetadata(val type: String, val filename: String, val color1: Color?, val color2: Color?)

data class UnitAnimationFrame(val direction: Direction, val index: Int)
