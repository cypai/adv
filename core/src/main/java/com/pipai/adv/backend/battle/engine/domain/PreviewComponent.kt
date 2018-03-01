package com.pipai.adv.backend.battle.engine.domain

sealed class PreviewComponent {

    abstract val description: String

    data class ToHitPreviewComponent(val toHit: Int) : PreviewComponent() {
        override val description: String = "Base to hit"
    }

    data class ToCritPreviewComponent(val toCrit: Int) : PreviewComponent() {
        override val description: String = "Base to crit"
    }

    data class ToHitFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class ToCritFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class DamagePreviewComponent(val minDamage: Int, val maxDamage: Int) : PreviewComponent() {
        override val description: String = "Base damage range"
    }

    data class DamageFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class AmmoChangePreviewComponent(val npcId: Int, val newAmount: Int) : PreviewComponent() {
        override val description: String = "Ammo change"
    }

    data class SecondaryEffectPreviewComponent(val chance: Int, override val description: String) : PreviewComponent()
}
