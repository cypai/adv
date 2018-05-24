package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.*

class MeleeHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is ActionCommand
                && command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as ActionCommand
        return if (state.getNpcWeapon(cmd.unitId)!!.schema.range == WeaponRange.MELEE) {
            listOf(
                    ToHitFlatAdjustmentPreviewComponent(30, "Melee weapon"),
                    ToCritFlatAdjustmentPreviewComponent(25, "Melee weapon"))
        } else {
            listOf()
        }
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
