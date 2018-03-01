package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

interface CommandRule {
    fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus
}
