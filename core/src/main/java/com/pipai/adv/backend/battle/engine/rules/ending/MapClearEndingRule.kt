package com.pipai.adv.backend.battle.engine.rules.ending

import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.log.EndingType

class MapClearEndingRule : EndingRule {

    override val endingType: EndingType = EndingType.MAP_CLEAR

    override fun evaluate(backend: BattleBackend, state: BattleState, cache: BattleBackendCache): Boolean {
        return cache.getTeam(Team.AI)
                .map { state.npcList.get(it)!! }
                .all { it.unitInstance.hp <= 0 }
    }
}
