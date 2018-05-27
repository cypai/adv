package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.MoveCommandFactory
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.npcFromStats
import org.junit.Assert
import org.junit.Test

class MoveCommandFactoryTest {

    @Test
    fun testMoveFactory() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), null)
        val id = npcList.addNpc(npc)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val factory = MoveCommandFactory(backend)
        val moves = factory.generate(id)
        Assert.assertEquals(5, moves.size)

        moves.forEach {
            Assert.assertTrue(backend.canBeExecuted(it).executable)
        }
    }
}
