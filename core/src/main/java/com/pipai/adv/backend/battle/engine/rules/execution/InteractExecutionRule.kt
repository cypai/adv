package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.ChestEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.InteractCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.EnvObjectDestroyEvent
import com.pipai.adv.backend.battle.engine.log.TextEvent
import com.pipai.adv.utils.fetch

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
        val fullEnvObj = cell.fullEnvObjId.fetch(state.envObjList)
        if (fullEnvObj is ChestEnvObject) {
            npc.unitInstance.inventory.first { it.item == null }.item = fullEnvObj.item
            state.battleLog.addEvent(EnvObjectDestroyEvent(cell.fullEnvObjId!!, fullEnvObj))
            state.battleLog.addEvent(TextEvent("${state.getNpc(cmd.unitId)!!.unitInstance.nickname} obtained a ${fullEnvObj.item.name}!"))
            cell.fullEnvObjId = null
        }
    }
}
