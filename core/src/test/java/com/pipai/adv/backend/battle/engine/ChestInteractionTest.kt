package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.InteractCommand
import com.pipai.adv.backend.battle.engine.log.CellStateEvent
import com.pipai.adv.domain.NpcList
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class ChestInteractionTest : GdxMockedTest() {

    @Test
    fun testChestInteraction() {
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        player.unitInstance.inventory.add(InventorySlot(null))
        val playerId = npcList.addNpc(player)
        val chestItem = bowFixture()
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(0, 1).fullEnvObject = FullEnvObject.ChestEnvObject(chestItem, EnvObjTilesetMetadata.NONE)

        val backend = generateBackend(npcList, map)
        val interactCommand = InteractCommand(playerId, GridPosition(0, 1))
        Assert.assertTrue(backend.canBeExecuted(interactCommand).executable)
        val events = backend.execute(interactCommand)

        Assert.assertEquals(1, backend.getNpcAp(playerId))
        Assert.assertTrue(player.unitInstance.inventory.any { it.item == chestItem })
        Assert.assertNull(map.getCell(0, 1).fullEnvObject)
        val cellStateEvent = events.first { it is CellStateEvent } as CellStateEvent
        Assert.assertNull(cellStateEvent.cell.fullEnvObject)
    }

    @Test
    fun testChestInteractionFullInventory() {
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val playerId = npcList.addNpc(player)
        val chestItem = bowFixture()
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(0, 1).fullEnvObject = FullEnvObject.ChestEnvObject(chestItem, EnvObjTilesetMetadata.NONE)

        val backend = generateBackend(npcList, map)
        val interactCommand = InteractCommand(playerId, GridPosition(0, 1))
        val executableStatus = backend.canBeExecuted(interactCommand)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Their inventory is full", executableStatus.reason)
    }
}
