package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend

class RunCommandFactory(backend: BattleBackend) : ActionCommandFactory<RunCommand>(backend) {
    override fun generate(npcId: Int): List<RunCommand> {
        val commands: MutableList<RunCommand> = mutableListOf()
        val cmd = RunCommand(npcId)
        if (backend.canBeExecuted(cmd).executable) {
            commands.add(cmd)
        }
        return commands
    }
}
