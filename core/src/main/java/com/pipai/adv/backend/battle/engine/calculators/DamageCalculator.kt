package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.engine.domain.DamageFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.DamagePreviewComponent
import com.pipai.adv.backend.battle.engine.domain.DamageScaleAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class DamageCalculator {

    fun calculateDamageRange(previews: List<PreviewComponent>): Pair<Int, Int>? {
        val damageComponents = damageRangeComponents(previews)
        return if (damageComponents == null) {
            null
        } else {
            val baseDamage = damageComponents.first
            val flatAdjustment = damageComponents.second
                    .filter { it is DamageFlatAdjustmentPreviewComponent }
                    .sumBy { (it as DamageFlatAdjustmentPreviewComponent).adjustment }

            val baseRange = Pair(baseDamage.minDamage + flatAdjustment, baseDamage.maxDamage + flatAdjustment)

            val scaleAdjustment = damageComponents.second
                    .filter { it is DamageScaleAdjustmentPreviewComponent }
                    .sumBy { (it as DamageScaleAdjustmentPreviewComponent).adjustment } / 100f

            Pair(
                    baseRange.first + (baseRange.first * scaleAdjustment).toInt(),
                    baseRange.second + (baseRange.second * scaleAdjustment).toInt())
        }
    }

    fun damageRangeComponents(previews: List<PreviewComponent>): Pair<DamagePreviewComponent, List<PreviewComponent>>? {
        val baseDamage = previews
                .find { it is DamagePreviewComponent }
                ?.let { (it as DamagePreviewComponent) }

        return if (baseDamage == null) {
            null
        } else {
            val adjustments = previews.filter { it is DamageFlatAdjustmentPreviewComponent || it is DamageScaleAdjustmentPreviewComponent }
            Pair(baseDamage, adjustments)
        }
    }

}
