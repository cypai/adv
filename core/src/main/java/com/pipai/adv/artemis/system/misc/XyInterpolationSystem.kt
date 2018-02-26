package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class XyInterpolationSystem : IteratingSystem(allOf()) {
    private val mPath by require<PathInterpolationComponent>()
    private val mXy by require<XYComponent>()

    override fun process(entityId: Int) {
        val cXy = mXy.get(entityId)
        val cPath = mPath.get(entityId)
        cXy.setXy(cPath.getCurrentPos())
    }
}
