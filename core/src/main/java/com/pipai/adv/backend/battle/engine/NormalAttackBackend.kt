package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.domain.WeaponAttribute
import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackend.Companion.MELEE_WEAPON_DISTANCE2
import com.pipai.adv.backend.battle.engine.BattleBackend.Companion.RANGED_WEAPON_DISTANCE2
import com.pipai.adv.npc.Npc
import com.pipai.adv.utils.MathUtils

data class NormalAttackCommand(override val unitId: Int,
                               override val targetId: Int,
                               override val weapon: InventoryItem.WeaponInstance) : ActionCommand, HitCritCommand, WeaponCommand {
    override val requiredAp: Int = 1
    override val baseHit = 65
    override val baseCrit = 25
}

class NormalAttackCommandSanityRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is NormalAttackCommand) {
            val attacker = state.npcList.getNpc(command.unitId)
                    ?: return ExecutableStatus(false, "Attacker ${command.unitId} does not exist")
            val target = state.npcList.getNpc(command.targetId)
                    ?: return ExecutableStatus(false, "Target ${command.unitId} does not exist")

            if (attacker.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "Attacker ${command.unitId} has < 0 HP")
            }
            if (target.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "Target ${command.unitId} has < 0 HP")
            }

            val range = command.weapon.schema.range
            val attackerPosition = cache.npcPositions[command.unitId]!!
            val targetPosition = cache.npcPositions[command.targetId]!!

            val distanceLimit = when (range) {
                WeaponRange.MELEE -> MELEE_WEAPON_DISTANCE2
                WeaponRange.RANGED -> RANGED_WEAPON_DISTANCE2
            }
            val distance2 = MathUtils.distance2(attackerPosition.x, attackerPosition.y, targetPosition.x, targetPosition.y)
            if (distance2 >= distanceLimit) {
                return ExecutableStatus(false, "Attacking distance is too great")
            }

            val weapon = command.weapon
            if (weapon.ammo <= 0
                    && (weapon.schema.attributes.contains(WeaponAttribute.CAN_RELOAD)
                            || weapon.schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD))) {

                return ExecutableStatus(false, "This weapon needs to be reloaded")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class NormalAttackExecutionRule : CommandExecutionRule {

    companion object {
        const val DAMAGE_RANGE = 2
    }

    override fun matches(command: BattleCommand): Boolean {
        return command is NormalAttackCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as NormalAttackCommand
        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.schema.baseStats.strength + cmd.weapon.schema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(PreviewComponent.DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))

        if (cmd.weapon.schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD)
                || cmd.weapon.schema.attributes.contains(WeaponAttribute.CAN_RELOAD)) {

            previewComponents.add(PreviewComponent.AmmoChangePreviewComponent(cmd.unitId, cmd.weapon.ammo - 1))
        }

        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as NormalAttackCommand
        state.battleLog.addEvent(NormalAttackEvent(
                cmd.unitId,
                state.npcList.getNpc(cmd.unitId)!!,
                cmd.targetId,
                state.npcList.getNpc(cmd.unitId)!!,
                cmd.weapon))
    }
}

data class NormalAttackEvent(val attackerId: Int,
                             val attacker: Npc,
                             val targetId: Int,
                             val target: Npc,
                             val weapon: InventoryItem.WeaponInstance) : BattleLogEvent {

    override fun description() = "$attacker (id $attackerId) attacked $target (id $targetId) with $weapon"
    override fun userFriendlyDescription() = "${attacker.unitInstance.nickname} attacked ${target.unitInstance.nickname}!"
}
