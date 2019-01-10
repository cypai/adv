package com.pipai.adv.backend.battle.engine.rules.ending

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.log.EndingType

class RunAwayEndingRule : EndingRule {

    override val endingType: EndingType = EndingType.RAN_AWAY

    override fun evaluate(backend: BattleBackend, state: BattleState, cache: BattleBackendCache): Boolean {
        return state.variables.getOrDefault("runAway", "false") == "true"
    }
}
