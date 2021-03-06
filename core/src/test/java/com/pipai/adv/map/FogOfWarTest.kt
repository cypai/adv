package com.pipai.adv.map

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.GridUtils
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class FogOfWarTest : GdxMockedTest() {

    @Test
    fun testVisibilitySemiWalledMap() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(10, 10)
        GridUtils.boundaries(GridPosition(0, 0), GridPosition(9, 9)).forEach {
            if (!(it.x in 1..3 && it.y == 0)) {
                map.getCell(it).fullEnvObjId = envObjList.add(FullWall(FullWallType.SOLID))
            }
        }
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val playerId = npcList.add(player)
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

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
