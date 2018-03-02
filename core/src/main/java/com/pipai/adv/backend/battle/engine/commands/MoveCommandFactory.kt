package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.MapGraph

class MoveCommandFactory(backend: BattleBackend) : ActionCommandFactory<MoveCommand>(backend) {
    override fun generate(npcId: Int): List<MoveCommand> {
        val state = backend.getBattleState()
        val currentAp = state.apState.getNpcAp(npcId)
        val mapGraph = MapGraph(
                backend.getBattleMapState(),
                backend.getNpcPositions()[npcId]!!,
                state.npcList.getNpc(npcId)!!.unitInstance.schema.baseStats.mobility,
                currentAp,
                ActionPointState.startingNumAPs, debug = true)

        val availableCommands: MutableList<MoveCommand> = mutableListOf()
        for (ap in 1..currentAp) {
            availableCommands.addAll(mapGraph.getMovableCellPositions(ap)
                    .map { MoveCommand(npcId, mapGraph.getPath(it)) })
        }
        return availableCommands
    }
}
