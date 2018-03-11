package com.pipai.adv.artemis.system.misc

import com.artemis.managers.TagManager
import com.pipai.adv.ai.SimpleAi
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.events.CommandAnimationEndEvent
import com.pipai.adv.artemis.events.EndTurnEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleAiSystem : NoProcessingSystem() {

    private val mBackend by mapper<BattleBackendComponent>()

    private val sBattleAnimation by system<BattleAnimationSystem>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    private var active = false
    private val unitAi: MutableMap<Int, SimpleAi> = mutableMapOf()
    private val unitsToMove: MutableList<Int> = mutableListOf()

    fun initializeAi() {
        val backend = getBackend()
        backend.getTeam(Team.AI).forEach {
            unitAi[it] = SimpleAi(backend, it)
        }
    }

    @Subscribe
    fun beginTurn(event: EndTurnEvent) {
        val backend = getBackend()
        if (event.team == Team.PLAYER) {
            unitsToMove.clear()
            unitsToMove.addAll(backend.getTeam(Team.AI).filter { BattleUtils.canTakeAction(it, 1, backend) })
            active = true
            performMove()
        }
    }

    @Subscribe
    fun commandAnimationEndListener(@Suppress("UNUSED_PARAMETER") event: CommandAnimationEndEvent) {
        if (active) {
            performMove()
        }
    }

    private fun performMove() {
        val backend = getBackend()
        val npcId = unitsToMove.firstOrNull()
        if (npcId == null) {
            backend.endTurn()
            sEvent.dispatch(EndTurnEvent(Team.AI))
            active = false
        } else {
            val events = backend.execute(unitAi[npcId]!!.generateCommand())
            if (backend.getNpcAp(npcId) == 0) {
                unitsToMove.removeAt(0)
            }
            sBattleAnimation.processBattleEvents(events)
        }
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

}
