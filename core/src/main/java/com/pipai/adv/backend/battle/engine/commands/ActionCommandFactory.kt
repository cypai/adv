package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.BattleBackend

abstract class ActionCommandFactory<out T : ActionCommand>(protected val backend: BattleBackend) {
    /**
     * Generate only commands that can be executed. Useful for AI
     */
    abstract fun generate(npcId: Int): List<T>

    /**
     * Generate commands that might not be able to be executed, but still should be displayed in UI.
     */
    open fun generateInvalid(npcId: Int): List<T> {
        return generate(npcId)
    }
}
