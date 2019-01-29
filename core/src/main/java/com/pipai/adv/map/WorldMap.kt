package com.pipai.adv.map

import com.badlogic.gdx.math.Vector2

class WorldMap {

    private val pois: MutableMap<String, PointOfInterest> = mutableMapOf()

    fun getPoi(name: String) = pois[name]!!

    fun getAllPois() = pois.values.toList()

    fun addPoi(poi: PointOfInterest) {
        pois[poi.name] = poi
    }

    fun removePoi(name: String) {
        pois.remove(name)
    }
}

/**
 * x and y here are pixel positions on the world map texture
 */
data class WorldMapLocation(val x: Int, val y: Int) {
    constructor(x: Float, y: Float) : this(x.toInt(), y.toInt())

    fun toVector2() = Vector2(x.toFloat(), y.toFloat())
}

data class PointOfInterest(val name: String, val type: PointOfInterestType, val location: WorldMapLocation)

enum class PointOfInterestType {
    VILLAGE, DUNGEON, QUEST_DUNGEON
}
