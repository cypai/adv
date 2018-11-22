package com.pipai.adv.artemis.system.cutscene.directors

import com.pipai.adv.backend.battle.engine.BattleBackend

interface BattleCutsceneDirector {

    fun check(backend: BattleBackend)

}
