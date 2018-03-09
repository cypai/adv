package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.EndStrategy
import com.pipai.adv.artemis.components.TimerComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class TimerSystem : IteratingSystem(allOf()) {
    private val mTimer by require<TimerComponent>()

    override fun process(entityId: Int) {
        val cTimer = mTimer.get(entityId)
        cTimer.t += cTimer.tIncrement
        if (cTimer.t > cTimer.maxT) {
            cTimer.onEndCallback?.invoke()
            when (cTimer.onEnd) {
                EndStrategy.REMOVE -> mTimer.remove(entityId)
                EndStrategy.RESTART -> cTimer.t = 0
                EndStrategy.DESTROY -> world.delete(entityId)
            }
        }
    }
}
