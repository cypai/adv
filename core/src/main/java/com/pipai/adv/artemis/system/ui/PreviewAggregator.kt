package com.pipai.adv.artemis.system.ui

import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent
import com.pipai.adv.backend.battle.engine.rules.execution.AttackCalculationExecutionRule
import com.pipai.adv.backend.battle.engine.rules.execution.HealExecutionRule
import kotlin.math.roundToInt

class PreviewAggregator {

    private val attackCalculator = AttackCalculationExecutionRule()
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
            leftPreviewList.add(StringMenuItem("Effects", null, "").withData("disabled", true))
        }
        return leftPreviewList
    }

    private fun generateHitPreview(command: TargetCommand, preview: List<PreviewComponent>): StringMenuItem {

        val stages = preview
                .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                .map { it as TargetStagePreviewComponent }

        val primaryToHit = attackCalculator.calculateToHit(preview)

        if (primaryToHit == null) {
            val stageToHit = stages.map { attackCalculator.calculateToHit(it.previews) }
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

        val primaryToCrit = attackCalculator.calculateToCrit(preview)

        if (primaryToCrit == null) {
            val stageToCrit = stages.map { attackCalculator.calculateToCrit(it.previews) }
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

        val primaryDamageRange = attackCalculator.calculateDamageRange(preview)

        if (primaryDamageRange == null) {
            val stageDamage = stages.map { attackCalculator.calculateDamageRange(it.previews) }
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

    fun aggregateDetails(command: BattleCommand, preview: List<PreviewComponent>, previewType: String): List<StringMenuItem> {
        val rightPreviewList: MutableList<StringMenuItem> = mutableListOf()

        when {
            previewType.startsWith("Hit") -> {
                val details = attackCalculator.toHitComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
                if (command is TargetCommand) {
                    preview
                            .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                            .map { it as TargetStagePreviewComponent }
                            .forEach {
                                val stageDetails = attackCalculator.toHitComponents(it.previews)
                                if (stageDetails != null) {
                                    rightPreviewList.add(previewToStringMenuItem(stageDetails.first))
                                    rightPreviewList.addAll(stageDetails.second.map { previewToStringMenuItem(it) })
                                }
                            }
                }
            }
            previewType.startsWith("Crit") -> {
                val details = attackCalculator.toCritComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
                if (command is TargetCommand) {
                    preview
                            .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                            .map { it as TargetStagePreviewComponent }
                            .forEach {
                                val stageDetails = attackCalculator.toCritComponents(it.previews)
                                if (stageDetails != null) {
                                    rightPreviewList.add(previewToStringMenuItem(stageDetails.first))
                                    rightPreviewList.addAll(stageDetails.second.map { previewToStringMenuItem(it) })
                                }
                            }
                }
            }
            previewType.startsWith("Damage") -> {
                val details = attackCalculator.damageRangeComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
                if (command is TargetCommand) {
                    preview
                            .filter { it is TargetStagePreviewComponent && it.unitId == command.unitId && it.targetId == command.targetId }
                            .map { it as TargetStagePreviewComponent }
                            .forEach {
                                val stageDetails = attackCalculator.damageRangeComponents(it.previews)
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
        }
        return rightPreviewList
    }

    private fun previewToStringMenuItem(preview: PreviewComponent): StringMenuItem {
        return StringMenuItem(preview.description, null, preview.rightText())
    }
}
