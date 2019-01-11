package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.InteractCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.CellStateEvent
import com.pipai.adv.backend.battle.engine.log.TextEvent

class InteractExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is InteractCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as InteractCommand
        val apComponent = ApUsedPreviewComponent(cmd.unitId, 1)

        return listOf(apComponent)
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as InteractCommand
        val npc = state.getNpc(cmd.unitId)!!
        val cell = state.battleMap.getCell(cmd.position)
        if (cell.fullEnvObject is FullEnvObject.ChestEnvObject) {
            val chest = cell.fullEnvObject as FullEnvObject.ChestEnvObject
            npc.unitInstance.inventory.first { it.item == null }.item = chest.item
            cell.fullEnvObject = null
            state.battleLog.addEvent(CellStateEvent(command.position, cell.deepCopy(), "Opens this chest"))
            state.battleLog.addEvent(TextEvent("${state.getNpc(cmd.unitId)!!.unitInstance.nickname} obtained a ${chest.item.name}!"))
        }
    }
}
