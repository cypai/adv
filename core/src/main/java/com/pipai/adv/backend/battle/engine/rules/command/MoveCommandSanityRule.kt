package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class MoveCommandSanityRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is MoveCommand) {
            command.path.forEach {
                if (it.x < 0 || it.x >= state.battleMap.width || it.y < 0 || it.y >= state.battleMap.height) {
                    return ExecutableStatus(false, "Cannot move off the map")
                }
            }
            val origin = command.path.first()
            val originObject = state.battleMap.getCell(origin).fullEnvObject
            if (originObject == null || originObject !is FullEnvObject.NpcEnvObject) {
                return ExecutableStatus(false, "Origin has no movable NPC")
            } else if (originObject.npcId != command.unitId) {
                return ExecutableStatus(false, "NPC at origin is not the stated NPC")
            }
            val destination = command.path.last()
            if (state.battleMap.getCell(destination).fullEnvObject != null) {
                return ExecutableStatus(false, "Destination is not empty")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}