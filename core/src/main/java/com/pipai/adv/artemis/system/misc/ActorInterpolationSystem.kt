package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.ActorComponent
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class ActorInterpolationSystem : IteratingSystem(allOf()) {
    private val mPath by require<PathInterpolationComponent>()
    private val mActor by require<ActorComponent>()

    override fun process(entityId: Int) {
        val cActor = mActor.get(entityId)
        val cPath = mPath.get(entityId)
        val position = cPath.getCurrentPos()
        cActor.actor.x = position.x
        cActor.actor.y = position.y
    }
}
