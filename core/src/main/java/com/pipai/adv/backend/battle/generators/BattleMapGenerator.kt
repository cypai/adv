package com.pipai.adv.backend.battle.generators

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.BattleMapCellSpecialFlag.Exit
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.domain.FullEnvObject.DestructibleEnvObject
import com.pipai.adv.backend.battle.domain.FullEnvObject.DestructibleEnvObjectType
import com.pipai.adv.backend.battle.domain.FullEnvObject.DestructibleEnvObjectType.ROCK
import com.pipai.adv.backend.battle.domain.FullEnvObject.DestructibleEnvObjectType.TREE
import com.pipai.adv.utils.RNG

internal val EXIT = Exit()

interface BattleMapGenerator {
    fun generate(width: Int, height: Int): BattleMap
}

class OpenBattleMapGenerator : BattleMapGenerator {

    var trees: Int = 0
    var rocks: Int = 0

    override fun generate(width: Int, height: Int): BattleMap {
        val map = BattleMap.createBattleMap(width, height)
        repeat(trees, {
            val tree = createDefaultDestructibleFullEnvironmentObject(TREE)
            addFullEnvironmentObjectToRandomPosition(map, tree)
        })
        repeat(rocks, {
            val rock = createDefaultDestructibleFullEnvironmentObject(ROCK)
            addFullEnvironmentObjectToRandomPosition(map, rock)
        })
        addExitsToPerimeter(map)
        return map
    }

}

fun addExitsToPerimeter(map: BattleMap) {
    val width = map.width
    val height = map.height
    for (x in 0 until width) {
        map.getCell(x, 0).specialFlags.add(EXIT)
        map.getCell(x, height - 1).specialFlags.add(EXIT)
    }
    for (y in 1 until height - 1) {
        map.getCell(0, y).specialFlags.add(EXIT)
        map.getCell(width - 1, y).specialFlags.add(EXIT)
    }
}

fun createDefaultDestructibleFullEnvironmentObject(type: DestructibleEnvObjectType): DestructibleEnvObject {
    val min = type.defaultMinHp
    val max = type.defaultMaxHp
    val hp = min + RNG.nextInt(max - min)
    return DestructibleEnvObject(type, hp, EnvObjTilesetMetadata.NONE)
}

fun addFullEnvironmentObjectToRandomPosition(map: BattleMap, envObj: FullEnvObject) {
    val width = map.width
    val height = map.height

    var added = false
    while (!added) {
        val x = RNG.nextInt(width)
        val y = RNG.nextInt(height)
        val cell = map.getCell(x, y)
        if (cell.fullEnvObject == null) {
            cell.fullEnvObject = envObj
            added = true
        }
    }
}
