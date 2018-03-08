package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.utils.BattleUtils

class DefendCommandFactory(backend: BattleBackend) : ActionCommandFactory<DefendCommand>(backend) {
    override fun generate(npcId: Int): List<DefendCommand> {
        val commands: MutableList<DefendCommand> = mutableListOf()
        if (BattleUtils.canTakeAction(npcId, 1, backend)) {
            commands.add(DefendCommand(npcId))
        }
        return commands
    }
}
