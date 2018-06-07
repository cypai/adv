package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.engine.domain.BindPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class BindCalculator {

    fun calculateBindTurnRange(previews: List<PreviewComponent>): Pair<Int, Int>? {
        val component = previews.find { it is BindPreviewComponent }
                ?.let { it as BindPreviewComponent }
                ?: return null
        return Pair(component.minTurns, component.maxTurns)
    }

    fun bindComponents(previews: List<PreviewComponent>): BindPreviewComponent? {
        return previews.find { it is BindPreviewComponent }
                ?.let { it as BindPreviewComponent }
    }

}
