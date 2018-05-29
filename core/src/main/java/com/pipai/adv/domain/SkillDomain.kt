package com.pipai.adv.domain

enum class SkillType {
    PASSIVE, ACTIVE, ACTIVE_TARGET, ACTIVE_TARGET_PARTY, ACTIVE_AOE
}

data class SkillSchema(val name: String, val type: SkillType, val maxLevel: Int, val description: String) {
    fun new(): UnitSkill {
        return new(1)
    }

    fun new(level: Int): UnitSkill {
        return UnitSkill(level, this)
    }
}

data class UnitSkill(var level: Int, val schema: SkillSchema)
