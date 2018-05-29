package com.pipai.adv.classes

import com.pipai.adv.domain.SkillSchema
import com.pipai.adv.domain.UnitSkill

data class ClassTree(val name: String, val description: String, private val skillMap: MutableMap<String, SkillNode>) {

    constructor(name: String, description: String) : this(name, description, mutableMapOf())

    fun getSkillMap() = skillMap.toMap()

    fun addSkill(skillSchema: SkillSchema) {
        skillMap[skillSchema.name] = SkillNode(skillSchema.new(0), mutableListOf())
    }

    fun addSkillRequirement(skillName: String, requirement: SkillRequirement) {
        if (!skillMap.containsKey(skillName)) {
            throw IllegalArgumentException("$skillName is not part of this class tree")
        }
        skillMap[skillName]!!.skillRequirements.add(requirement)
    }

}

data class SkillNode(val skill: UnitSkill, val skillRequirements: MutableList<SkillRequirement>)

data class SkillRequirement(val skill: SkillNode, val level: Int)
