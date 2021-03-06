package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.ChestEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.utils.GridUtils

class InteractCommandFactory(backend: BattleBackend) : ActionCommandFactory<InteractCommand>(backend) {
    override fun generate(npcId: Int): List<InteractCommand> {
        val commands: MutableList<InteractCommand> = mutableListOf()
        if (BattleUtils.canTakeAction(npcId, 1, backend)) {
            val position = backend.getNpcPosition(npcId)!!
            val neighbors = GridUtils.neighbors(position)

            val map = backend.getBattleMapUnsafe()
            neighbors.filter { it.x >= 0 && it.y >= 0 && it.x < map.width && it.y < map.height }
                    .filter { backend.getFullEnvObj(it) is ChestEnvObject }
                    .map { InteractCommand(npcId, it) }
                    .forEach { commands.add(it) }
        }
        return commands
    }
}
