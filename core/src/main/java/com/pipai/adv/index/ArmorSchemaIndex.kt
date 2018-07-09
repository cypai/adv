package com.pipai.adv.index

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.backend.battle.domain.ArmorSchema
import com.pipai.adv.backend.battle.domain.ArmorType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class ArmorSchemaIndex(armorFile: FileHandle) {

    val index: Map<String, ArmorSchema>

    init {
        val rawData = armorFile.readString("UTF-8")
        val parser = CSVParser.parse(rawData, CSVFormat.DEFAULT.withHeader())

        val mutIndex: MutableMap<String, ArmorSchema> = mutableMapOf()
        for (record in parser.records) {
            val name = record.get("name")
            val weaponSchema = ArmorSchema(
                    name,
                    ArmorType.valueOf(record.get("type")),
                    record.get("pdef").toInt(),
                    record.get("mdef").toInt(),
                    record.get("value").toInt(),
                    record.get("description"))
            mutIndex[name] = weaponSchema
        }
        index = mutIndex.toMap()
    }

    fun getArmorSchema(armor: String): ArmorSchema? = index[armor]

}
