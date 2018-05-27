package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DevHpChangeCommand
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.EndingType
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.npcFromStats
import org.junit.Assert
import org.junit.Test

class BattleEndTest {

    @Test
    fun testTPK() {
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

        val events = backend.execute(DevHpChangeCommand(playerId, 0))
        Assert.assertTrue(events.any { it is BattleEndEvent && it.endingType == EndingType.GAME_OVER })
    }

    @Test
    fun testMapClear() {
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

        val events = backend.execute(DevHpChangeCommand(enemyId, 0))
        Assert.assertTrue(events.any { it is BattleEndEvent && it.endingType == EndingType.MAP_CLEAR })
    }
}
