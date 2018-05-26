package com.pipai.adv.backend.battle.engine.rules.ending

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.log.EndingType

interface EndingRule {
    val endingType: EndingType
    fun evaluate(backend: BattleBackend, state: BattleState, cache: BattleBackendCache): Boolean
}
