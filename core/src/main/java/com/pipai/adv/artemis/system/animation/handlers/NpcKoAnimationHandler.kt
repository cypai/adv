package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.PartialRenderComponent
import com.pipai.adv.artemis.components.PartialRenderHeightInterpolationComponent
import com.pipai.adv.artemis.components.PartialRenderHeightInterpolationEndStrategy
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.log.NpcKoEvent
import net.mostlyoriginal.api.event.common.EventSystem

class NpcKoAnimationHandler(val config: AdvConfig, world: World) {

    private lateinit var mPartialRender: ComponentMapper<PartialRenderComponent>
    private lateinit var mInterpolation: ComponentMapper<PartialRenderHeightInterpolationComponent>

    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sEvent: EventSystem

    init {
        world.inject(this)
    }

    fun animate(koEvent: NpcKoEvent) {
        val entityId = sNpcId.getNpcEntityId(koEvent.npcId)

        if (entityId != null) {
            mPartialRender.create(entityId)
            val cInterpolation = mInterpolation.create(entityId)
            cInterpolation.start = 1f
            cInterpolation.end = 0f
            cInterpolation.maxT = 15
            cInterpolation.interpolation = Interpolation.pow2In
            cInterpolation.onEnd = PartialRenderHeightInterpolationEndStrategy.DESTROY
            cInterpolation.onEndCallback = { sEvent.dispatch(BattleEventAnimationEndEvent(koEvent)) }
        }
    }

}
