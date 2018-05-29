package com.pipai.adv.save

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pipai.adv.classes.ClassTree
import com.pipai.adv.domain.NpcList

class AdvSave() {

    constructor(data: String) : this() {
        val lines = data.split("\n")
        playerGuild = lines[0].trim()
        guilds = mapper.readValue(lines[1])
        globalNpcList = mapper.readValue(lines[2])
        classes = mapper.readValue(lines[3])
        sp = mapper.readValue(lines[4])
    }

    companion object {
        val mapper = jacksonObjectMapper()
    }

    var globalNpcList = NpcList()
        private set

    var playerGuild: String = "Moriya"
        private set

    var guilds: MutableMap<String, MutableList<Int>>
        private set

    var classes: MutableMap<Int, ClassTree?> = mutableMapOf()
        private set

    var sp: MutableMap<Int, Int> = mutableMapOf()
        private set

    init {
        guilds = mutableMapOf()
        guilds.put(playerGuild, mutableListOf())
    }

    fun changePlayerGuildName(name: String) {
        guilds.put(name, guilds[playerGuild]!!)
        guilds.remove(playerGuild)
        playerGuild = name
    }

    fun addToGuild(name: String, npcId: Int) {
        if (!guildExists(name)) {
            throw IllegalArgumentException("$name does not exist as a guild")
        }
        guilds[name]!!.add(npcId)
        if (npcInPlayerGuild(npcId)) {
            classes[npcId] = null
            sp[npcId] = globalNpcList.getNpc(npcId)!!.unitInstance.level
        }
    }

    fun guildExists(name: String): Boolean = guilds.keys.contains(name)

    fun npcInPlayerGuild(npcId: Int): Boolean = guilds[playerGuild]?.contains(npcId) ?: false

    fun serialize(): String {
        val guildsLine = mapper.writeValueAsString(guilds)
        val npcListLine = mapper.writeValueAsString(globalNpcList)
        val classesLine = mapper.writeValueAsString(classes)
        val spLine = mapper.writeValueAsString(sp)
        return "$playerGuild\n$guildsLine\n$npcListLine\n$classesLine\n$spLine"
    }
}
