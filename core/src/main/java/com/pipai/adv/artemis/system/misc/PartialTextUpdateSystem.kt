package com.pipai.adv.artemis.system.misc

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.CameraFollowComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system
import com.pipai.adv.artemis.components.PartialTextComponent
import com.pipai.adv.utils.MathUtils

class PartialTextUpdateSystem() : IteratingSystem(allOf()) {

    private val mPartialText by require<PartialTextComponent>()

    override fun process(entityId: Int) {
        val cPartialText = mPartialText.get(entityId)
        if (cPartialText.currentText.length < cPartialText.fullText.length) {
            cPartialText.timer -= 1
            if (cPartialText.timer <= 0) {
                cPartialText.timer = cPartialText.timerSlowness
                val substringIndex = Math.min(cPartialText.currentText.length + cPartialText.textUpdateRate,
                        cPartialText.fullText.length)
                cPartialText.currentText = cPartialText.fullText.substring(0, substringIndex)
            }
        }
    }

}
