package com.pipai.adv.backend.battle.engine

import com.pipai.adv.npc.Npc

data class DamageEvent(val npcId: Int,
                       val npc: Npc,
                       val damage: Int,
                       val outcome: DamageOutcome) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) took $damage $outcome damage"
    override fun userFriendlyDescription(): String {
        return when (outcome) {
            DamageOutcome.HIT -> "${npc.unitInstance.nickname} took $damage damage!"
            DamageOutcome.CRIT -> "Critical hit! ${npc.unitInstance.nickname} took $damage damage!"
            DamageOutcome.MISS -> "But it missed!"
        }
    }
}

enum class DamageOutcome {
    HIT, CRIT, MISS
}

data class AmmoChangeEvent(val npcId: Int, val newAmount: Int) : BattleLogEvent {
    override fun description() = "$npcId's weapon ammo count was set to $newAmount"
    override fun userFriendlyDescription() = ""
}
