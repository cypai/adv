package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.fetch
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test
import java.util.*

class MoveBackendTest : GdxMockedTest() {

    @Test
    fun testMove() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val id = npcList.add(npc)
        map.getCell(2, 1).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = MoveCommand(id, Arrays.asList(GridPosition(2, 1), GridPosition(3, 1)))

        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        backend.execute(cmd)

        val expectedDestination = GridPosition(3, 1)
        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        val destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObjId

        Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)

        val unit = destinationObj.fetch(envObjList) as NpcEnvObject

        Assert.assertEquals(id.toLong(), unit.npcId.toLong())
    }

    @Test
    fun testCantMoveToFullSpace() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        map.getCell(3, 1).fullEnvObjId = envObjList.add(FullWall(FullWallType.SOLID))
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val id = npcList.add(npc)
        map.getCell(2, 1).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val cmd = MoveCommand(id, Arrays.asList(GridPosition(2, 1), GridPosition(3, 1)))

        val executable = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable.executable)
        Assert.assertEquals("Destination is not empty", executable.reason)

        try {
            backend.execute(cmd)
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue(e.message!!.endsWith(executable.reason!!))
        }

        val expectedLocation = GridPosition(2, 1)
        Assert.assertEquals(expectedLocation, backend.getNpcPositions()[id])

        val destinationObj = backend.getBattleMapState().getCell(expectedLocation).fullEnvObjId

        Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)

        val unit = destinationObj.fetch(envObjList) as NpcEnvObject

        Assert.assertEquals(id.toLong(), unit.npcId.toLong())
    }

    @Test
    fun testCantMoveMoreThanApAllow() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val id = npcList.add(npc)
        map.getCell(2, 1).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        var cmd = MoveCommand(id, Arrays.asList(GridPosition(2, 1), GridPosition(3, 1)))
        Assert.assertTrue(backend.canBeExecuted(cmd).executable)
        backend.execute(cmd)

        cmd = MoveCommand(id, Arrays.asList(GridPosition(3, 1), GridPosition(3, 2)))
        Assert.assertTrue(backend.canBeExecuted(cmd).executable)
        backend.execute(cmd)

        cmd = MoveCommand(id, Arrays.asList(GridPosition(3, 2), GridPosition(3, 3)))
        val executable = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable.executable)
        Assert.assertEquals("Not enough AP", executable.reason)
    }

    @Test
    fun testCantMoveNonexistentNpc() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val id = npcList.add(npc)
        map.getCell(2, 1).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val badId = id + 1
        val cmd = MoveCommand(badId, Arrays.asList(GridPosition(2, 1), GridPosition(3, 1)))
        val executable = backend.canBeExecuted(cmd)
        Assert.assertFalse(executable.executable)
        Assert.assertEquals("Npc $badId does not exist", executable.reason)
    }

    @Test
    fun testCantMoveToOutOfMap() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(1, 1)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val id = npcList.add(npc)
        val startLocation = GridPosition(0, 0)
        map.getCell(startLocation).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val badLocations: List<GridPosition> = listOf(
                GridPosition(0, 1),
                GridPosition(1, 0),
                GridPosition(-1, 0),
                GridPosition(0, -1))
        badLocations.forEach {
            val cmd = MoveCommand(id, Arrays.asList(startLocation, it))

            val executable = backend.canBeExecuted(cmd)
            Assert.assertFalse(executable.executable)
            Assert.assertEquals("Cannot move off the map", executable.reason)

            try {
                backend.execute(cmd)
                Assert.fail()
            } catch (e: IllegalArgumentException) {
                Assert.assertTrue(e.message!!.endsWith(executable.reason!!))
            }

            Assert.assertEquals(startLocation, backend.getNpcPositions()[id])
            val destinationObj = backend.getBattleMapState().getCell(startLocation).fullEnvObjId
            Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)
            val unit = destinationObj.fetch(envObjList) as NpcEnvObject
            Assert.assertEquals(id.toLong(), unit.npcId.toLong())
        }
    }
}
