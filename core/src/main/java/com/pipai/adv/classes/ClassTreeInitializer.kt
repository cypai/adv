package com.pipai.adv.classes

import com.pipai.adv.index.SkillIndex
import com.pipai.adv.save.AdvSave

class ClassTreeInitializer(private val skillIndex: SkillIndex) {

    fun availableClasses(): List<ClassTree> {
        return listOf(
                generateFighterTree(),
                generateArcherTree(),
                generateElementalistTree(),
                generateMedicTree(),
                generateMartialArtistTree()
        )
    }

    fun generateTree(npcId: Int, save: AdvSave): ClassTree {
        val name = save.classes[npcId]!!

        val tree = when (name) {
            "Fighter" -> generateFighterTree()
            "Archer" -> generateArcherTree()
            "Elementalist" -> generateElementalistTree()
            "Medic" -> generateMedicTree()
            "Martial Artist" -> generateMartialArtistTree()
            "Rookie" -> ClassTree("Rookie", "Doesn't have anything special.")
            else -> throw IllegalArgumentException("Class $name does not exist")
        }

        save.globalNpcList.get(npcId)!!.unitInstance.skills.forEach {
            tree.setSkillLevel(it.name, it.level)
        }

        return tree
    }

    fun generateFighterTree(): ClassTree {
        val tree = ClassTree("Fighter",
                "An attacker specializing in close range elemental and non-elemental attacks")
        tree.addSkill(skillIndex.getSkillSchema("Rush")!!)
        tree.addSkill(skillIndex.getSkillSchema("Double Slash")!!)
        tree.addSkill(skillIndex.getSkillSchema("Spin Attack")!!)
        tree.addSkill(skillIndex.getSkillSchema("Attack Doubler")!!)
        tree.addSkill(skillIndex.getSkillSchema("Flame Chase")!!)
        tree.addSkill(skillIndex.getSkillSchema("Lightning Chase")!!)
        tree.addSkill(skillIndex.getSkillSchema("Ice Chase")!!)
        tree.addSkill(skillIndex.getSkillSchema("Double Chase")!!)
        tree.addSkill(skillIndex.getSkillSchema("Wide Chase")!!)
        return tree
    }

    fun generateArcherTree(): ClassTree {
        val tree = ClassTree("Archer",
                "An attacker specializing in long ranged attacks")
        tree.addSkill(skillIndex.getSkillSchema("Rapid Fire")!!)
        tree.addSkill(skillIndex.getSkillSchema("Fire Arrow")!!)
        tree.addSkill(skillIndex.getSkillSchema("Hamstring")!!)
        return tree
    }

    fun generateElementalistTree(): ClassTree {
        val tree = ClassTree("Elementalist",
                "An attacker specializing in elemental magic")
        tree.addSkill(skillIndex.getSkillSchema("Fireball")!!)
        tree.addSkill(skillIndex.getSkillSchema("Thunder")!!)
        tree.addSkill(skillIndex.getSkillSchema("Icicle")!!)
        return tree
    }

    fun generateMedicTree(): ClassTree {
        val tree = ClassTree("Medic",
                "A specialist in healing and status support")
        tree.addSkill(skillIndex.getSkillSchema("Heal")!!)
        tree.addSkill(skillIndex.getSkillSchema("Healing Circle")!!)
        tree.addSkill(skillIndex.getSkillSchema("Smokeblight")!!)
        tree.addSkill(skillIndex.getSkillSchema("Poison")!!)
        tree.addSkill(skillIndex.getSkillSchema("Refresh")!!)
        tree.addSkill(skillIndex.getSkillSchema("Unbind")!!)
        return tree
    }

    fun generateMartialArtistTree(): ClassTree {
        val tree = ClassTree("Martial Artist",
                "A specialist in unarmed combat and binds")
        tree.addSkill(skillIndex.getSkillSchema("Ki")!!)
        tree.addSkill(skillIndex.getSkillSchema("Head Strike")!!)
        tree.addSkill(skillIndex.getSkillSchema("Arm Strike")!!)
        tree.addSkill(skillIndex.getSkillSchema("Leg Strike")!!)
        tree.addSkill(skillIndex.getSkillSchema("Leading Blow")!!)
        tree.addSkill(skillIndex.getSkillSchema("Cross Counter")!!)
        tree.addSkill(skillIndex.getSkillSchema("Stretch")!!)
        return tree
    }

}
