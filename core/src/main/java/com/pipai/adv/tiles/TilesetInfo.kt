package com.pipai.adv.tiles

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.utils.split

enum class MapTileType {
    // Walls by direction (C_WALL is center)
    U_WALL, D_WALL, L_WALL, R_WALL, C_WALL, UL_WALL, UR_WALL, LL_WALL, LR_WALL,

    // Ground types
    GROUND,

    // Ground decorations
    GROUND_DECO
}

// Encodes tileset offsets
data class TilePosition(val x: Int, val y: Int)

interface MapTilesetInfo {
    fun tileWidth(): Int
    fun tileHeight(): Int

    fun tilePositions(tileType: MapTileType): List<TilePosition>
    fun tiles(tileType: MapTileType): List<TextureRegion>
    fun tile(tilePosition: TilePosition): TextureRegion
}

class GrassyTileset(val tilesetFile: FileHandle) : MapTilesetInfo {

    private val tilesetTexture = Texture(tilesetFile)
    private val tiles: Array<Array<TextureRegion>>

    init {
        tiles = tilesetTexture.split(32, 32, 1, 1);
    }

    override fun tileWidth(): Int = 32
    override fun tileHeight(): Int = 32

    override fun tilePositions(tileType: MapTileType): List<TilePosition> {
        return when (tileType) {
            MapTileType.U_WALL, MapTileType.D_WALL, MapTileType.L_WALL, MapTileType.R_WALL, MapTileType.C_WALL,
            MapTileType.UL_WALL, MapTileType.UR_WALL, MapTileType.LL_WALL, MapTileType.LR_WALL -> listOf(TilePosition(4, 3))

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
