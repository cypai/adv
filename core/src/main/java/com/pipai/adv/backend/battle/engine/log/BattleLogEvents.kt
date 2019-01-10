package com.pipai.adv.backend.battle.engine.log

import com.pipai.adv.backend.battle.domain.BattleMapCell
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.engine.BattleStats
import com.pipai.adv.backend.battle.engine.domain.BodyPart
import com.pipai.adv.domain.Npc
import com.pipai.adv.domain.UnitSkill

interface BattleLogEvent {
    fun description(): String
    fun userFriendlyDescription(): String = ""
}

data class BattleEndEvent(val endingType: EndingType, val battleStats: BattleStats) : BattleLogEvent {
    override fun description(): String = endingType.description
    override fun userFriendlyDescription(): String = endingType.description
}

enum class EndingType(val description: String) {
    MAP_CLEAR("The map is clear of enemies!"),
    RAN_AWAY("The party ran away..."),
    GAME_OVER("The party was defeated...")
}

data class ApChangeEvent(val npcId: Int, val newApAmount: Int) : BattleLogEvent {
    override fun description() = "NPC id $npcId AP set to $newApAmount"
}

data class TpChangeEvent(val npcId: Int, val newTpAmount: Int) : BattleLogEvent {
    override fun description() = "NPC id $npcId TP set to $newTpAmount"
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

data class BindEvent(val npcId: Int,
                     val npc: Npc,
                     val bodyPart: BodyPart,
                     val turns: Int) : BattleLogEvent {

    override fun description() = "$npc's (id $npcId) $bodyPart was bound for $turns turns"
    override fun userFriendlyDescription(): String {
        return when (bodyPart) {
            BodyPart.HEAD -> "${npc.unitInstance.nickname}'s head was bound!"
            BodyPart.ARMS -> "${npc.unitInstance.nickname}'s arms were bound!"
            BodyPart.LEGS -> "${npc.unitInstance.nickname}'s legs were bound!"
        }
    }
}

data class UnbindEvent(val npcId: Int,
                       val npc: Npc,
                       val bodyPart: BodyPart) : BattleLogEvent {

    override fun description() = "$npc's (id $npcId) $bodyPart is now free"
    override fun userFriendlyDescription(): String {
        return when (bodyPart) {
            BodyPart.HEAD -> "${npc.unitInstance.nickname}'s head is now free!"
            BodyPart.ARMS -> "${npc.unitInstance.nickname}'s arms are now free!"
            BodyPart.LEGS -> "${npc.unitInstance.nickname}'s legs are now free!"
        }
    }
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

data class TargetSkillEvent(val npcId: Int,
                            val npc: Npc,
                            val targetId: Int,
                            val target: Npc,
                            val skill: UnitSkill) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) used ${skill.name} on $target (id $targetId)"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} used ${skill.name}!"
}

data class TextEvent(val text: String) : BattleLogEvent {
    override fun description(): String = text
    override fun userFriendlyDescription(): String = text
}

data class CellStateEvent(val position: GridPosition,
                          val cell: BattleMapCell) : BattleLogEvent {

    override fun description(): String = "Cell at $position changed state to $cell"
}
