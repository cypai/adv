package com.pipai.adv.artemis.system.ui

import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.backend.battle.engine.calculators.BindCalculator
import com.pipai.adv.backend.battle.engine.calculators.CritCalculator
import com.pipai.adv.backend.battle.engine.calculators.DamageCalculator
import com.pipai.adv.backend.battle.engine.calculators.HitCalculator
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.StageType
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent
import com.pipai.adv.backend.battle.engine.rules.execution.HealExecutionRule
import kotlin.math.roundToInt

class PreviewAggregator {

    private val hitCalculator = HitCalculator()
    private val critCalculator = CritCalculator()
    private val damageCalculator = DamageCalculator()
    private val bindCalculator = BindCalculator()
    private val healCalculator = HealExecutionRule()

    fun aggregate(command: BattleCommand, preview: List<PreviewComponent>): List<StringMenuItem> {
        val leftPreviewList: MutableList<StringMenuItem> = mutableListOf()
        if (command is TargetCommand) {
            leftPreviewList.add(generateHitPreview(command, preview))
            leftPreviewList.add(generateCritPreview(command, preview))
            val healPreview = generateHealPreview(preview)
            if (healPreview == null) {
                leftPreviewList.add(generateDamagePreview(command, preview))
            } else {
                leftPreviewList.add(healPreview)
            }
            leftPreviewList.add(generateEffectPreview(command, preview))
        }
        return leftPreviewList
    }

    private fun generateHitPreview(command: TargetCommand, preview: List<PreviewComponent>): StringMenuItem {

        val stages = preview
                .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                .map { it as TargetStagePreviewComponent }

        val primaryToHit = hitCalculator.calculateToHit(preview)

        if (primaryToHit == null) {
            val stageToHit = stages.map { hitCalculator.calculateToHit(it.previews) }
                    .filter { it != null }
                    .map { it!! }

            return when (stageToHit.size) {
                0 -> StringMenuItem("Hit", null, "").withData("disabled", true)
                1 -> StringMenuItem("Hit", null, "${stageToHit[0]} %").withData("disabled", false)
                else -> {
                    val avgToHit = stageToHit.average().roundToInt()
                    StringMenuItem("Hit (${stageToHit.size}x avg)", null, "$avgToHit %").withData("disabled", false)
                }
            }
        } else {
            return StringMenuItem("Hit", null, "$primaryToHit %").withData("disabled", false)
        }
    }

    private fun generateCritPreview(command: TargetCommand, preview: List<PreviewComponent>): StringMenuItem {

        val stages = preview
                .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                .map { it as TargetStagePreviewComponent }

        val primaryToCrit = critCalculator.calculateToCrit(preview)

        if (primaryToCrit == null) {
            val stageToCrit = stages.map { critCalculator.calculateToCrit(it.previews) }
                    .filter { it != null }
                    .map { it!! }

            return when (stageToCrit.size) {
                0 -> StringMenuItem("Crit", null, "").withData("disabled", true)
                1 -> StringMenuItem("Crit", null, "${stageToCrit[0]} %").withData("disabled", false)
                else -> {
                    val avgToCrit = stageToCrit.average().roundToInt()
                    StringMenuItem("Crit (${stageToCrit.size}x avg)", null, "$avgToCrit %").withData("disabled", false)
                }
            }
        } else {
            return StringMenuItem("Crit", null, "$primaryToCrit %").withData("disabled", false)
        }
    }

    private fun generateHealPreview(preview: List<PreviewComponent>): StringMenuItem? {
        val healRange = healCalculator.calculateHealRange(preview) ?: return null

        return StringMenuItem("Heal", null, "${healRange.first} - ${healRange.second}").withData("disabled", false)
    }

    private fun generateDamagePreview(command: TargetCommand, preview: List<PreviewComponent>): StringMenuItem {

        val stages = preview
                .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                .map { it as TargetStagePreviewComponent }

        val primaryDamageRange = damageCalculator.calculateDamageRange(preview)

        if (primaryDamageRange == null) {
            val stageDamage = stages.map { damageCalculator.calculateDamageRange(it.previews) }
                    .filter { it != null }
                    .map { it!! }

            if (stageDamage.isEmpty()) {
                return StringMenuItem("Damage", null, "").withData("disabled", true)
            } else {
                val minDamage = stageDamage.map { it.first }.min()!!
                val maxDamage = stageDamage.map { it.second }.sum()
                return StringMenuItem("Damage", null, "$minDamage - $maxDamage").withData("disabled", false)
            }
        } else {
            return StringMenuItem("Damage", null, "${primaryDamageRange.first} - ${primaryDamageRange.second}").withData("disabled", false)
        }
    }

