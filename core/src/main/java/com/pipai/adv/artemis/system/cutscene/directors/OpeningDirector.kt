package com.pipai.adv.artemis.system.cutscene.directors

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule

class OpeningDirector : BattleCutsceneDirector {

    private var shown = false

    override fun check(backend: BattleBackend): Boolean {
        if (!shown) {
            when (backend.objective) {
                is MapClearEndingRule -> {
                    println("Objective: Clear the map")
                }
            }
            shown = true
        }
        return false
    }

}
