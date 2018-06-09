package com.pipai.adv.index

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.backend.battle.domain.WeaponAttribute
import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.domain.WeaponSchema
import com.pipai.adv.backend.battle.domain.WeaponType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class WeaponSchemaIndex(weaponsFile: FileHandle) {

    val index: Map<String, WeaponSchema>

    init {
        val rawData = weaponsFile.readString("UTF-8")
        val parser = CSVParser.parse(rawData, CSVFormat.DEFAULT.withHeader())

        val mutIndex: MutableMap<String, WeaponSchema> = mutableMapOf()
        for (record in parser.records) {
            val name = record.get("name")
            val weaponSchema = WeaponSchema(
                    name,
                    WeaponType.valueOf(record.get("type")),
                    WeaponRange.valueOf(record.get("range")),
                    record.get("patk").toInt(),
                    record.get("matk").toInt(),
                    record.get("attributes")
                            .split("|")
                            .filter { it.isNotBlank() }
                            .map { WeaponAttribute.valueOf(it) },
                    record.get("magazineSize").toInt(),
                    record.get("value").toInt(),
                    record.get("description"))
            mutIndex[name] = weaponSchema
        }
        index = mutIndex.toMap()
    }

    fun getWeaponSchema(weapon: String): WeaponSchema? = index[weapon]

}
