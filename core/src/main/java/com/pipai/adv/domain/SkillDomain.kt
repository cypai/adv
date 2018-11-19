package com.pipai.adv.domain

import com.pipai.adv.backend.battle.engine.domain.BodyPart

enum class SkillType {
    PASSIVE, ACTIVE, ACTIVE_TARGET, ACTIVE_TARGET_PARTY, ACTIVE_AOE
}

enum class SkillRangeType {
    NONE, WEAPON, MELEE, RANGED
}

data class SkillSchema(val name: String, val type: SkillType, val rangeType: SkillRangeType, val maxLevel: Int,
                       val bodyPartUsed: BodyPart?, val description: String) {

    fun new(): UnitSkill {
        return new(1)
    }

    fun new(level: Int): UnitSkill {
        return UnitSkill(level, name)
    }
}

data class UnitSkill(var level: Int, val name: String)
