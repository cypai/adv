package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.npcFromStats
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.math.exp

class MoveApTest {

    @Test
    fun testBlueMoves() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 4), null)
        val id = npcList.addNpc(npc)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        val blueMove = MoveCommand(id, Arrays.asList(GridPosition(0, 0), GridPosition(0, 1)))
        Assert.assertTrue(backend.canBeExecuted(blueMove).executable)

        val blueMovePreview = backend.preview(blueMove)
        Assert.assertEquals(1,
                (blueMovePreview.find { it is PreviewComponent.ApUsedPreviewComponent }
                        as PreviewComponent.ApUsedPreviewComponent).apUsed)

        backend.execute(blueMove)

        var expectedDestination = GridPosition(0, 1)
        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        var destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObject
        Assert.assertTrue(destinationObj is FullEnvObject.NpcEnvObject)
        var unit = destinationObj as FullEnvObject.NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(1, backend.getNpcAp(id))

        val yellowMove = MoveCommand(id, Arrays.asList(GridPosition(0, 1), GridPosition(0, 2)))
        Assert.assertTrue(backend.canBeExecuted(yellowMove).executable)

        val yellowMovePreview = backend.preview(yellowMove)
        Assert.assertEquals(1,
                (yellowMovePreview.find { it is PreviewComponent.ApUsedPreviewComponent }
                        as PreviewComponent.ApUsedPreviewComponent).apUsed)

        backend.execute(yellowMove)

        expectedDestination = GridPosition(0, 2)
        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObject
        Assert.assertTrue(destinationObj is FullEnvObject.NpcEnvObject)
        unit = destinationObj as FullEnvObject.NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(0, backend.getNpcAp(id))

        val badMove = MoveCommand(id, Arrays.asList(GridPosition(0, 2), GridPosition(0, 3)))
        val executableStatus = backend.canBeExecuted(badMove)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Not enough AP", executableStatus.reason)
    }

    @Test
    fun testYellowMoves() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 4), null)
        val id = npcList.addNpc(npc)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        Assert.assertEquals(2, backend.getNpcAp(id))

        val expectedDestination = GridPosition(0, 3)
        val yellowMove = MoveCommand(id, Arrays.asList(GridPosition(0, 0), expectedDestination))
        Assert.assertTrue(backend.canBeExecuted(yellowMove).executable)

        val yellowMovePreview = backend.preview(yellowMove)
        Assert.assertEquals(2,
                (yellowMovePreview.find { it is PreviewComponent.ApUsedPreviewComponent }
                        as PreviewComponent.ApUsedPreviewComponent).apUsed)

        backend.execute(yellowMove)

        Assert.assertEquals(expectedDestination, backend.getNpcPositions()[id])

        val destinationObj = backend.getBattleMapState().getCell(expectedDestination).fullEnvObject
        Assert.assertTrue(destinationObj is FullEnvObject.NpcEnvObject)
        val unit = destinationObj as FullEnvObject.NpcEnvObject
        Assert.assertEquals(id, unit.npcId)

        Assert.assertEquals(0, backend.getNpcAp(id))

        val badMove = MoveCommand(id, Arrays.asList(expectedDestination, GridPosition(0, 1)))
        val executableStatus = backend.canBeExecuted(badMove)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Not enough AP", executableStatus.reason)
    }
}
