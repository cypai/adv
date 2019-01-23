package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DevHpChangeCommand
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.EndingType
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class BattleEndTest : GdxMockedTest() {

    @Test
    fun testTPK() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val playerId = npcList.add(player)
        val enemy = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val enemyId = npcList.add(enemy)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val events = backend.execute(DevHpChangeCommand(playerId, 0))
        Assert.assertTrue(events.any { it is BattleEndEvent && it.endingType == EndingType.GAME_OVER })
    }

    @Test
    fun testMapClear() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()
        val map = BattleMap.createBattleMap(4, 4)
        val player = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val playerId = npcList.add(player)
        val enemy = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3), null)
        val enemyId = npcList.add(enemy)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        val events = backend.execute(DevHpChangeCommand(enemyId, 0))
        Assert.assertTrue(events.any { it is BattleEndEvent && it.endingType == EndingType.MAP_CLEAR })
    }
}
