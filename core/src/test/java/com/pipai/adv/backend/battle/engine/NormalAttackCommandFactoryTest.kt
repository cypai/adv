package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommandFactory
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import org.junit.Assert
import org.junit.Test

class NormalAttackCommandFactoryTest {

    @Test
    fun testSwordNormalAttackCommandFactory() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val player = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), sword)
        npcList.addNpc(player)
        val playerId = npcList.addNpc(player)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val enemy = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), sword)
        npcList.addNpc(enemy)
        val enemyId = npcList.addNpc(player)
        map.getCell(1, 0).fullEnvObject = FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE)

        val outOfRangeEnemy = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), sword)
        npcList.addNpc(outOfRangeEnemy)
        val outOfRangeEnemyId = npcList.addNpc(player)
        map.getCell(3, 0).fullEnvObject = FullEnvObject.NpcEnvObject(outOfRangeEnemyId, Team.AI, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val factory = NormalAttackCommandFactory(backend)
        val playerCommands = factory.generate(playerId)
        Assert.assertEquals(1, playerCommands.size)
        Assert.assertTrue(backend.canBeExecuted(playerCommands[0]).executable)
        Assert.assertEquals(playerId, playerCommands[0].unitId)
        Assert.assertEquals(enemyId, playerCommands[0].targetId)

        val enemyCommands = factory.generate(enemyId)
        Assert.assertEquals(1, enemyCommands.size)
        Assert.assertTrue(backend.canBeExecuted(enemyCommands[0]).executable)
        Assert.assertEquals(enemyId, enemyCommands[0].unitId)
        Assert.assertEquals(playerId, enemyCommands[0].targetId)

        val outOfRangeEnemyCommands = factory.generate(outOfRangeEnemyId)
        Assert.assertEquals(0, outOfRangeEnemyCommands.size)
    }

    @Test
    fun testBowNormalAttackCommandFactory() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val bow = bowFixture()
        val player = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), bow)
        npcList.addNpc(player)
        val playerId = npcList.addNpc(player)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val enemy1 = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), bow)
        npcList.addNpc(enemy1)
        val enemy1Id = npcList.addNpc(player)
        map.getCell(1, 0).fullEnvObject = FullEnvObject.NpcEnvObject(enemy1Id, Team.AI, EnvObjTilesetMetadata.NONE)

        val enemy2 = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 2), bow)
        npcList.addNpc(enemy2)
        val enemy2Id = npcList.addNpc(player)
        map.getCell(3, 0).fullEnvObject = FullEnvObject.NpcEnvObject(enemy2Id, Team.AI, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val factory = NormalAttackCommandFactory(backend)
        val playerCommands = factory.generate(playerId)
        Assert.assertEquals(2, playerCommands.size)
        playerCommands.forEach { Assert.assertTrue(backend.canBeExecuted(it).executable) }
        Assert.assertTrue(playerCommands.map { it.unitId }.all { it == playerId })
        Assert.assertTrue(playerCommands.map { it.targetId }.containsAll(listOf(enemy1Id, enemy2Id)))

        val enemy1Commands = factory.generate(enemy1Id)
        Assert.assertEquals(1, enemy1Commands.size)
        Assert.assertTrue(backend.canBeExecuted(enemy1Commands[0]).executable)
        Assert.assertEquals(enemy1Id, enemy1Commands[0].unitId)
        Assert.assertEquals(playerId, enemy1Commands[0].targetId)

        val enemy2Commands = factory.generate(enemy2Id)
        Assert.assertEquals(1, enemy2Commands.size)
        Assert.assertTrue(backend.canBeExecuted(enemy2Commands[0]).executable)
        Assert.assertEquals(enemy2Id, enemy2Commands[0].unitId)
        Assert.assertEquals(playerId, enemy2Commands[0].targetId)
    }
}
