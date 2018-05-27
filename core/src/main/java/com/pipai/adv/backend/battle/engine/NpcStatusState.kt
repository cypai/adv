package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.engine.domain.AilmentInstance
import com.pipai.adv.backend.battle.engine.domain.NpcStatus
import com.pipai.adv.backend.battle.engine.domain.NpcStatusInstance
import com.pipai.adv.domain.NpcList

class NpcStatusState(npcList: NpcList) {

    private val npcAilments: MutableMap<Int, AilmentInstance?> = mutableMapOf()
    private val npcStatus: MutableMap<Int, MutableList<NpcStatusInstance>> = mutableMapOf()

    init {
        npcList.forEach {
            npcAilments[it.key] = null
            npcStatus[it.key] = mutableListOf()
        }
    }

    fun getNpcAilment(npcId: Int): AilmentInstance? {
        return npcAilments[npcId]
    }

    fun setNpcAilment(npcId: Int, ailment: AilmentInstance?) {
        npcAilments[npcId] = ailment
    }

    fun getNpcStatus(npcId: Int): List<NpcStatusInstance> {
        return npcStatus[npcId] ?: throw IllegalArgumentException("NPC $npcId does not exist")
    }

    fun addNpcStatus(npcId: Int, status: NpcStatusInstance) {
        npcStatus[npcId]?.add(status)
    }

    fun checkNpcStatus(npcId: Int, status: NpcStatus): Boolean {
        return npcStatus[npcId]?.filter { it.status == status }.orEmpty().isNotEmpty()
    }

    fun decreaseTurnCount() {
        npcAilments.forEach { it.value?.turns?.minus(1) }
        npcAilments.filterValues { it != null }.forEach { npcId, _ -> npcAilments[npcId] = null }
        npcStatus.forEach { it.value.forEach { it.turns -= 1 } }
        npcStatus.forEach { npcId, status -> npcStatus[npcId] = status.filter { it.turns > 0 }.toMutableList() }
    }
}
