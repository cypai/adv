package com.pipai.adv.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.utils.getLogger

class PccManager {

    private val logger = getLogger()

    private val pccTilesets: MutableMap<String, FileTileset> = mutableMapOf()

    fun loadPccTextures(pccMetadataList: List<PccMetadata>) {
        for (metadata in pccMetadataList) {
            val key = metadata.toString()
            if (!pccTilesets.containsKey(key)) {
                val file = Gdx.files.internal("assets/binassets/graphics/pccs/${metadata.type}/${metadata.type}_${metadata.filename}.png")
                if (file.exists()) {
                    pccTilesets.put(key, FileTileset(file, 32, 48))
                } else {
                    logger.warn("Unable to load PCC ${metadata} because ${file.path()} does not exist")
                }
            }
        }
    }

    fun unloadPccTextures(pccMetadataList: List<PccMetadata>) {
        for (metadata in pccMetadataList) {
            val key = metadata.toString()
            if (pccTilesets.containsKey(key)) {
                pccTilesets.get(key)!!.dispose()
                pccTilesets.remove(key)
            }
        }
    }

    fun pccIsLoaded(metadata: PccMetadata): Boolean = pccTilesets.contains(metadata.toString())

    fun getPccFrame(metadata: PccMetadata, frame: PccFrame): TextureRegion {
        val key = metadata.toString()
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
            Direction.SW -> 0
            Direction.NE -> 2
            Direction.SE -> 3
        }
    }

    fun dispose() {
        for (kv in pccTilesets) {
            kv.value.dispose()
        }
    }

}

data class PccMetadata(val type: String, val filename: String)

data class PccFrame(val direction: Direction, val index: Int)
