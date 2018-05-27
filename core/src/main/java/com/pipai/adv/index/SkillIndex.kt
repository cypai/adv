package com.pipai.adv.index

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.domain.SkillSchema
import com.pipai.adv.domain.SkillType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class SkillIndex(skillsFile: FileHandle) {

    val index: Map<String, SkillSchema>

    init {
        val rawData = skillsFile.readString("UTF-8")
        val parser = CSVParser.parse(rawData, CSVFormat.DEFAULT.withHeader())

        val mutIndex: MutableMap<String, SkillSchema> = mutableMapOf()
        for (record in parser.records) {
            val name = record.get("name")
            val weaponSchema = SkillSchema(
                    name,
                    SkillType.valueOf(record.get("type")),
                    record.get("maxLevel").toInt(),
                    record.get("description"))
            mutIndex[name] = weaponSchema
        }
        index = mutIndex.toMap()
    }

    fun getSkillSchema(skill: String): SkillSchema? = index[skill]

}
