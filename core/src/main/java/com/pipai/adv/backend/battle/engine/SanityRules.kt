package com.pipai.adv.backend.battle.engine

class NpcMustExistRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            state.npcList.getNpc(command.unitId)
                    ?: return ExecutableStatus(false, "Npc ${command.unitId} does not exist")
        }
        if (command is HitCritCommand) {
            state.npcList.getNpc(command.targetId)
                    ?: return ExecutableStatus(false, "Npc ${command.targetId} does not exist")
        }
        return ExecutableStatus.COMMAND_OK
    }
}
