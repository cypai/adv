package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.log.NormalAttackEvent
import net.mostlyoriginal.api.event.common.EventSystem

class NormalAttackAnimationHandler(world: World) {

    private lateinit var mXy: ComponentMapper<XYComponent>

    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sCameraInterpolation: CameraInterpolationSystem
    private lateinit var sEvent: EventSystem

    init {
        world.inject(this)
    }

    fun animate(normalAttackEvent: NormalAttackEvent) {
        val entityId = sNpcId.getNpcEntityId(normalAttackEvent.attackerId)
        
        if (entityId == null) {
            sEvent.dispatch(BattleEventAnimationEndEvent(normalAttackEvent))
        } else {
            val cXy = mXy.get(entityId)
            sCameraInterpolation.sendCameraToPosition(cXy.toVector2(), { sEvent.dispatch(BattleEventAnimationEndEvent(normalAttackEvent)) })
        }
    }

}
