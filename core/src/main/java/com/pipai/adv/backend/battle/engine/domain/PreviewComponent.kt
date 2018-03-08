package com.pipai.adv.backend.battle.engine.domain

interface PreviewComponent {
    val description: String
    fun rightText(): String
}

data class ApUsedPreviewComponent(val npcId: Int, val apUsed: Int) : PreviewComponent {
    override val description: String = "AP used"
    override fun rightText(): String = apUsed.toString()
}

data class AmmoChangePreviewComponent(val npcId: Int, val newAmount: Int) : PreviewComponent {
    override val description: String = "Ammo change"
    override fun rightText(): String = newAmount.toString()
}

data class DamagePreviewComponent(val minDamage: Int, val maxDamage: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$minDamage - $maxDamage"
}

data class DamageFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment > 0) {
            "+ $adjustment"
        } else {
            "- ${-adjustment}"
        }
    }
}

data class ToHitPreviewComponent(val toHit: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$toHit %"
}

data class ToHitFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment > 0) {
            "+ $adjustment %"
        } else {
            "- ${-adjustment} %"
        }
    }
}

data class ToCritPreviewComponent(val toCrit: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$toCrit %"
}

data class ToCritFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment > 0) {
            "+ $adjustment %"
        } else {
            "- ${-adjustment} %"
        }
    }
}

data class SecondaryEffectPreviewComponent(val chance: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String = "$chance %"
}
