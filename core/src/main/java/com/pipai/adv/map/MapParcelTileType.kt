package com.pipai.adv.map

/**
 * Enum of tile types seen in MapParcels. Each tile represents a fullEnvObj.
 */
enum class MapParcelTileType(val encoding: Int) {
    HALF_COVER(1),
    FULL_COVER(2),
    TREE(3),
    WALL(4)
}
