package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.BodyPart
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class BodyPartUseRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            val binds = state.npcStatusState.getNpcBind(command.unitId)

            when (command) {
                is NormalAttackCommand -> return ExecutableStatus(binds.arms == 0, "Arm Binds")
                is TargetSkillCommand -> {
                    val skill = command.skill
                    val bodyPartRequired = when (skill.name) {
                        "Double Slash" -> BodyPart.ARMS
                        "Hamstring" -> BodyPart.ARMS
                        "Fireball" -> BodyPart.HEAD
                        "Thunder" -> BodyPart.HEAD
                        "Icicle" -> BodyPart.HEAD
                        "Heal" -> BodyPart.HEAD
                        "Head Strike" -> BodyPart.ARMS
                        "Arm Strike" -> BodyPart.ARMS
                        "Leg Strike" -> BodyPart.ARMS
                        else -> null
                    }
                    when (bodyPartRequired) {
                        BodyPart.HEAD -> return ExecutableStatus(binds.head == 0, "Head Binds")
                        BodyPart.ARMS -> return ExecutableStatus(binds.arms == 0, "Arm Binds")
                        BodyPart.LEGS -> return ExecutableStatus(binds.legs == 0, "Leg Binds")
                    }
                }
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

}
