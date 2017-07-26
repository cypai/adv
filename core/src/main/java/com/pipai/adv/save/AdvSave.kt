package com.pipai.adv.save

import com.pipai.adv.npc.NpcList

class AdvSave {

    val globalNpcList = NpcList()

    var playerGuild: String = "Moriya"

    val guilds: MutableMap<String, MutableList<Int>>

    init {
        guilds = mutableMapOf()
        guilds.put(playerGuild, mutableListOf())
    }
}
