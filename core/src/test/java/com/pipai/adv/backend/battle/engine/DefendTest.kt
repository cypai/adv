package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DefendCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.NpcStatus
import com.pipai.adv.backend.battle.engine.domain.ToCritFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitFlatAdjustmentPreviewComponent
import com.pipai.adv.domain.NpcList
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class DefendTest : GdxMockedTest() {

    @Test
    fun testDefend() {
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val playerId = npcList.addNpc(player)
        val enemy = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val enemyId = npcList.addNpc(enemy)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE)

        val backend = generateBackend(npcList, map)
        val defendCommand = DefendCommand(playerId)
        Assert.assertTrue(backend.canBeExecuted(defendCommand).executable)
        backend.execute(defendCommand)
        Assert.assertEquals(0, backend.getNpcAp(playerId))
        Assert.assertTrue(backend.checkNpcStatus(playerId, NpcStatus.DEFENDING))

        backend.endTurn()

        val attackCommand = NormalAttackCommand(enemyId, playerId)
        Assert.assertTrue(backend.canBeExecuted(attackCommand).executable)
        Assert.assertTrue(backend.preview(attackCommand)
                .filter { it is ToHitFlatAdjustmentPreviewComponent }
                .any { (it as ToHitFlatAdjustmentPreviewComponent).adjustment < 0 && it.description == "Defending" })
        Assert.assertTrue(backend.preview(attackCommand)
                .filter { it is ToCritFlatAdjustmentPreviewComponent }
                .any { (it as ToCritFlatAdjustmentPreviewComponent).adjustment < 0 && it.description == "Defending" })
        backend.execute(attackCommand)
        Assert.assertEquals(0, backend.getNpcAp(enemyId))

        backend.endTurn()

        Assert.assertEquals(2, backend.getNpcAp(playerId))
        Assert.assertFalse(backend.checkNpcStatus(playerId, NpcStatus.DEFENDING))
    }
}
