package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.utils.BattleUtils

class NormalAttackCommandFactory(backend: BattleBackend) : ActionCommandFactory(backend) {
    override fun generate(npcId: Int): List<ActionCommand> {
        val npc = backend.getNpc(npcId)!!
        val commands: MutableList<NormalAttackCommand> = mutableListOf()
        val weapon = npc.unitInstance.weapon
        if (BattleUtils.canTakeAction(npcId, 1, backend)
                && weapon != null
                && BattleUtils.weaponCanAttack(weapon, 1)) {

            val targets = BattleUtils.enemiesInRange(npcId, backend)
            commands.addAll(targets.map { NormalAttackCommand(npcId, it, weapon) })
        }
        return commands
    }
}
