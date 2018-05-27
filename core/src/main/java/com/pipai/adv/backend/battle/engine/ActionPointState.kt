package com.pipai.adv.backend.battle.engine

import com.pipai.adv.domain.NpcList

class ActionPointState(npcList: NpcList) {

    companion object {
        val startingNumAPs = 2
    }

    private var apMap: MutableMap<Int, Int> = mutableMapOf()

    init {
        npcList.forEach { apMap.put(it.key, startingNumAPs) }
    }

    fun npcIdExists(npcId: Int): Boolean {
        return apMap.containsKey(npcId)
    }

    fun getNpcAp(npcId: Int): Int {
        val npcAp = apMap.get(npcId)
        if (npcAp != null) {
            return npcAp
        }
        throw IllegalArgumentException("Cannot get AP of null NPC")
    }

    fun setNpcAp(npcId: Int, points: Int) {
        if (points < 0) {
            throw IllegalArgumentException("Cannot set AP to be negative")
        }
        apMap.put(npcId, points)
    }
}