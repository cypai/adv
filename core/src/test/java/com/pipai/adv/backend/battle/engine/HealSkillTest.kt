package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.log.HealEvent
import com.pipai.adv.domain.NpcList
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class HealSkillTest : GdxMockedTest() {

    @Test
    fun testHeal() {
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        target.unitInstance.hp = 1
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = generateBackend(npcList, map)

        val cmd = TargetSkillCommand(backend.skillIndex.getSkillSchema("Heal")!!.new(), attackerId, targetId)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.any { it -> it is HealEvent })

        val healEvents = events.filter { it -> it is HealEvent }.map { it as HealEvent }
        for (healEvent in healEvents) {
            Assert.assertEquals(targetId.toLong(), healEvent.npcId.toLong())
        }
        Assert.assertEquals((1 + healEvents.map { it.healAmount }.sum()).toLong(), target.unitInstance.hp.toLong())

        Assert.assertEquals(0, backend.getNpcAp(attackerId))
    }
}
