package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.NpcIdComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.MoveEvent
import com.pipai.adv.utils.GridUtils

class MoveAnimationHandler(val config: AdvConfig, world: World) {

    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mNpc: ComponentMapper<NpcIdComponent>

    private lateinit var sNpcId: NpcIdSystem

    init {
        world.inject(this)
    }

    fun animate(moveEvent: MoveEvent) {
        val entityId = sNpcId.getNpcEntityId(moveEvent.npcId)

        if (entityId != null) {
            val destination = GridUtils.gridPositionToLocal(moveEvent.endPosition, config.resolution.tileSize.toFloat())
            val cXy = mXy.get(entityId)
            cXy.x = destination.x
            cXy.y = destination.y + config.resolution.tileOffset
        }
    }

}
