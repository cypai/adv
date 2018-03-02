package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.HitCritCommand
import com.pipai.adv.backend.battle.engine.commands.WeaponCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitFlatAdjustmentPreviewComponent

class MeleeHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is HitCritCommand && command is WeaponCommand && command.weapon.schema.range == WeaponRange.MELEE
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf(
                ToHitFlatAdjustmentPreviewComponent(30, "Melee weapon to hit bonus"),
                ToCritFlatAdjustmentPreviewComponent(25, "Melee weapon to crit bonus"))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
