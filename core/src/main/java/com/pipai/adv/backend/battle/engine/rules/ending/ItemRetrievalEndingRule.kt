package com.pipai.adv.backend.battle.engine.rules.ending

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.log.EndingType

class ItemRetrievalEndingRule(val item: String, val itemPosition: GridPosition) : EndingRule {

    override val endingType: EndingType = EndingType.MISSION_COMPLETE

    override fun evaluate(backend: BattleBackend, state: BattleState, cache: BattleBackendCache): Boolean {
        val ranAway = state.variables.getOrDefault("runAway", "false") == "true"
        val obtainedItem = backend.getTeam(Team.PLAYER)
                .map { backend.getNpc(it)!!.unitInstance.inventory }
                .flatten()
                .any { it.item?.name == item }
        val properlyRetrieved = backend.getBattleMapUnsafe().getCell(itemPosition).fullEnvObjId == null
        return ranAway && obtainedItem && properlyRetrieved
    }
}
