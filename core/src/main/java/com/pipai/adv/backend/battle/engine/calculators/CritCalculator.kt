package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritPreviewComponent

class CritCalculator {

    fun calculateToCrit(previews: List<PreviewComponent>): Int? {
        val toCrit = toCritComponents(previews)
        return if (toCrit == null) {
            null
        } else {
            val baseToCrit = toCrit.first.toCrit
            val flatAdjustment = toCrit.second.sumBy { (it as ToCritFlatAdjustmentPreviewComponent).adjustment }
            baseToCrit + flatAdjustment
        }
    }

    fun toCritComponents(previews: List<PreviewComponent>): Pair<ToCritPreviewComponent, List<PreviewComponent>>? {
        val toCrit = previews
                .find { it is ToCritPreviewComponent }
                ?.let { (it as ToCritPreviewComponent) }

        return if (toCrit == null) {
            null
        } else {
            val flatAdjustment = previews.filter { it is ToCritFlatAdjustmentPreviewComponent }
            Pair(toCrit, flatAdjustment)
        }
    }
}
