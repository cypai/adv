package com.pipai.adv.map

data class WorldMap(val villageLocations: MutableMap<String, WorldMapLocation>)

/**
 * x and y here are pixel positions on the world map texture
 */
data class WorldMapLocation(val x: Int, val y: Int)
