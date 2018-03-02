package com.pipai.adv.backend.battle.engine.rules.verification

import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class ApRequirementRule : VerificationRule {
    override fun verify(preview: List<PreviewComponent>, state: BattleState): ExecutableStatus {
        val badApPreview = preview.filter { it is ApUsedPreviewComponent }
                .map { it as ApUsedPreviewComponent }
                .find { state.apState.getNpcAp(it.npcId) < it.apUsed }
        return if (badApPreview == null) {
            ExecutableStatus.COMMAND_OK
        } else {
            ExecutableStatus(false, "Not enough AP")
        }
    }
}
