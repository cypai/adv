package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.DefendCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.NpcStatus
import com.pipai.adv.backend.battle.engine.domain.NpcStatusInstance
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.TextEvent

class DefendExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is DefendCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as DefendCommand
        val ap = Math.max(1, state.getNpcAp(cmd.unitId))
        val apComponent = ApUsedPreviewComponent(cmd.unitId, ap)

        return listOf(apComponent)
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as DefendCommand
        state.npcStatusState.addNpcStatus(cmd.unitId, NpcStatusInstance(NpcStatus.DEFENDING, 2))
        state.battleLog.addEvent(TextEvent("${state.getNpc(cmd.unitId)!!.unitInstance.nickname} is defending!"))
    }
}
