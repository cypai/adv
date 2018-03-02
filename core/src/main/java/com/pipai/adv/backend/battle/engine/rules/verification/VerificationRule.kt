package com.pipai.adv.backend.battle.engine.rules.verification

import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

interface VerificationRule {
    fun verify(preview: List<PreviewComponent>, state: BattleState): ExecutableStatus
}
