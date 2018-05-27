package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.MapGraph

class MoveCommandFactory(backend: BattleBackend) : ActionCommandFactory<MoveCommand>(backend) {
    override fun generate(npcId: Int): List<MoveCommand> {
        val currentAp = backend.getNpcAp(npcId)
        return generate(npcId, currentAp)
    }

    fun generate(npcId: Int, maxAp: Int): List<MoveCommand> {
        val mapGraph = getMapGraph(npcId)

        val availableCommands: MutableList<MoveCommand> = mutableListOf()
        for (ap in 1..maxAp) {
            availableCommands.addAll(mapGraph.getMovableCellPositions(ap)
                    .map { MoveCommand(npcId, mapGraph.getPath(it)) })
        }
        return availableCommands
    }

    fun getMapGraph(npcId: Int): MapGraph {
        val state = backend.getBattleState()
        val currentAp = backend.getNpcAp(npcId)
        return MapGraph(
                backend.getBattleMapState(),
                backend.getNpcPositions()[npcId]!!,
                state.npcList.getNpc(npcId)!!.unitInstance.stats.mobility,
                currentAp,
                ActionPointState.startingNumAPs)
    }
}
