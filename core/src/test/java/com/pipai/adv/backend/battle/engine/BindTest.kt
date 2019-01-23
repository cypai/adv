package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DevBindChangeCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.BodyPart
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.generateBackend
import org.junit.Assert
import org.junit.Test

class BindTest : GdxMockedTest() {

    @Test
    fun testBoundNormalAttack() {
        val npcList = AutoIncrementIdMap<Npc>()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.add(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val targetId = npcList.add(target)
        map.getCell(0, 0).fullEnvObjId = envObjList.add(NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE))
        map.getCell(1, 1).fullEnvObjId = envObjList.add(NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE))

        val backend = generateBackend(npcList, envObjList, map)

        backend.execute(DevBindChangeCommand(attackerId, BodyPart.ARMS, 1))

        val cmd = NormalAttackCommand(attackerId, targetId)

        val executableStatus = backend.canBeExecuted(cmd)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Arm Binds", executableStatus.reason)
    }

}
