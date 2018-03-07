package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.PartialRenderComponent
import com.pipai.adv.artemis.components.PartialRenderHeightInterpolationComponent
import com.pipai.adv.artemis.components.PartialRenderHeightInterpolationEndStrategy
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class PartialRenderHeightInterpolationSystem : IteratingSystem(allOf()) {
    private val mPartialRender by require<PartialRenderComponent>()
    private val mInterpolation by require<PartialRenderHeightInterpolationComponent>()

    override fun process(entityId: Int) {
        val cPartialRender = mPartialRender.get(entityId)
        val cInterpolation = mInterpolation.get(entityId)
        cPartialRender.heightPercentage = cInterpolation.heightPercentage()

        cInterpolation.t += cInterpolation.tIncrement
        if (cInterpolation.t > cInterpolation.maxT) {
            cInterpolation.onEndCallback?.invoke()
            when (cInterpolation.onEnd) {
                PartialRenderHeightInterpolationEndStrategy.REMOVE -> mInterpolation.remove(entityId)
                PartialRenderHeightInterpolationEndStrategy.DESTROY -> world.delete(entityId)
            }
        }
    }
}
