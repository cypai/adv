package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.NpcEnvObject
import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.utils.GridUtils
import com.pipai.adv.utils.fetch

class MoveExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is MoveCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as MoveCommand
        val npc = state.npcList.get(cmd.unitId)!!
        val apMax = ActionPointState.startingNumAPs
        val mobility = npc.unitInstance.stats.mobility
        val distancePerAp = mobility.toFloat() / apMax.toFloat()

        val distance = cmd.path.zipWithNext()
                .map { GridUtils.gridDistance(it.first, it.second) }
                .sum()
        val requiredAp = Math.ceil((distance / distancePerAp).toDouble()).toInt()

        val apComponent = ApUsedPreviewComponent(cmd.unitId, requiredAp)

        return listOf(apComponent)
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as MoveCommand

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()

        val startingCell = state.battleMap.getCell(startPosition)
        val endingCell = state.battleMap.getCell(endPosition)

        val envObjId = startingCell.fullEnvObjId!!

        val npc = startingCell.fullEnvObjId.fetch(state.envObjList) as NpcEnvObject

        startingCell.fullEnvObjId = null
        endingCell.fullEnvObjId = envObjId

        state.battleLog.addEvent(MoveEvent(npc.npcId, state.npcList.get(npc.npcId)!!, cmd.path))
    }
}
