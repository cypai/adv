package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.RunCommand
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.EndingType
import com.pipai.adv.backend.battle.generators.EXIT
import com.pipai.adv.domain.NpcList
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class RunTest : GdxMockedTest() {

    @Test
    fun testRun() {
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val player1 = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val player1Id = npcList.addNpc(player1)
        val player2 = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val player2Id = npcList.addNpc(player2)
        map.getCell(0, 0).specialFlags.add(EXIT)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(player1Id, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(player2Id, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = generateBackend(npcList, map)
        var runCommand = RunCommand(player2Id)
        var runPreview = backend.canBeExecuted(runCommand)
        Assert.assertFalse(runPreview.executable)
        Assert.assertEquals("Not on exit tile", runPreview.reason)

        runCommand = RunCommand(player1Id)
        runPreview = backend.canBeExecuted(runCommand)
        Assert.assertTrue(runPreview.executable)
        val events = backend.execute(runCommand)
        Assert.assertTrue(events.any { it is BattleEndEvent && it.endingType == EndingType.RAN_AWAY })
    }
}
