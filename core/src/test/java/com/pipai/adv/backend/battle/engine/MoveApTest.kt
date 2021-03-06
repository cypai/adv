package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.fetch
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test
import java.util.*

class MoveApTest : GdxMockedTest() {

    @Test
    fun testBlueMoves() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 4), null)
        val id = npcList.add(npc)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val blueMove = MoveCommand(id, Arrays.asList(GridPosition(0, 0), GridPosition(0, 1)))
        Assert.assertTrue(backend.canBeExecuted(blueMove).executable)

        val blueMovePreview = backend.preview(blueMove)
        Assert.assertEquals(1,
                (blueMovePreview.find { it is ApUsedPreviewComponent }
                        as ApUsedPreviewComponent).apUsed)

        backend.execute(blueMove)

        var expectedDestination = GridPosition(0, 1)
        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        var destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObjId
        Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)
        var unit = destinationObj.fetch(envObjList) as NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(1, backend.getNpcAp(id))

        val yellowMove = MoveCommand(id, Arrays.asList(GridPosition(0, 1), GridPosition(0, 2)))
        Assert.assertTrue(backend.canBeExecuted(yellowMove).executable)

        val yellowMovePreview = backend.preview(yellowMove)
        Assert.assertEquals(1,
                (yellowMovePreview.find { it is ApUsedPreviewComponent }
                        as ApUsedPreviewComponent).apUsed)

        backend.execute(yellowMove)

        expectedDestination = GridPosition(0, 2)
        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObjId
        Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)
        unit = destinationObj.fetch(envObjList) as NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(0, backend.getNpcAp(id))

        val badMove = MoveCommand(id, Arrays.asList(GridPosition(0, 2), GridPosition(0, 3)))
        val executableStatus = backend.canBeExecuted(badMove)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Not enough AP", executableStatus.reason)
    }

    @Test
    fun testYellowMoves() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 4), null)
        val id = npcList.add(npc)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        Assert.assertEquals(2, backend.getNpcAp(id))

        val expectedDestination = GridPosition(0, 3)
        val yellowMove = MoveCommand(id, Arrays.asList(GridPosition(0, 0), expectedDestination))
        Assert.assertTrue(backend.canBeExecuted(yellowMove).executable)

        val yellowMovePreview = backend.preview(yellowMove)
        Assert.assertEquals(2,
                (yellowMovePreview.find { it is ApUsedPreviewComponent }
                        as ApUsedPreviewComponent).apUsed)

        backend.execute(yellowMove)

        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        val destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObjId
        Assert.assertTrue(destinationObj.fetch(envObjList) is NpcEnvObject)
        val unit = destinationObj.fetch(envObjList) as NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(0, backend.getNpcAp(id))

        val badMove = MoveCommand(id, Arrays.asList(expectedDestination, GridPosition(0, 1)))
        val executableStatus = backend.canBeExecuted(badMove)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Not enough AP", executableStatus.reason)
    }
}
