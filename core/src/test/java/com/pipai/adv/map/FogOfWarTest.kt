package com.pipai.adv.map

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.adv.utils.GridUtils
import com.pipai.test.fixtures.npcFromStats
import org.junit.Assert
import org.junit.Test

class FogOfWarTest {

    @Test
    fun testVisibilitySemiWalledMap() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(10, 10)
        GridUtils.boundaries(GridPosition(0, 0), GridPosition(9, 9)).forEach {
            if (!(it.x in 1..3 && it.y == 0)) {
                map.getCell(it).fullEnvObject = FullEnvObject.SOLID_FULL_WALL
            }
        }
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val playerId = npcList.addNpc(player)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        val backend = BattleBackend(save, npcList, map)

        val fogOfWar = FogOfWar()
        fogOfWar.calculateVisibility(backend, playerId, GridPosition(1, 1))

        val corners = listOf(
                GridPosition(0, 9),
                GridPosition(9, 0),
                GridPosition(9, 9))

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val position = GridPosition(x, y)
                if (corners.contains(position)) {
                    continue
                }
                Assert.assertEquals(TileVisibility.VISIBLE, fogOfWar.getPlayerTileVisibility(position))
                Assert.assertTrue(fogOfWar.canSee(playerId, position))
            }
        }
    }
}
