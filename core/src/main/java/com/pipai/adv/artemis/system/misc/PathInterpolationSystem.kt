package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.PathInterpolationEndStrategy
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class PathInterpolationSystem : IteratingSystem(allOf()) {
    private val mPath by require<PathInterpolationComponent>()

    override fun process(entityId: Int) {
        val cPath = mPath.get(entityId)

        cPath.t += cPath.tIncrement
        if (cPath.t > cPath.maxT) {
            cPath.endpointIndex++
            if (cPath.speed > 0) {
                cPath.setUsingSpeed(cPath.speed)
            } else {
                cPath.t = 0
            }
            if (cPath.onEndpoint != null) {
                cPath.onEndpoint?.invoke(cPath)
            }
            if (cPath.endpointIndex >= cPath.endpoints.size - 1) {
                when (cPath.onEnd) {
                    PathInterpolationEndStrategy.REMOVE -> mPath.remove(entityId)
                    PathInterpolationEndStrategy.DESTROY -> world.delete(entityId)
                    PathInterpolationEndStrategy.RESTART -> cPath.endpointIndex = 0
                }
            }
        }
    }
}
