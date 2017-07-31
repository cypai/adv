package com.pipai.adv.npc

import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.utils.DeepCopyable
import com.pipai.utils.ShallowCopyable

class NpcList : Iterable<Map.Entry<Int, Npc>>, ShallowCopyable<NpcList> {
    private var nextId = 0
    private val npcs: MutableMap<Int, Npc> = mutableMapOf()

    override operator fun iterator(): Iterator<Map.Entry<Int, Npc>> {
        return npcs.asIterable().iterator()
    }

    fun addNpc(npc: Npc): Int {
        val id = nextId
        npcs.put(id, npc)
        nextId += 1
        return id
    }

    fun setNpc(npc: Npc, id: Int) {
        npcs.put(id, npc)
        if (id >= nextId) {
            nextId = id + 1
        }
    }

    fun removeNpc(id: Int) {
        if (npcExists(id)) {
            npcs.remove(id)
        }
    }

    fun clear() {
        nextId = 0
        npcs.clear()
    }

    fun npcExists(id: Int): Boolean = npcs.containsKey(id)

    fun getNpc(id: Int): Npc {
        if (!npcExists(id)) {
            throw IllegalArgumentException("NPC id ${id} does not exist")
        }
        return npcs.get(id)!!
    }

    override fun shallowCopy(): NpcList {
        val copy = NpcList()
        npcs.forEach { copy.setNpc(it.value, it.key) }
        return copy
    }
}

data class Npc(val unitInstance: UnitInstance,
               val tilesetMetadata: EnvObjTilesetMetadata) : DeepCopyable<Npc> {

    override fun deepCopy() = copy(unitInstance.deepCopy(), tilesetMetadata.deepCopy())
}
