package com.pipai.adv.backend.battle.engine.command

import com.pipai.adv.backend.battle.engine.ActionCommand
import com.pipai.adv.backend.battle.engine.BattleBackend

abstract class ActionCommandFactory(protected val backend: BattleBackend) {
    abstract fun generate(npcId: Int): List<ActionCommand>
}
