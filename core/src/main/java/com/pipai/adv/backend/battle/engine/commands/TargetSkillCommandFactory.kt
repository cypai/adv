package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.domain.SkillRangeType
import com.pipai.adv.domain.SkillType
import com.pipai.adv.domain.UnitSkill

class TargetSkillCommandFactory(backend: BattleBackend, private val skill: UnitSkill) : ActionCommandFactory<TargetSkillCommand>(backend) {
    override fun generate(npcId: Int): List<TargetSkillCommand> {
        val npc = backend.getNpc(npcId)!!
        val commands: MutableList<TargetSkillCommand> = mutableListOf()

        when (skill.schema.rangeType) {
            SkillRangeType.WEAPON -> {
                val weapon = npc.unitInstance.weapon
                if (BattleUtils.canTakeAction(npcId, 1, backend)
                        && weapon != null
                        && BattleUtils.weaponCanAttack(weapon, 1)) {

                    val targets = BattleUtils.enemiesInWeaponRange(npcId, backend)
                    commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
                }
            }
            SkillRangeType.MELEE -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.RANGED -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.NONE -> {
            }
        }
        return commands
    }

    override fun generateInvalid(npcId: Int): List<TargetSkillCommand> {
        val npc = backend.getNpc(npcId)!!
        val commands: MutableList<TargetSkillCommand> = mutableListOf()

        when (skill.schema.rangeType) {
            SkillRangeType.WEAPON -> {
                val weapon = npc.unitInstance.weapon
                if (BattleUtils.canTakeAction(npcId, 1, backend)
                        && weapon != null) {

                    val targets = BattleUtils.enemiesInWeaponRange(npcId, backend)
                    commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
                }
            }
            SkillRangeType.MELEE -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.RANGED -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.NONE -> {
            }
        }
        return commands
    }

    fun generateForPosition(npcId: Int, position: GridPosition): List<TargetSkillCommand> {
        val npc = backend.getNpc(npcId)!!
        val commands: MutableList<TargetSkillCommand> = mutableListOf()

        when (skill.schema.rangeType) {
            SkillRangeType.WEAPON -> {
                val weapon = npc.unitInstance.weapon
                if (BattleUtils.canTakeAction(npcId, 1, backend)
                        && weapon != null
                        && BattleUtils.weaponCanAttack(weapon, 1)) {

                    val targets = BattleUtils.enemiesInWeaponRange(npcId, position, backend)
                    commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
                }
            }
            SkillRangeType.MELEE -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, position, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, position, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.RANGED -> {
                val targets = when (skill.schema.type) {
                    SkillType.ACTIVE_TARGET -> {
                        BattleUtils.enemiesInRange(npcId, position, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    SkillType.ACTIVE_TARGET_PARTY -> {
                        BattleUtils.teammatesInRange(npcId, position, backend, BattleBackend.RANGED_WEAPON_DISTANCE2)
                    }
                    else -> listOf()
                }
                commands.addAll(targets.map { TargetSkillCommand(skill, npcId, it) })
            }
            SkillRangeType.NONE -> {
            }
        }
        return commands
    }
}
