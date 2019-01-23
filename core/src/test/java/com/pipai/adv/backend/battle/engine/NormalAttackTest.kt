package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.NormalAttackEvent
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class NormalAttackTest : GdxMockedTest() {

    @Test
    fun testMeleeNormalAttack() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

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

        val cmd = NormalAttackCommand(attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }

    @Test
    fun testBowRangedNormalAttack() {
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
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(BattleBackend.RANGED_WEAPON_DISTANCE.toInt() + 1, 1)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(BattleBackend.RANGED_WEAPON_DISTANCE.toInt(), 0)
                .fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = NormalAttackCommand(attackerId, targetId)

        val (executable, reason) = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable)
        Assert.assertEquals("Attacking distance is too great", reason)
    }
}
