package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.commands.DevBindChangeCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.BodyPart
import com.pipai.adv.domain.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.test.fixtures.npcFromStats
import com.pipai.test.fixtures.swordFixture
import org.junit.Assert
import org.junit.Test

class BindTest {

    @Test
    fun testBoundNormalAttack() {
        val save = AdvSave()
        val npcList = NpcList()
        val map = BattleMap.createBattleMap(4, 4)
        val sword = swordFixture()
        val attacker = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword)
        val attackerId = npcList.addNpc(attacker)
        val target = npcFromStats(UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null)
        val targetId = npcList.addNpc(target)
        map.getCell(0, 0).fullEnvObject = FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE)
        map.getCell(1, 1).fullEnvObject = FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val backend = BattleBackend(save, npcList, map)

        backend.execute(DevBindChangeCommand(attackerId, BodyPart.ARMS, 1))

        val cmd = NormalAttackCommand(attackerId, targetId)

        val executableStatus = backend.canBeExecuted(cmd)
        Assert.assertFalse(executableStatus.executable)
        Assert.assertEquals("Arm Binds", executableStatus.reason)
    }

}