    private fun generateEffectPreview(command: TargetCommand, preview: List<PreviewComponent>): StringMenuItem {
        return StringMenuItem("Effects", null, "")
                .withData("disabled", aggregateDetails(command, preview, "Effects").isEmpty())
    }

    fun aggregateDetails(command: BattleCommand, preview: List<PreviewComponent>, previewType: String): List<StringMenuItem> {
        val rightPreviewList: MutableList<StringMenuItem> = mutableListOf()

        when {
            previewType.startsWith("Hit") -> {
                if (command is TargetCommand) {
                    val details = hitCalculator.toHitComponents(preview)
                    if (details == null) {
                        preview
                                .filter {
                                    it is TargetStagePreviewComponent
                                            && it.unitId == command.unitId
                                            && it.targetId == command.targetId
                                            && it.stageTypeDescription.stageType == StageType.PRIMARY
                                }
                                .map { it as TargetStagePreviewComponent }
                                .forEach {
                                    val stageDetails = hitCalculator.toHitComponents(it.previews)
                                    if (stageDetails != null) {
                                        rightPreviewList.add(previewToStringMenuItem(stageDetails.first))
                                        rightPreviewList.addAll(stageDetails.second.map { previewToStringMenuItem(it) })
                                    }
                                }
                    } else {
                        rightPreviewList.add(previewToStringMenuItem(details.first))
                        rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                    }
                }
            }
            previewType.startsWith("Crit") -> {
                if (command is TargetCommand) {
                    val details = critCalculator.toCritComponents(preview)
                    if (details == null) {
                        preview
                                .filter {
                                    it is TargetStagePreviewComponent
                                            && it.unitId == command.unitId
                                            && it.targetId == command.targetId
                                            && it.stageTypeDescription.stageType == StageType.PRIMARY
                                }
                                .map { it as TargetStagePreviewComponent }
                                .forEach {
                                    val stageDetails = critCalculator.toCritComponents(it.previews)
                                    if (stageDetails != null) {
                                        rightPreviewList.add(previewToStringMenuItem(stageDetails.first))
                                        rightPreviewList.addAll(stageDetails.second.map { previewToStringMenuItem(it) })
                                    }
                                }
                    } else {
                        rightPreviewList.add(previewToStringMenuItem(details.first))
                        rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                    }
                }
            }
            previewType.startsWith("Damage") -> {
                val details = damageCalculator.damageRangeComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
                if (command is TargetCommand) {
                    preview
                            .filter {
                                it is TargetStagePreviewComponent
                                        && it.unitId == command.unitId
                                        && it.targetId == command.targetId
                                        && it.stageTypeDescription.stageType == StageType.PRIMARY
                            }
                            .map { it as TargetStagePreviewComponent }
                            .forEach {
                                val stageDetails = damageCalculator.damageRangeComponents(it.previews)
                                if (stageDetails != null) {
                                    rightPreviewList.add(previewToStringMenuItem(stageDetails.first))
                                    rightPreviewList.addAll(stageDetails.second.map { previewToStringMenuItem(it) })
                                }
                            }
                }
            }
            previewType.startsWith("Heal") -> {
                val details = healCalculator.calculateHealComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
            }
            previewType.startsWith("Effects") -> {
                preview
                        .filter {
                            it is TargetStagePreviewComponent
                                    && it.stageTypeDescription.stageType == StageType.EFFECT
                        }
                        .map { it as TargetStagePreviewComponent }
                        .forEach {
                            rightPreviewList.add(StringMenuItem(it.stageTypeDescription.description, null, it.stageTypeDescription.rightText))
                        }
            }
        }
        return rightPreviewList
    }

    private fun previewToStringMenuItem(preview: PreviewComponent): StringMenuItem {
        return StringMenuItem(preview.description, null, preview.rightText())
    }
}
