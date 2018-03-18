package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.pipai.adv.artemis.components.TimerComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import net.mostlyoriginal.api.event.common.EventSystem

class DelayAnimationHandler(private val world: World) {

    private lateinit var mTimer: ComponentMapper<TimerComponent>

    private lateinit var sEvent: EventSystem

    init {
        world.inject(this)
    }

    fun animate(event: BattleLogEvent, delay: Int) {
        val entityId = world.create()
        val cTimer = mTimer.create(entityId)
        cTimer.maxT = delay
        cTimer.onEndCallback = { sEvent.dispatch(BattleEventAnimationEndEvent(event)) }
    }

}
