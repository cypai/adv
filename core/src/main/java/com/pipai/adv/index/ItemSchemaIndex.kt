package com.pipai.adv.index

import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.backend.battle.domain.ItemSchema
import com.pipai.adv.backend.battle.domain.ItemType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class ItemSchemaIndex(itemsFile: FileHandle) {

    val index: Map<String, ItemSchema>

    init {
        val rawData = itemsFile.readString("UTF-8")
        val parser = CSVParser.parse(rawData, CSVFormat.DEFAULT.withHeader())

        val mutIndex: MutableMap<String, ItemSchema> = mutableMapOf()
        for (record in parser.records) {
            val name = record.get("name")
            val itemSchema = ItemSchema(
                    name,
                    ItemType.valueOf(record.get("type")),
                    record.get("value").toInt(),
                    record.get("description"))
            mutIndex[name] = itemSchema
        }
        index = mutIndex.toMap()
    }

    fun getItemSchema(item: String): ItemSchema? = index[item]

}
