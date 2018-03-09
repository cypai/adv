package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.EndStrategy
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
            cPath.onEndpoint?.invoke(cPath)
            if (cPath.endpointIndex >= cPath.endpoints.size - 1) {
                when (cPath.onEnd) {
                    EndStrategy.REMOVE -> mPath.remove(entityId)
                    EndStrategy.DESTROY -> world.delete(entityId)
                    EndStrategy.RESTART -> cPath.endpointIndex = 0
                }
            }
        }
    }
}
