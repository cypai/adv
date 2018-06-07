package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitPreviewComponent

class HitCalculator {

    fun calculateToHit(previews: List<PreviewComponent>): Int? {
        val toHit = toHitComponents(previews)
        return if (toHit == null) {
            null
        } else {
            val baseToHit = toHit.first.toHit
            val flatAdjustment = toHit.second.sumBy { (it as ToHitFlatAdjustmentPreviewComponent).adjustment }
            baseToHit + flatAdjustment
        }
    }

    fun toHitComponents(previews: List<PreviewComponent>): Pair<ToHitPreviewComponent, List<PreviewComponent>>? {
        val toHit = previews
                .find { it is ToHitPreviewComponent }
                ?.let { (it as ToHitPreviewComponent) }

        return if (toHit == null) {
            null
        } else {
            val flatAdjustment = previews.filter { it is ToHitFlatAdjustmentPreviewComponent }
            Pair(toHit, flatAdjustment)
        }
    }

}
