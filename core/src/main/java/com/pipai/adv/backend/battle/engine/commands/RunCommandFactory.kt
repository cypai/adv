package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.utils.BattleUtils

class RunCommandFactory(backend: BattleBackend) : ActionCommandFactory<RunCommand>(backend) {
    override fun generate(npcId: Int): List<RunCommand> {
        val commands: MutableList<RunCommand> = mutableListOf()
        if (BattleUtils.canTakeAction(npcId, 1, backend)) {
            commands.add(RunCommand(npcId))
        }
        return commands
    }
}
