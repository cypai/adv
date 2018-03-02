package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend

abstract class ActionCommandFactory<out T : ActionCommand>(protected val backend: BattleBackend) {
    abstract fun generate(npcId: Int): List<T>
}
