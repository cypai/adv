package com.pipai.adv.artemis.system.animation

import com.artemis.managers.TagManager
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.events.BattleTextEvent
import com.pipai.adv.artemis.events.CommandAnimationEndEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.animation.handlers.*
import com.pipai.adv.artemis.system.ui.BattleEndSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.log.*
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleAnimationSystem(private val game: AdvGame) : NoProcessingSystem() {

    private val logger = getLogger()

    private val mBackend by mapper<BattleBackendComponent>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    private val sBattleEnd by system<BattleEndSystem>()

    private lateinit var moveAnimationHandler: MoveAnimationHandler
    private lateinit var damageAnimationHandler: DamageAnimationHandler
    private lateinit var healAnimationHandler: HealAnimationHandler
    private lateinit var npcKoAnimationHandler: NpcKoAnimationHandler
    private lateinit var delayAnimationHandler: DelayAnimationHandler
    private lateinit var normalAttackAnimationHandler: NormalAttackAnimationHandler
    private lateinit var tpUseAnimationHandler: TpUseAnimationHandler

    private val animatingEvents: MutableList<BattleLogEvent> = mutableListOf()

    override fun initialize() {
        moveAnimationHandler = MoveAnimationHandler(game.advConfig, world)
        damageAnimationHandler = DamageAnimationHandler(game.advConfig, game.smallFont, world)
        healAnimationHandler = HealAnimationHandler(game.advConfig, game.smallFont, world)
        npcKoAnimationHandler = NpcKoAnimationHandler(game.advConfig, world)
        delayAnimationHandler = DelayAnimationHandler(world)
        normalAttackAnimationHandler = NormalAttackAnimationHandler(world)
        tpUseAnimationHandler = TpUseAnimationHandler(game.advConfig, world)
    }

    @Subscribe
    fun handleBattleEventAnimationEnd(@Suppress("UNUSED_PARAMETER") event: BattleEventAnimationEndEvent) {
        animateNextEvent()
    }

    fun processBattleEvents(events: List<BattleLogEvent>) {
        logger.debug("Received animation request for $events")
        animatingEvents.addAll(events)
        animateNextEvent()
    }

    private fun animateNextEvent() {
        if (animatingEvents.isEmpty()) {
            logger.debug("Animation finished")
            sEvent.dispatch(CommandAnimationEndEvent())
        } else {
            val event = animatingEvents.removeAt(0)
            logger.debug("Animating $event")
            sEvent.dispatch(BattleTextEvent(event.userFriendlyDescription()))
            animateEvent(event)
        }
    }

    private fun animateEvent(event: BattleLogEvent) {
        when (event) {
            is MoveEvent -> moveAnimationHandler.animate(event)
            is DamageEvent -> damageAnimationHandler.animate(event)
            is HealEvent -> healAnimationHandler.animate(event)
            is NpcKoEvent -> npcKoAnimationHandler.animate(event)
            is NormalAttackEvent -> normalAttackAnimationHandler.animate(event)
            is TpChangeEvent -> tpUseAnimationHandler.animate(event)
            is BattleEndEvent -> sBattleEnd.activateEndSequence(event)
            else -> {
                val backend = getBackend()
                when (backend.getCurrentTurn()) {
                    Team.PLAYER -> sEvent.dispatch(BattleEventAnimationEndEvent(event))
                    Team.AI -> delayAnimationHandler.animate(event, 1)
                }
            }
        }
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

}
