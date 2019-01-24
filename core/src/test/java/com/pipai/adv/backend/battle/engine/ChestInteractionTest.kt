package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.InteractCommand
import com.pipai.adv.backend.battle.engine.log.EnvObjectDestroyEvent
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.fetch
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class ChestInteractionTest : GdxMockedTest() {

    @Test
    fun testChestInteraction() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        player.unitInstance.inventory.add(InventorySlot(null))
        val playerId = npcList.add(player)
        val chestItem = bowFixture()
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        val chestId = envObjList.add(ChestEnvObject(chestItem, EnvObjTilesetMetadata.NONE))
        map.getCell(0, 1).fullEnvObjId = chestId

        val backend = generateBackend(npcList, envObjList, map)
        val interactCommand = InteractCommand(playerId, GridPosition(0, 1))
        Assert.assertTrue(backend.canBeExecuted(interactCommand).executable)
        val events = backend.execute(interactCommand)

        Assert.assertEquals(1, backend.getNpcAp(playerId))
        Assert.assertTrue(player.unitInstance.inventory.any { it.item == chestItem })
        Assert.assertNull(map.getCell(0, 1).fullEnvObjId)
        val envObjDestroyEvent = events.first { it is EnvObjectDestroyEvent } as EnvObjectDestroyEvent
        Assert.assertEquals(chestId, envObjDestroyEvent.envObjId)
        Assert.assertEquals(chestId.fetch(envObjList), envObjDestroyEvent.envObj)
    }

    @Test
    fun testChestInteractionFullInventory() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val playerId = npcList.add(player)
        val chestItem = bowFixture()
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(0, 1).fullEnvObjId = envObjList.add(ChestEnvObject(chestItem, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)
        val interactCommand = InteractCommand(playerId, GridPosition(0, 1))
        val executableStatus = backend.canBeExecuted(interactCommand)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Their inventory is full", executableStatus.reason)
    }
}
