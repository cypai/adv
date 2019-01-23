package com.pipai.adv.backend.battle.engine

import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap

class BattleStats(private val npcList: AutoIncrementIdMap<Npc>) {

    private val creditedKills: MutableMap<Int, MutableList<Int>> = mutableMapOf()
    private val allKills: MutableList<Int> = mutableListOf()

    private var totalExp: Int = 0

    fun recordTargetedKill(attackerId: Int, koId: Int) {
        val kill = npcList.get(koId)!!
        totalExp += kill.unitInstance.expGiven

        if (creditedKills.containsKey(attackerId)) {
            creditedKills[attackerId]!!.add(koId)
        } else {
            creditedKills[attackerId] = mutableListOf(koId)
        }

        allKills.add(koId)
    }

    fun recordUntargetedKill(koId: Int) {
        val kill = npcList.get(koId)!!
        totalExp += kill.unitInstance.expGiven

        allKills.add(koId)
    }

    fun getCreditedKills(): Map<Int, List<Int>> {
        return creditedKills.toMap()
    }

    fun getAllKills(): List<Int> = allKills

    fun getExpGained() = totalExp

}
