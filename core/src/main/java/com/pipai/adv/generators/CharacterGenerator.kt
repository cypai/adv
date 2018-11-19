package com.pipai.adv.generators

import com.badlogic.gdx.graphics.Color
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.MathUtils

class CharacterGenerator {

    private val hairColors: List<Pair<Color?, Color?>> = listOf(
            Pair(Color.YELLOW, Color.valueOf("692300F9")),  // Blonde
            Pair(Color.valueOf("BA6BFFFF"), Color.valueOf("2B1906E9")), // Brunette
            Pair(Color.valueOf("3D7CD4FF"), Color.valueOf("000D1FE5")), // Bluenette
            Pair(Color.valueOf("FFA8CCFF"), Color.valueOf("8F008FEB")), // Pinkette
            Pair(Color.valueOf("F58F22FF"), null)   // Purple

    )

    private val rainbowColors: List<Color?> = listOf(
            null,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.PURPLE,
            Color.PINK,
            Color.BROWN
    )

    fun generateSchoolBoy(): List<PccMetadata> {
        val pcc: MutableList<PccMetadata> = mutableListOf()
        pcc.add(PccMetadata("body", "body_1.png", null, null))
        pcc.add(PccMetadata("eye", "eye_0.png", null, null))
        val hairColor = MathUtils.randomSelect(hairColors)
        val hairStyle = MathUtils.randomSelect(listOf("hair_0.png", "hair_3.png", "hair_5.png"))
        pcc.add(PccMetadata("hair", hairStyle, hairColor.first, hairColor.second))
        pcc.add(PccMetadata("pants", "pants_13.png", null, null))
        pcc.add(PccMetadata("cloth", "cloth_63.png", null, null))
        pcc.add(PccMetadata("etc", "etc_205.png", null, MathUtils.randomSelect(rainbowColors)))
        val hat = MathUtils.randomSelect(listOf(null, "etc_24.png"))
        if (hat != null) {
            pcc.add(PccMetadata("etc", hat, null, MathUtils.randomSelect(rainbowColors)))
        }
        return pcc
    }

    fun generateSchoolGirl(): List<PccMetadata> {
        val pcc = MathUtils.randomSelect(listOf(generateSchoolGirl1(), generateSchoolGirl2()))
        addFemaleHairstyles(pcc)
        return pcc
    }

    private fun generateSchoolGirl1(): MutableList<PccMetadata> {
        val pcc: MutableList<PccMetadata> = mutableListOf()
        pcc.add(PccMetadata("body", "body_1.png", null, null))
        pcc.add(PccMetadata("eye", "eye_0.png", null, null))
        val clothList = listOf("cloth_59.png", "cloth_60.png", "cloth_61.png")
        pcc.add(PccMetadata("cloth", MathUtils.randomSelect(clothList), null, null))
        return pcc
    }

    private fun generateSchoolGirl2(): MutableList<PccMetadata> {
        val pcc: MutableList<PccMetadata> = mutableListOf()
        pcc.add(PccMetadata("body", "body_1.png", null, null))
        pcc.add(PccMetadata("eye", "eye_0.png", null, null))
        pcc.add(PccMetadata("cloth", "cloth_155.png", null, null))
        return pcc
    }

    private fun addFemaleHairstyles(pcc: MutableList<PccMetadata>) {
        val hairColor = MathUtils.randomSelect(hairColors)
        val hairStyle = MathUtils.randomSelect(listOf("hair_2.png", "hair_4.png"))
        pcc.add(PccMetadata("hair", hairStyle, hairColor.first, hairColor.second))
        val subHair = MathUtils.randomSelect(listOf(null, "subhair_1.png", "subhair_3.png", "subhair_9.png",
                "subhair_10.png", "subhair_11.png", "subhair_12.png", "subhair_58.png"))
        if (subHair != null) {
            pcc.add(PccMetadata("subhair", subHair, hairColor.first, hairColor.second))
        }
        val hairDecs = MathUtils.randomSelect(listOf(null, "etc_9.png", "etc_10.png", "etc_11.png", "etc_12.png", "etc_13.png"))
        if (hairDecs != null) {
            pcc.add(PccMetadata("etc", hairDecs, null, null))
        }
    }

}
