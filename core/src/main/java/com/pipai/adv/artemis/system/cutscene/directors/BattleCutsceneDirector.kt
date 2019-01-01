package com.pipai.adv.artemis.system.cutscene.directors

import com.pipai.adv.backend.battle.engine.BattleBackend

interface BattleCutsceneDirector {

    /**
     * Returns whether or not the cutscene system should pause
     */
    fun check(backend: BattleBackend): Boolean

}
