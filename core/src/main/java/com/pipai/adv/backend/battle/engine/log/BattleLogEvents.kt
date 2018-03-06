package com.pipai.adv.backend.battle.engine.log

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.npc.Npc

interface BattleLogEvent {
    fun description(): String
    fun userFriendlyDescription(): String = ""
}

data class ApChangeEvent(val npcId: Int, val newApAmount: Int) : BattleLogEvent {
    override fun description() = "$npcId AP set to $newApAmount"
}

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

data class HealEvent(val npcId: Int,
                     val npc: Npc,
                     val healAmount: Int) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) was healed for $healAmount"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was healed!"
}

data class AmmoChangeEvent(val npcId: Int, val newAmount: Int) : BattleLogEvent {
    override fun description() = "$npcId's weapon ammo count was set to $newAmount"
}

data class PlayerKoEvent(val npcId: Int, val npc: Npc) : BattleLogEvent {
    override fun description() = "$npcId was KOed"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was KOed!"
}

data class NpcKoEvent(val npcId: Int, val npc: Npc) : BattleLogEvent {
    override fun description() = "$npcId was defeated"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} was KOed!"
}

data class MoveEvent(val npcId: Int,
                     val npc: Npc,
                     val path: List<GridPosition>) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) moved using path $path"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} is moving..."
}

data class NormalAttackEvent(val attackerId: Int,
                             val attacker: Npc,
                             val targetId: Int,
                             val target: Npc,
                             val weapon: InventoryItem.WeaponInstance) : BattleLogEvent {

    override fun description() = "$attacker (id $attackerId) attacked $target (id $targetId) with $weapon"
    override fun userFriendlyDescription() = "${attacker.unitInstance.nickname} attacked ${target.unitInstance.nickname}!"
}
