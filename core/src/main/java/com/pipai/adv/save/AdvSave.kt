package com.pipai.adv.save

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.domain.NpcList
import com.pipai.adv.map.WorldMapLocation

class AdvSave() {

    constructor(data: String) : this() {
        val lines = data.split("\n")
        playerGuild = lines[0].trim()
        guilds = mapper.readValue(lines[1])
        globalNpcList = mapper.readValue(lines[2])
        classes = mapper.readValue(lines[3])
        sp = mapper.readValue(lines[4])
        gold = lines[5].toInt()
        inventory = mapper.readValue(lines[6])
        squads = mapper.readValue(lines[7])
        squadLocations = mapper.readValue(lines[8])
        squadDestinations = mapper.readValue(lines[9])
        availableQuests = mapper.readValue(lines[10])
        activeQuests = mapper.readValue(lines[11])
        finishedQuests = mapper.readValue(lines[12])
        variables = mapper.readValue(lines[13])
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

    var classes: MutableMap<Int, String?> = mutableMapOf()
        private set

    var sp: MutableMap<Int, Int> = mutableMapOf()
        private set

    var gold: Int = 500

    var inventory: MutableList<InventoryItem> = mutableListOf()
        private set

    var squads: MutableMap<String, MutableList<Int>> = mutableMapOf()
        private set

    var squadLocations: MutableMap<String, WorldMapLocation> = mutableMapOf()
        private set

    var squadDestinations: MutableMap<String, WorldMapLocation> = mutableMapOf()
        private set

    var availableQuests: MutableList<String> = mutableListOf()
        private set

    var activeQuests: MutableMap<String, String> = mutableMapOf()
        private set

    var finishedQuests: MutableList<String> = mutableListOf()
        private set

    var variables: MutableMap<String, String> = mutableMapOf()
        private set

    init {
        guilds = mutableMapOf()
        guilds.put(playerGuild, mutableListOf())
    }

    fun questTaken(quest: String): Boolean {
        return quest in activeQuests || quest in finishedQuests
    }

    fun playerTheoreticalRank(): Char {
        if (finishedQuests.contains("Guild Exam: D")) {
            return 'D'
        } else {
            return 'F'
        }
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

    fun setSquad(name: String, npcIds: List<Int>) {
        squads[name] = npcIds.toMutableList()
    }

    fun changeSquad(name: String, newName: String, npcIds: List<Int>) {
        squads.remove(name)
        squads[newName] = npcIds.toMutableList()
        val previousLocation = squadLocations[name]!!
        squadLocations.remove(name)
        squadLocations[newName] = previousLocation
        val previousDestination = squadDestinations[name]
        if (previousDestination != null) {
            squadDestinations.remove(name)
            squadDestinations[newName] = previousLocation
        }
    }

    fun removeSquad(name: String) {
        squads.remove(name)
        squadLocations.remove(name)
        squadDestinations.remove(name)
    }

    fun serialize(): String {
        val guildsLine = mapper.writeValueAsString(guilds)
        val npcListLine = mapper.writeValueAsString(globalNpcList)
        val classesLine = mapper.writeValueAsString(classes)
        val spLine = mapper.writeValueAsString(sp)
        val inventoryLine = mapper
                .writerFor(object : TypeReference<List<InventoryItem>>() {})
                .writeValueAsString(inventory)
        val squadLine = mapper.writeValueAsString(squads)
        val squadLocationLine = mapper.writeValueAsString(squadLocations)
        val squadDestinationsLine = mapper.writeValueAsString(squadDestinations)
        val availableQuestsLine = mapper.writeValueAsString(availableQuests)
        val activeQuestsLine = mapper.writeValueAsString(activeQuests)
        val finishedQuestsLine = mapper.writeValueAsString(finishedQuests)
        val variablesLine = mapper.writeValueAsString(variables)
        return "$playerGuild\n" +
                "$guildsLine\n" +
                "$npcListLine\n" +
                "$classesLine\n" +
                "$spLine\n" +
                "$gold\n" +
                "$inventoryLine\n" +
                "$squadLine\n" +
                "$squadLocationLine\n" +
                "$squadDestinationsLine\n" +
                "$availableQuestsLine\n" +
                "$activeQuestsLine\n" +
                "$finishedQuestsLine\n" +
                "$variablesLine\n"
    }
}
