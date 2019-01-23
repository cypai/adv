package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class DoubleSlashTest : GdxMockedTest() {

    @Test
    fun testDoubleSlash() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 10, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = TargetSkillCommand(backend.skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        val preview = backend.preview(cmd)
        val stages = preview.filter { it is TargetStagePreviewComponent }
                .map { it as TargetStagePreviewComponent }
        Assert.assertTrue(stages.all { it.previews.filter { it.description == "Melee weapon" }.size == 2 })

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.any { it -> it is DamageEvent })

        val damageEvents = events.filter { it -> it is DamageEvent }.map { it as DamageEvent }
        for (damageEvent in damageEvents) {
            Assert.assertEquals(targetId.toLong(), damageEvent.npcId.toLong())
        }
        Assert.assertEquals((100 - damageEvents.map { it.damage }.sum()).toLong(), target.unitInstance.hp.toLong())

        Assert.assertEquals(0, backend.getNpcAp(attackerId))
        Assert.assertEquals(5, backend.getNpcTp(attackerId))
    }

    @Test
    fun testDoubleSlashOutOfRange() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(2, 0).fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = TargetSkillCommand(backend.skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }

    @Test
    fun testDoubleSlashBow() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val bow = bowFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(2, 0).fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = TargetSkillCommand(backend.skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacker is not wielding a weapon suitable for this skill", reason)
    }
}
