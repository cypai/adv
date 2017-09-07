package com.pipai.adv.save

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle

class SaveManager(private val path: String) {

    constructor() : this("save/")

    fun save(slot: Int, save: AdvSave) {
        val handle = generateFileHandle(slot)
        handle.writeString(save.serialize(), false)
    }

    fun load(slot: Int): AdvSave {
        val handle = generateFileHandle(slot)
        val data = handle.readString()
        return AdvSave(data)
    }

    fun delete(slot: Int) {
        val handle = generateFileHandle(slot)
        handle.delete()
    }

    private fun generateFileHandle(slot: Int): FileHandle = Gdx.files.local("${path}save${slot}.txt")

}
