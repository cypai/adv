package com.pipai.adv.tiles

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

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
    fun tilePositions(tileType: MapTileType): List<TilePosition>
    fun tile(tilePosition: TilePosition): TextureRegion
}

class GrassyTileset(val tilesetFile: FileHandle) : MapTilesetInfo {

    private val tilesetTexture = Texture(tilesetFile)
    private val tiles: Array<Array<TextureRegion>>

    init {
        tiles = TextureRegion.split(tilesetTexture, 33, 33);
    }

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

    override fun tile(tilePosition: TilePosition): TextureRegion {
        return tiles[tilePosition.x][tilePosition.y]
    }
}
