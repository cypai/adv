package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.InterpolationComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class InterpolationSystem : IteratingSystem(allOf()) {
    private val mInterpolation by require<InterpolationComponent>()

    override fun process(entityId: Int) {
        val cInterpolation = mInterpolation.get(entityId)
        if (cInterpolation.t > cInterpolation.maxT) {
            mInterpolation.remove(entityId)
        } else {
            cInterpolation.t++
        }
    }
}
