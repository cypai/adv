package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.utils.GridUtils

class MoveExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is MoveCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as MoveCommand
        val npc = state.npcList.getNpc(cmd.unitId)!!
        val apMax = ActionPointState.startingNumAPs
        val mobility = npc.unitInstance.schema.baseStats.mobility
        val distancePerAp = mobility / apMax

        val distance = cmd.path.zipWithNext()
                .map { GridUtils.gridDistance(it.first, it.second) }
                .sum()
        val requiredAp = Math.ceil((distance / distancePerAp).toDouble()).toInt()

        val apComponent = ApUsedPreviewComponent(cmd.unitId, requiredAp)

        return listOf(apComponent)
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as MoveCommand

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()

        val startingCell = state.battleMap.getCell(startPosition)
        val endingCell = state.battleMap.getCell(endPosition)

        val npc = startingCell.fullEnvObject as FullEnvObject.NpcEnvObject

        startingCell.fullEnvObject = null
        endingCell.fullEnvObject = npc

        state.battleLog.addEvent(MoveEvent(npc.npcId, state.npcList.getNpc(npc.npcId)!!, startPosition, endPosition))
    }
}
