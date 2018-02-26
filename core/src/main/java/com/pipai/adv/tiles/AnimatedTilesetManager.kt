package com.pipai.adv.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.utils.getLogger

class AnimatedTilesetManager {

    companion object {
        const val TILE_WIDTH = 32
        const val TILE_HEIGHT = 32
    }

    private val logger = getLogger()

    private val tilesets: MutableMap<String, FileTileset> = mutableMapOf()

    fun loadTextures(filenames: List<String>) {
        for (filename in filenames) {
            if (!tilesets.containsKey(filename)) {
                val file = Gdx.files.local("assets/binassets/graphics/tilesets/$filename")
                if (file.exists()) {
                    tilesets.put(filename, FileTileset(file, TILE_WIDTH, TILE_HEIGHT))
                } else {
                    logger.warn("Unable to load $filename because ${file.path()} does not exist")
                }
            }
        }
    }

    fun unloadTextures(StringList: List<String>) {
        for (filename in StringList) {
            if (tilesets.containsKey(filename)) {
                tilesets[filename]!!.dispose()
                tilesets.remove(filename)
            }
        }
    }

    fun tilesetIsLoaded(filename: String): Boolean = tilesets.contains(filename)

    fun getTilesetFrame(filename: String, frame: UnitAnimationFrame): TextureRegion {
        if (tilesets.containsKey(filename)) {
            val tileset = tilesets[filename]!!
            return tileset.tile(TilePosition(frame.index, directionToRow(frame.direction)))
        } else {
            throw IllegalArgumentException("$filename has not yet been loaded")
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
        for (kv in tilesets) {
            kv.value.dispose()
        }
    }

}
