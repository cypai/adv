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

data class HealEvent(val npcId: Int,
                     val npc: Npc,
                     val healAmount: Int) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) was healed for $healAmount"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was healed!"
}

enum class DamageOutcome {
    HIT, CRIT, MISS
}

data class AmmoChangeEvent(val npcId: Int, val newAmount: Int) : BattleLogEvent {
    override fun description() = "$npcId's weapon ammo count was set to $newAmount"
    override fun userFriendlyDescription() = ""
}

data class PlayerKoEvent(val npcId: Int, val npc: Npc) : BattleLogEvent {
    override fun description() = "$npcId was KOed"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was KOed!"
}

data class NpcKoEvent(val npcId: Int, val npc: Npc) : BattleLogEvent {
    override fun description() = "$npcId was defeated"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was KOed!"
}
