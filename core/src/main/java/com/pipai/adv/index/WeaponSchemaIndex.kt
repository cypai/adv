package com.pipai.adv.index

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.backend.battle.domain.WeaponSchema
import com.pipai.adv.backend.battle.domain.WeaponType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class WeaponSchemaIndex(weaponsFile: FileHandle) {

    val index: Map<String, WeaponSchema>

    init {
        val rawData = weaponsFile.readString("UTF-8");
        val parser = CSVParser.parse(rawData, CSVFormat.DEFAULT.withHeader());

        val mutIndex: MutableMap<String, WeaponSchema> = mutableMapOf()
        for (record in parser.getRecords()) {
            val name = record.get("name")
            val weaponSchema = WeaponSchema(
                    name,
                    WeaponType.valueOf(record.get("type")),
                    record.get("atk").toInt(),
                    record.get("rarity").toInt())
            mutIndex.put(name, weaponSchema)
        }
        index = mutIndex.toMap()
    }

}
