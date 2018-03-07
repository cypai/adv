package com.pipai.adv.artemis.system.animation

import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.events.CommandAnimationEndEvent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.animation.handlers.DamageAnimationHandler
import com.pipai.adv.artemis.system.animation.handlers.MoveAnimationHandler
import com.pipai.adv.artemis.system.animation.handlers.NpcKoAnimationHandler
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.backend.battle.engine.log.NpcKoEvent
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleAnimationSystem(private val game: AdvGame) : NoProcessingSystem() {

    private val logger = getLogger()

    private val sEvent by system<EventSystem>()

    private lateinit var moveAnimationHandler: MoveAnimationHandler
    private lateinit var damageAnimationHandler: DamageAnimationHandler
    private lateinit var npcKoAnimationHandler: NpcKoAnimationHandler

    private val animatingEvents: MutableList<BattleLogEvent> = mutableListOf()

    override fun initialize() {
        moveAnimationHandler = MoveAnimationHandler(game.advConfig, world)
        damageAnimationHandler = DamageAnimationHandler(game.advConfig, game.smallFont, world)
        npcKoAnimationHandler = NpcKoAnimationHandler(game.advConfig, world)
    }

    @Subscribe
    fun handleBattleEventAnimationEnd(event: BattleEventAnimationEndEvent) {
        animateNextEvent()
    }

    fun processBattleEvents(events: List<BattleLogEvent>) {
        logger.debug("Animating $events")
        animatingEvents.addAll(events)
        animateNextEvent()
    }

    private fun animateNextEvent() {
        if (animatingEvents.isEmpty()) {
            sEvent.dispatch(CommandAnimationEndEvent())
        } else {
            animateEvent(animatingEvents.removeAt(0))
        }
    }

    private fun animateEvent(event: BattleLogEvent) {
        when (event) {
            is MoveEvent -> moveAnimationHandler.animate(event)
            is DamageEvent -> damageAnimationHandler.animate(event)
            is NpcKoEvent -> npcKoAnimationHandler.animate(event)
            else -> sEvent.dispatch(BattleEventAnimationEndEvent(event))
        }
    }

}
