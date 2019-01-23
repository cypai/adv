package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.domain.Npc
import com.pipai.adv.utils.AutoIncrementIdMap

class NpcStatusState(npcList: AutoIncrementIdMap<Npc>) {

    private val npcAilments: MutableMap<Int, AilmentInstance?> = mutableMapOf()
    private val npcBinds: MutableMap<Int, BindStatus> = mutableMapOf()
    private val npcStatus: MutableMap<Int, MutableList<NpcStatusInstance>> = mutableMapOf()

    init {
        npcList.forEach {
            npcAilments[it.key] = null
            npcBinds[it.key] = BindStatus(0, 0, 0)
            npcStatus[it.key] = mutableListOf()
        }
    }

    fun getNpcAilment(npcId: Int): AilmentInstance? {
        return npcAilments[npcId]
    }

    fun setNpcAilment(npcId: Int, ailment: AilmentInstance?) {
        npcAilments[npcId] = ailment
    }

    fun getNpcBind(npcId: Int): BindStatus = npcBinds[npcId]
            ?: throw IllegalArgumentException("NPC $npcId does not exist")

    fun getNpcBind(npcId: Int, bodyPart: BodyPart): Int {
        val status = npcBinds[npcId] ?: throw IllegalArgumentException("NPC $npcId does not exist")
        return when (bodyPart) {
            BodyPart.HEAD -> status.head
            BodyPart.ARMS -> status.arms
            BodyPart.LEGS -> status.legs
        }
    }

    fun setNpcBind(npcId: Int, bodyPart: BodyPart, turns: Int) {
        val status = npcBinds[npcId] ?: throw IllegalArgumentException("NPC $npcId does not exist")
        return when (bodyPart) {
            BodyPart.HEAD -> status.head += turns
            BodyPart.ARMS -> status.arms += turns
            BodyPart.LEGS -> status.legs += turns
        }
    }

    fun getNpcStatus(npcId: Int): List<NpcStatusInstance> {
        return npcStatus[npcId] ?: throw IllegalArgumentException("NPC $npcId does not exist")
    }

    fun addNpcStatus(npcId: Int, status: NpcStatusInstance) {
        npcStatus[npcId]?.add(status)
    }

    fun checkNpcStatus(npcId: Int, status: NpcStatus): Boolean {
        return npcStatus[npcId]?.filter { it.status == status }.orEmpty().isNotEmpty()
    }

    fun decreaseTurnCount() {
        npcAilments.forEach { it.value?.turns?.minus(1) }
        npcAilments.filterValues { it != null }.forEach { npcId, _ -> npcAilments[npcId] = null }
        npcBinds.forEach {
            it.value.head = Math.max(0, it.value.head - 1)
            it.value.arms = Math.max(0, it.value.arms - 1)
            it.value.legs = Math.max(0, it.value.legs - 1)
        }
        npcStatus.forEach { it.value.forEach { it.turns -= 1 } }
        npcStatus.forEach { npcId, status -> npcStatus[npcId] = status.filter { it.turns > 0 }.toMutableList() }
    }
}
