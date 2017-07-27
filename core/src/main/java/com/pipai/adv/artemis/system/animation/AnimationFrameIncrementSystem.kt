package com.pipai.adv.artemis.system.animation

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require
import com.pipai.adv.utils.RNG

class AnimationFrameIncrementSystem : IteratingSystem(allOf()) {

    private val mAnimationFrames by require<AnimationFramesComponent>()

    override protected fun process(entityId: Int) {
        val cAnimationFrames = mAnimationFrames.get(entityId)
        if (cAnimationFrames.freeze) {
            return
        }
        cAnimationFrames.t += 1
        if (cAnimationFrames.t > cAnimationFrames.tMax) {
            cAnimationFrames.t = if (cAnimationFrames.tStartNoise > 0) RNG.nextInt(cAnimationFrames.tStartNoise) else 0
            cAnimationFrames.frame += 1
            if (cAnimationFrames.frame > cAnimationFrames.frameMax) {
                cAnimationFrames.frame = 0
            }
        }
    }
}
