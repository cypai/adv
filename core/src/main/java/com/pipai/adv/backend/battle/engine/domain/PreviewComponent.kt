package com.pipai.adv.backend.battle.engine.domain

import com.pipai.adv.backend.battle.domain.AttackElement

interface PreviewComponent {
    val description: String
    fun rightText(): String
}

data class ApUsedPreviewComponent(val npcId: Int, val apUsed: Int) : PreviewComponent {
    override val description: String = "AP used"
    override fun rightText(): String = apUsed.toString()
}

data class TpUsedPreviewComponent(val npcId: Int, val tpUsed: Int) : PreviewComponent {
    override val description: String = "TP used"
    override fun rightText(): String = tpUsed.toString()
}

data class AmmoChangePreviewComponent(val npcId: Int, val newAmount: Int) : PreviewComponent {
    override val description: String = "Ammo change"
    override fun rightText(): String = newAmount.toString()
}

data class HealPreviewComponent(val minHeal: Int, val maxHeal: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$minHeal - $maxHeal"
}

data class HealScaleAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment >= 0) {
            "+ $adjustment %"
        } else {
            "- ${-adjustment} %"
        }
    }
}

data class DamagePreviewComponent(val minDamage: Int, val maxDamage: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$minDamage - $maxDamage"
}

data class DamageFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment >= 0) {
            "+ $adjustment"
        } else {
            "- ${-adjustment}"
        }
    }
}

data class DamageScaleAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment >= 0) {
            "+ $adjustment %"
        } else {
            "- ${-adjustment} %"
        }
    }
}

data class ToHitPreviewComponent(val toHit: Int) : PreviewComponent {
    override val description: String = "Base"
    override fun rightText(): String = "$toHit %"
}

data class ToHitFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent {
    override fun rightText(): String {
        return if (adjustment >= 0) {
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
        return if (adjustment >= 0) {
            "+ $adjustment %"
        } else {
            "- ${-adjustment} %"
        }
    }
}

data class AttackElementPreviewComponent(val element: AttackElement) : PreviewComponent {
    override val description: String = "Element"
    override fun rightText(): String = element.toString()
}

data class BindPreviewComponent(val bodyPart: BodyPart, val minTurns: Int, val maxTurns: Int) : PreviewComponent {
    override val description: String = when (bodyPart) {
        BodyPart.HEAD -> "Head Bind"
        BodyPart.ARMS -> "Arm Bind"
        BodyPart.LEGS -> "Leg Bind"
    }

    override fun rightText(): String = "$minTurns - $maxTurns"
}

data class TargetStagePreviewComponent(val unitId: Int,
                                       val targetId: Int,
                                       val previews: MutableList<PreviewComponent>,
                                       val stageTypeDescription: StageTypeDescription) : PreviewComponent {
    override val description: String = stageTypeDescription.description
    override fun rightText(): String = stageTypeDescription.rightText
}

enum class StageType {
    PRIMARY, EFFECT
}

data class StageTypeDescription(val stageType: StageType, val description: String, val rightText: String)
