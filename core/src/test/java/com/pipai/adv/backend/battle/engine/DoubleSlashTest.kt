package com.pipai.adv.backend.battle.engine

import com.badlogic.gdx.Gdx
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.index.SkillIndex
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import org.junit.Assert
import org.junit.Test

class DoubleSlashTest : GdxMockedTest() {

    @Test
    fun testDoubleSlash() {
        val skillIndex = SkillIndex(Gdx.files.internal("data/skills.csv"))
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = TargetSkillCommand(skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.any { it -> it is DamageEvent })

        val damageEvents = events.filter { it -> it is DamageEvent }.map { it as DamageEvent }
        for (damageEvent in damageEvents) {
            Assert.assertEquals(targetId.toLong(), damageEvent.npcId.toLong())
        }
        Assert.assertEquals((100 - damageEvents.map { it.damage }.sum()).toLong(), target.unitInstance.hp.toLong())

        Assert.assertEquals(0, backend.getNpcAp(attackerId))
    }

    @Test
    fun testDoubleSlashOutOfRange() {
        val skillIndex = SkillIndex(Gdx.files.internal("data/skills.csv"))
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(2, 0).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = TargetSkillCommand(skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }

    @Test
    fun testDoubleSlashBow() {
        val skillIndex = SkillIndex(Gdx.files.internal("data/skills.csv"))
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val bow = bowFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(2, 0).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = TargetSkillCommand(skillIndex.getSkillSchema("Double Slash")!!.new(), attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacker is not wielding a weapon suitable for this skill", reason)
    }
}
