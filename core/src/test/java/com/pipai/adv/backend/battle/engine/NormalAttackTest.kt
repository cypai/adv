package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.NormalAttackEvent
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import org.junit.Assert
import org.junit.Test

class NormalAttackTest {

    @Test
    fun testMeleeNormalAttack() {
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

        val cmd = NormalAttackCommand(attackerId, targetId)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.stream().anyMatch { it -> it is DamageEvent })
        val (npcId, _, damage) = events.stream().filter { it -> it is DamageEvent }.findFirst().get() as DamageEvent
        Assert.assertEquals(targetId.toLong(), npcId.toLong())
        Assert.assertEquals((100 - damage).toLong(), target.unitInstance.hp.toLong())

        Assert.assertEquals(0, backend.getNpcAp(attackerId))
    }

    @Test
    fun testMeleeNormalAttackOutOfRange() {
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

        val cmd = NormalAttackCommand(attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }

    @Test
    fun testBowRangedNormalAttack() {
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

        val cmd = NormalAttackCommand(attackerId, targetId)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.stream().anyMatch { it -> it is NormalAttackEvent })
        val (attackerId1, _, targetId1, _, weapon) = events.stream().filter { it -> it is NormalAttackEvent }.findFirst().get() as NormalAttackEvent
        Assert.assertEquals(attackerId.toLong(), attackerId1.toLong())
        Assert.assertEquals(targetId.toLong(), targetId1.toLong())
        Assert.assertEquals(bow, weapon)

        Assert.assertTrue(events.stream().anyMatch { it -> it is DamageEvent })
        val (npcId, _, damage) = events.stream().filter { it -> it is DamageEvent }.findFirst().get() as DamageEvent
        Assert.assertEquals(targetId.toLong(), npcId.toLong())
        Assert.assertEquals((100 - damage).toLong(), target.unitInstance.hp.toLong())

        Assert.assertEquals(0, backend.getNpcAp(attackerId))
    }

    @Test
    fun testBowRangedNormalAttackOutOfRange() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(BattleBackend.RANGED_WEAPON_DISTANCE.toInt() + 1, 1)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(BattleBackend.RANGED_WEAPON_DISTANCE.toInt(), 0).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = NormalAttackCommand(attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }

    @Test
    fun testBowOutOfAmmoNormalAttack() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val bow = bowFixture()
        bow.ammo = 0
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(2, 0).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = NormalAttackCommand(attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("This weapon needs to be reloaded", reason)
    }
}
