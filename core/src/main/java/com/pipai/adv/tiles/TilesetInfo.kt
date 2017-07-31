package com.pipai.adv.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.utils.split

enum class MapTileType {
    // Walls
    WALL,

    // Ground types
    GROUND,

    // Ground decorations
    GROUND_DECO
}

// Encodes tileset offsets
data class TilePosition(val x: Int, val y: Int)

interface Tileset {
    fun dispose()

    fun tileWidth(): Int
    fun tileHeight(): Int

    fun tile(tilePosition: TilePosition): TextureRegion
}

class FileTileset(private val tilesetFile: FileHandle, private val width: Int, private val height: Int) : Tileset {

    private val tilesetTexture = Texture(tilesetFile)
    private val tiles: Array<Array<TextureRegion>>

    init {
        tiles = TextureRegion.split(tilesetTexture, width, height)
    }

    override fun dispose() {
        tilesetTexture.dispose()
    }

    override fun tileWidth() = width
    override fun tileHeight() = height

    override fun tile(tilePosition: TilePosition): TextureRegion {
        return tiles[tilePosition.y][tilePosition.x]
    }
}

interface MapTileset : Tileset {
    fun tilePositions(tileType: MapTileType): List<TilePosition>
    fun tiles(tileType: MapTileType): List<TextureRegion>
}

class GrassyTileset(private val tilesetFile: FileHandle) : MapTileset {

    private val tilesetTexture = Texture(tilesetFile)
    private val tiles: Array<Array<TextureRegion>>

    init {
        tiles = tilesetTexture.split(32, 32, 1, 1);
    }

    override fun dispose() {
        tilesetTexture.dispose()
    }

    override fun tileWidth(): Int = 32
    override fun tileHeight(): Int = 32

    override fun tilePositions(tileType: MapTileType): List<TilePosition> {
        return when (tileType) {
            MapTileType.WALL -> listOf(TilePosition(4, 3))

            MapTileType.GROUND -> listOf(TilePosition(1, 2))

            MapTileType.GROUND_DECO -> listOf(
                    TilePosition(1, 8), TilePosition(1, 9),
                    TilePosition(1, 10), TilePosition(1, 11),
                    TilePosition(2, 8), TilePosition(2, 9),
                    TilePosition(2, 10), TilePosition(2, 11))
        }
    }

    override fun tiles(tileType: MapTileType): List<TextureRegion> {
        return tilePositions(tileType).map { it -> tile(it) }
    }

    override fun tile(tilePosition: TilePosition): TextureRegion {
        return tiles[tilePosition.y][tilePosition.x]
    }
}
