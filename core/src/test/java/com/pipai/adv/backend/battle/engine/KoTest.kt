package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DevHpChangeCommand
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.NpcKoEvent
import com.pipai.adv.backend.battle.engine.log.PlayerKoEvent
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.*
import org.junit.Assert
import org.junit.Test

class KoTest {

    @Test
    fun testPlayerKo() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val playerId = npcList.addNpc(player)
        val enemy = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val enemyId = npcList.addNpc(enemy)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = DevHpChangeCommand(playerId, 0)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.stream().anyMatch { it -> it is DamageEvent })
        val (npcId) = events.stream().filter { it -> it is DamageEvent }.findFirst().get() as DamageEvent
        Assert.assertEquals(playerId.toLong(), npcId.toLong())
        Assert.assertEquals(0, player.unitInstance.hp.toLong())

        Assert.assertTrue(events.stream().anyMatch { it -> it is PlayerKoEvent })
        val (npcId1) = events.stream().filter { it -> it is PlayerKoEvent }.findFirst().get() as PlayerKoEvent
        Assert.assertEquals(playerId.toLong(), npcId1.toLong())

        val envObj = map.getCell(0, 0).fullEnvObject
        Assert.assertTrue(envObj is FullEnvObject.NpcEnvObject)
        Assert.assertEquals(playerId.toLong(), (envObj as FullEnvObject.NpcEnvObject).npcId.toLong())
    }

    @Test
    fun testEnemyKo() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val playerId = npcList.addNpc(player)
        val enemy = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val enemyId = npcList.addNpc(enemy)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val cmd = DevHpChangeCommand(enemyId, 0)

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        val events = backend.execute(cmd)
        Assert.assertTrue(events.stream().anyMatch { it -> it is DamageEvent })
        val (npcId) = events.stream().filter { it -> it is DamageEvent }.findFirst().get() as DamageEvent
        Assert.assertEquals(enemyId.toLong(), npcId.toLong())
        Assert.assertEquals(0, enemy.unitInstance.hp.toLong())

        Assert.assertTrue(events.stream().anyMatch { it -> it is NpcKoEvent })
        val (npcId1) = events.stream().filter { it -> it is NpcKoEvent }.findFirst().get() as NpcKoEvent
        Assert.assertEquals(enemyId.toLong(), npcId1.toLong())
        Assert.assertEquals(null, map.getCell(1, 1).fullEnvObject)
    }
}
