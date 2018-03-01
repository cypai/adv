package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

interface CommandExecutionRule {
    fun matches(command: BattleCommand): Boolean
    fun preview(command: BattleCommand, state: BattleState, cache: BattleBackendCache): List<PreviewComponent>
    fun execute(command: BattleCommand, previews: List<PreviewComponent>, state: BattleState, cache: BattleBackendCache)
}
