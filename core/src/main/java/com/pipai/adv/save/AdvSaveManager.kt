package com.pipai.adv.save

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.utils.AlphanumComparator

class SaveManager(private val path: String) {

    constructor() : this("save/")

    private val regex = Regex("save([0-9]+)\\.txt")

    fun save(slot: Int, save: AdvSave) {
        val handle = generateFileHandle(slot)
        handle.writeString(save.serialize(), false)
    }

    fun load(slot: Int): AdvSave {
        val handle = generateFileHandle(slot)
        return load(handle)
    }

    fun load(handle: FileHandle): AdvSave {
        val data = handle.readString()
        return AdvSave(data)
    }

    fun delete(slot: Int) {
        val handle = generateFileHandle(slot)
        handle.delete()
    }

    fun getAllSaves(): List<AdvSaveSlot> {
        return Gdx.files.local(path).list()
                .filter { it.name().matches(regex) }
                .sortedWith(AlphanumComparator({ it.name() }))
                .map { AdvSaveSlot(load(it).playerGuild, parseSaveSlot(it.name())!!) }
    }

    private fun parseSaveSlot(filename: String): Int? {
        return regex.matchEntire(filename)?.groups?.get(1)?.value?.toInt()
    }

    private fun generateFileHandle(slot: Int): FileHandle = Gdx.files.local("${path}save${slot}.txt")
}
