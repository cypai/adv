package com.pipai.adv.artemis.system.animation

import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.animation.handlers.MoveAnimationHandler
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.utils.getLogger

class BattleAnimationSystem(private val game: AdvGame) : NoProcessingSystem() {

    private val logger = getLogger()

    private lateinit var moveAnimationHandler: MoveAnimationHandler

    override fun initialize() {
        moveAnimationHandler = MoveAnimationHandler(game.advConfig, world)
    }

    fun processBattleEvents(events: List<BattleLogEvent>) {
        logger.debug("Animating $events")
        for (event in events) {
            when (event) {
                is MoveEvent -> moveAnimationHandler.animate(event)
            }
        }
    }

}
