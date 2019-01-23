package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.MoveCommandFactory
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class MoveCommandFactoryTest : GdxMockedTest() {

    @Test
    fun testMoveFactory() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), null)
        val id = npcList.add(npc)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val factory = MoveCommandFactory(backend)
        val moves = factory.generate(id)
        Assert.assertEquals(5, moves.size)

        moves.forEach {
            Assert.assertTrue(backend.canBeExecuted(it).executable)
        }
    }
}
