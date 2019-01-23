package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.CoverType
import com.pipai.adv.backend.battle.engine.domain.ToCritFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitFlatAdjustmentPreviewComponent
import com.pipai.adv.domain.Npc
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.bowFixture
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class CoverNormalAttackTest : GdxMockedTest() {

    @Test
    fun testNormalAttackCover() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        /*
         * Map looks like:
         * A 0 - D
         * 0 1 0 0
         * 0 D 0 0
         * D 0 0 0
         */
        val map = BattleMap.createBattleMap(4, 4)
        map.getCell(1, 2).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
        map.getCell(2, 3).fullEnvObjId = envObjList.add(DestructibleEnvObject(10, CoverType.HALF, EnvObjTilesetMetadata.MapTilesetMetadata(MapTileType.WALL)))

        val bow = bowFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow)
        val attackerId = npcList.add(attacker)
        val fullCoverTarget = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val fullTargetId = npcList.add(fullCoverTarget)
        val halfCoverTarget = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val halfTargetId = npcList.add(halfCoverTarget)
        val openCoverTarget = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val openTargetId = npcList.add(openCoverTarget)
        map.getCell(0, 3).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(fullTargetId, Team.AI, EnvObjTilesetMetadata.NONE))
        map.getCell(3, 3).fullEnvObjId = envObjList.add(NpcEnvObject(halfTargetId, Team.AI, EnvObjTilesetMetadata.NONE))
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(openTargetId, Team.AI, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        var cmd = NormalAttackCommand(attackerId, fullTargetId)
        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        var preview = backend.preview(cmd)
        Assert.assertTrue(preview.any { it is ToHitFlatAdjustmentPreviewComponent && it.description == "Full Cover" })
        Assert.assertTrue(preview.any { it is ToCritFlatAdjustmentPreviewComponent && it.description == "Full Cover" })

        cmd = NormalAttackCommand(attackerId, halfTargetId)
        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        preview = backend.preview(cmd)
        Assert.assertTrue(preview.any { it is ToHitFlatAdjustmentPreviewComponent && it.description == "Half Cover" })
        Assert.assertTrue(preview.any { it is ToCritFlatAdjustmentPreviewComponent && it.description == "Half Cover" })

        cmd = NormalAttackCommand(attackerId, openTargetId)
        Assert.assertTrue(backend.canBeExecuted(cmd).executable)

        preview = backend.preview(cmd)
        Assert.assertTrue(preview.none { it is ToHitFlatAdjustmentPreviewComponent && it.description == "Full Cover" })
        Assert.assertTrue(preview.none { it is ToCritFlatAdjustmentPreviewComponent && it.description == "Full Cover" })
        Assert.assertTrue(preview.none { it is ToHitFlatAdjustmentPreviewComponent && it.description == "Half Cover" })
        Assert.assertTrue(preview.none { it is ToCritFlatAdjustmentPreviewComponent && it.description == "Half Cover" })
    }
}
