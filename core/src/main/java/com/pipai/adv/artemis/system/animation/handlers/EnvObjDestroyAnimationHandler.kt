package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.World
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.EnvObjIdSystem
import com.pipai.adv.backend.battle.engine.log.EnvObjectDestroyEvent
import net.mostlyoriginal.api.event.common.EventSystem

class EnvObjDestroyAnimationHandler(private val world: World) {

    private lateinit var sEnvObjId: EnvObjIdSystem
    private lateinit var sEvent: EventSystem

    init {
        world.inject(this)
    }

    fun animate(destroyEvent: EnvObjectDestroyEvent) {
        val entityId = sEnvObjId.getEnvObjEntityId(destroyEvent.envObjId)

        if (entityId != null) {
            world.delete(entityId)
            sEvent.dispatch(BattleEventAnimationEndEvent(destroyEvent))
        }
    }

}
