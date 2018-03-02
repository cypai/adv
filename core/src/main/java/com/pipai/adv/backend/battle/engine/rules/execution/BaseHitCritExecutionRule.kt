package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.HitCritCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitPreviewComponent

class BaseHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is HitCritCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as HitCritCommand
        return listOf(
                ToHitPreviewComponent(cmd.baseHit),
                ToCritPreviewComponent(cmd.baseCrit))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
