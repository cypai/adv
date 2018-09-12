package com.pipai.adv.map

import com.badlogic.gdx.math.Vector2

data class WorldMap(val villageLocations: MutableMap<String, WorldMapLocation>)

/**
 * x and y here are pixel positions on the world map texture
 */
data class WorldMapLocation(val x: Int, val y: Int) {
    fun toVector2() = Vector2(x.toFloat(), y.toFloat())
}

