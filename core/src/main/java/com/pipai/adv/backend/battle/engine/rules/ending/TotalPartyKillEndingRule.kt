package com.pipai.adv.backend.battle.engine.rules.ending

import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.log.EndingType

class TotalPartyKillEndingRule : EndingRule {

    override val endingType: EndingType = EndingType.GAME_OVER

    override fun evaluate(backend: BattleBackend, state: BattleState, cache: BattleBackendCache): Boolean {
        return cache.getTeam(Team.PLAYER)
                .map { state.npcList.get(it)!! }
                .all { it.unitInstance.hp <= 0 }
    }
}
