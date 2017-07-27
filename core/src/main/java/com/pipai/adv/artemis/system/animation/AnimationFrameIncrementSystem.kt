package com.pipai.adv.artemis.system.animation

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class AnimationFrameIncrementSystem : IteratingSystem(allOf()) {

    private val mAnimationFrames by require<AnimationFramesComponent>()

    override protected fun process(entityId: Int) {
        val cAnimationFrames = mAnimationFrames.get(entityId)
        cAnimationFrames.t += 1
        if (cAnimationFrames.t > cAnimationFrames.tMax) {
            cAnimationFrames.t = 0
            cAnimationFrames.frame += 1
            if (cAnimationFrames.frame > cAnimationFrames.frameMax) {
                cAnimationFrames.frame = 0
            }
        }
    }
}
