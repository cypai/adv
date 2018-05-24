package com.pipai.adv.classes.skills

abstract class UnitSkill(var level: Int) {

    abstract val maxLevel: Int

    abstract val name: String
    abstract val description: String

}
